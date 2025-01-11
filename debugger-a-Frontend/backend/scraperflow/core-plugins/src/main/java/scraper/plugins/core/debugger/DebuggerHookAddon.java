package scraper.plugins.core.debugger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import scraper.annotations.ArgsCommand;
import scraper.annotations.NotNull;
import scraper.api.*;
import scraper.plugins.core.debugger.DTO.InstanceDTO;
import scraper.plugins.core.debugger.DTO.MessageStackDTO;
import scraper.plugins.core.debugger.DTO.NodeDTO;
import scraper.plugins.core.debugger.Graph.NodeItem;
import scraper.plugins.core.debugger.Graph.NodeState;
import scraper.plugins.core.debugger.Utils.StoppedException;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.GraphVisualizer;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.utils.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.*;


@ArgsCommand(
        value = "debug",
        doc = "Starts a debugging websocket server and waits for a debugger to be present for processing the flow. If no port is specified with debug-port, then 8890 is used.",
        example = "scraper app.scrape debug"
)
@ArgsCommand(
        value = "debug-ip",
        doc = "Binding ip for debugging. Default is 0.0.0.0",
        example = "scraper app.scrape debug debug-ip:0.0.0.0"
)
@ArgsCommand(
        value = "debug-port",
        doc = "Port for debugging. Default is 8890",
        example = "scraper app.scrape debug debug-port:8890"
)
@ArgsCommand(
        value = "stop-at-start",
        doc = "The stop the debugger at the first node",
        example = "scraper app.scrape debug stop-at-start"
)
public class DebuggerHookAddon implements Hook, Addon {
    private static final System.Logger l = System.getLogger("Debugger");

    public static DebuggerWebsocketServer debugger;
    private static ObjectMapper m = new ObjectMapper();
    private static ArrayList<InstanceDTO> impls = new ArrayList<>();

    public static DebuggerState state = new DebuggerState();
    private static ArrayList<String> graphList = new ArrayList<>();
    private static Map<String, NodeDTO> nodesMap;

    @Override
    public int order() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void execute(@NotNull DIContainer dependencies, @NotNull String[] args, @NotNull Map<ScrapeSpecification, ScrapeInstance> scraper) {
        startLogRedirection(debugger);

        for (ScrapeSpecification def : scraper.keySet()) {
            ScrapeInstance instance = scraper.get(def);
            impls.add(new InstanceDTO(instance));

            ScrapeInstance job = scraper.get(def);

            /*
            Generate all the graphs and save it.
            There will be sent later to the frontend
             */
            ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(job);
            String graph = GraphVisualizer.visualize(cfg);
            graphList.add(graph);

            nodesMap = new HashMap<>();
            job.getRoutes()
            .forEach(((address, nodeContainer) -> {
                if (address.isAbsolute()) {
                    if (!nodesMap.containsKey(nodeContainer.getAddress())) {
                        nodesMap.put(nodeContainer.getAddress().toString(), new NodeDTO(nodeContainer));
                    }
                }
            }));
        }
    }

    @Override
    public void load(@NotNull DIContainer loadedDependencies, @NotNull String[] args) {
        if (StringUtil.getArgument(args, "debug") != null) {
            l.log(System.Logger.Level.WARNING, "Debugging activated");
            String debugPort = StringUtil.getArgument(args, "debug-port");
            String stopAtStart = StringUtil.getArgument(args, "stop-at-start");
            String debugIp = StringUtil.getArgument(args, "debug-ip");
            String bindingIp = "0.0.0.0";
            int port = 8890;
            if (debugPort != null) port = Integer.parseInt(debugPort);
            if (debugIp != null) bindingIp = debugIp;

            debugger = new DebuggerWebsocketServer(this, port, bindingIp);

            if (stopAtStart == null) {
                state.setReady(true);
            } else {
                state.setReady(false);
            }
        }
    }

    // Redirect log message to websocket
    private void startLogRedirection(DebuggerWebsocketServer websocket) {
        Formatter formatter = new SimpleFormatter();

        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                websocket.get().ifPresent(client -> {
                    client.send(wrap("log",
                            Map.of(
                                    "message", formatter.format(record)
                            )
                    ));
                });
            }

