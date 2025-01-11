package scraper.plugins.core.debugger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import scraper.annotations.NotNull;
import scraper.api.FlowMap;
import scraper.api.Node;
import scraper.api.NodeContainer;
import scraper.api.NodeHook;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.core.AbstractNode;
import scraper.plugins.core.debugger.DTO.FlowMapDTO;
import scraper.plugins.core.debugger.DTO.NodeDTO;
import scraper.plugins.core.debugger.Graph.NodeState;

import java.util.Map;

@SuppressWarnings("unchecked")
public class DebuggerNodeHook implements NodeHook {
    private final ObjectMapper m = new ObjectMapper();

    // Send a wrapped message to the frontend using websocket
    public void sendMessage(String type, Map<String, Object> message ){
        DebuggerHookAddon.debugger.get().ifPresent(client -> {
            String wrapped = wrap(type, message);
            client.send(wrapped);
        });
    }

    @Override
    public void beforeProcess(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o2) {
        FlowMapImpl o = (FlowMapImpl) o2;
        AbstractNode n2 = (AbstractNode) n;

        // Create a new NodeItem for the current Node
        DebuggerHookAddon.state.graphstate.expand(n, o2);

        // Create the state of the Node
        NodeState newState = new NodeState(false, false);
        // - Get the parent's State
        var parentId = o.getParentId();
        if(parentId.isPresent()) {
            String pid = parentId.get().toString();
            NodeState parentState = DebuggerHookAddon.state.getNodeState(pid);
            // - Create new state
            if(parentState != null){
                newState = parentState.createChildState();
            }
        }
        DebuggerHookAddon.state.addNodeState(o.getId().toString(), newState);

        if(DebuggerHookAddon.debugger != null) {
            // stop the execution if the previous process throws an Exception
            // or the process was stoped
            DebuggerHookAddon.state.stopIfExeption();

            // Check for ready state
            if(!DebuggerHookAddon.state.getReady()){
                DebuggerHookAddon.state.waitUntilReady();
            }

            // Send to Front-end witch Flow is processed
            sendMessage("currentFlow", Map.of(
                    "flowId", o.getId()
            ));

            var message = Map.of(
                    "node", new NodeDTO(n2),
                    "flowMap", new FlowMapDTO(o2)
            );
            String wrapped = wrap("nodePre", message);
            //save to state
            DebuggerHookAddon.state.savePreMessage(o.getId().toString(), n2.getAddress().toString(), message, o2.getParentId());

            NodeState finalNewState = newState;
            DebuggerHookAddon.debugger.get().ifPresent(client -> {
                client.send(wrapped);

                // Check if the previous execution was a stepwise
                if(DebuggerHookAddon.state.getReady() && finalNewState.getStepwise()){
                    finalNewState.waitNextStep();
                }
                finalNewState.waitIfBeforeBreakpoint(n,
                        () -> client.send(wrap("beforeBreakpoint", Map.of("flowId", o.getId()))),
                        () -> client.send(wrap("beforeBreakpointContinue", Map.of("flowId", o.getId())))
                );
            });
        }
    }

    @Override
    public void afterProcess(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o2) {
        FlowMapImpl o = (FlowMapImpl) o2;
        AbstractNode n2 = (AbstractNode) n;

        // Get NodeState
        NodeState state = DebuggerHookAddon.state.getNodeState(o.getId().toString());

        if(DebuggerHookAddon.debugger != null) {
            // stop the execution if the previous process throws an Exception
            // or the process was stoped
            DebuggerHookAddon.state.stopIfExeption();

            var message = Map.of(
                    "node", new NodeDTO(n),
                    "flowMap", new FlowMapDTO(o)
            );

            String wrapped = wrap("nodePost", message);
            //save to state
            DebuggerHookAddon.state.savePostMessage(o.getId().toString(), n2.getAddress().toString(), message);

            DebuggerHookAddon.debugger.get().ifPresent(client -> {
                state.waitIfAfterBreakpoint(n,
                        () -> client.send(wrap("afterBreakpoint", Map.of("flowId", o.getId()))),
                        () -> client.send(wrap("afterBreakpointContinue", Map.of("flowId", o.getId())))
                );
                client.send(wrapped);

                /** If the process of this Node was successful*/
                if(o.getSuccessful()){
                } else {
                    DebuggerHookAddon.state.setStopped(true);
                }
            });
        }
    }

    @Override
    public int order() {
        return 99;
    }

    public String wrap(String type, Object data) {
        try {
            return m.writeValueAsString(Map.of("type", type, "data", data));
        } catch (JsonProcessingException e) {
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public String toString() {
        return "Debugger";
    }
}