            @Override
            public void close() throws SecurityException {}

            @Override
            public void flush() {}
        };

        // Add handler
        Logger.getLogger("Debugger").addHandler(handler);
        Logger.getGlobal().addHandler(handler);
    }

    public static String wrap(String type, Object data) {
        try {
            return m.writeValueAsString(Map.of("type", type, "data", data));
        } catch (JsonProcessingException e) {
            return "Error: " + e.getMessage();
        }
    }

    //======================
    // CLIENT API
    //======================

    @SuppressWarnings("unused") // give specifications about the current instances
    public void requestSpecifications(Map<String, Object> data) {
        l.log(System.Logger.Level.INFO, "Requesting specifications");
        for (InstanceDTO impl : impls) {
            debugger.get().ifPresent(client -> client.send(wrap("instance", impl)));
        }
    }

    @SuppressWarnings("unused") // switch the whole debugger to a 'Ready' State
    public void setReady(Map<String, Object> data) {
        l.log(System.Logger.Level.INFO, "Debugger is ready, waking up all flows");
        state.setReady(true);
        debugger.get().ifPresent(client -> client.send(wrap("readyState", Map.of(
                "ready", (DebuggerHookAddon.state.getReady())
        ))));
    }

    @SuppressWarnings("unused") // Execute the next Node in the process. And block after it.
    public void stepwiseExecution(Map<String, Object> data) {
        String flowId = (String) data.get("flowId");
        state.setStepwise(flowId, true);
    }

    @SuppressWarnings("unused") // Continue the execution
    public void setContinue(Map<String, Object> data) {
        String flowId = (String) data.get("flowId");
        state.setContinue(flowId, true);
    }

    @SuppressWarnings("unused") // Wakeup all the stopped/blocker flows
    public void wakeUpAll(Map<String, Object> data) {
        String flowId = (String) data.get("flowId");
        NodeItem nodeItem = state.graphstate.get(flowId);
        List<String> children = nodeItem.children;

        for(String child : children){
            state.setContinue(child, true);
        }
    }

    @SuppressWarnings({"unused"}) // set the Breakpoints defined by the user
    public void setBreakpoints(Map<String, Object> data) {
        HashMap<String, Object> breakpoints = (HashMap<String, Object>) data.get("breakpoints");

        for (var entry : breakpoints.entrySet()) {
            var value = (Map<String, Object>) entry.getValue();

            if((boolean) value.get("before")){
                state.addBeforeBreakpoint(entry.getKey());
            }
            if((boolean) value.get("after")){
                state.addAfterBreakpoint(entry.getKey());
            }
        }

        l.log(System.Logger.Level.INFO, "Breakpoints received");
    }

    @SuppressWarnings({"unused"}) // Return all the Graphs that represent the Whole flow's tree
    public void getGraphs(Map<String, Object> data) {
        debugger.get().ifPresent(client -> {
            client.send(wrap("graphs", Map.of(
                    "list", graphList,
                    "nodesMap", nodesMap
            )));
        });
    }

    @SuppressWarnings({"unused"}) // Get the current state of the debugger (Logs, informations about Nodes and flows)
    public void getState(Map<String, Object> data) {
        Map<String, MessageStackDTO> collector = new HashMap<>();

        for (var entry : state.graphstate.map.entrySet()) {
            collector.put(entry.getKey(), new MessageStackDTO(entry.getValue()));
        }
        debugger.get().ifPresent(client -> {
            client.send(wrap("state", Map.of(
                    "stack", collector,
                    "order", state.graphstate.order,
                    "ready", state.getReady()
            )));
        });
    }

    @SuppressWarnings({"unused"}) // Stop the whole process
    public void stopProcess(Map<String, Object> data) {
        state.setStopped(true);
        try {
            debugger.get().ifPresent(client -> client.send(wrap("stoppedState", Map.of(
                    "stopped", (state.getStopped())
            ))));
            throw new StoppedException("The process was stopped by the frontend");
        } catch (StoppedException e) {
            e.printStackTrace();
            l.log(System.Logger.Level.ERROR, e);
        }
    }

    @Override
    public String toString() {
        return "Debugger";
    }
}

