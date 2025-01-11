package scraper.plugins.core.debugger;

import scraper.api.FlowMap;
import scraper.api.Node;
import scraper.api.NodeContainer;
import scraper.plugins.core.debugger.Graph.Graph;
import scraper.plugins.core.debugger.Graph.NodeItem;
import scraper.plugins.core.debugger.Graph.NodeState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class DebuggerState {
    private static final System.Logger l = System.getLogger("Debugger");

    // State of each node
    public static final Map<String, NodeState> nodestates = new ConcurrentHashMap<>();


    // flows will wait on this object
    private final AtomicBoolean ready = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    private static final Set<String> beforeBreakpoints = new HashSet<String>();
    private static final Set<String> afterBreakpoints = new HashSet<String>();

    //Save the state of message sended by socket
    public final Graph graphstate = new Graph();

    public void waitUntilReady() {
        try {
            synchronized (ready) {
                if(!ready.get()) {
                    l.log(System.Logger.Level.INFO, "Waiting for debugger to connect");
                    ready.wait();
                }
            }
        } catch (Exception ignored) {}
    }

    public void setReady(boolean b) {
        synchronized (ready) {
            ready.set(b);
            // if ready, notify every flow to wake up
            if(b) ready.notifyAll();
        }
    }

    public boolean getReady(){
        return ready.get();
    }

    public AtomicBoolean getStopped() {
        return stopped;
    }

    public void stopIfExeption() {
        try {
            synchronized (stopped) {
                if(stopped.get()) {
                    l.log(System.Logger.Level.ERROR, "Processing stopped due to Exeption");
                    stopped.wait();
                }
            }
        } catch (Exception ignored) {}
    }

    public void setStopped(boolean b) {
        synchronized (stopped) {
            stopped.set(true);
            l.log(System.Logger.Level.ERROR, "Processing stopped due to Exeption");
        }
    }

    public void setContinue(String flowId, boolean b) {
        nodestates.get(flowId).setContinue(b);
    }

    public void addAfterBreakpoint(String br) {
        afterBreakpoints.add(br);
    }

    public void addBeforeBreakpoint(String br) {
        beforeBreakpoints.add(br);
    }

    public void addNodeState(String key, NodeState state){
        nodestates.put(key, state);
    }

    public NodeState getNodeState(String key){
        return nodestates.get(key);
    }

    public void setStepwise(String flowId, boolean b) {
        nodestates.get(flowId).setStepwise(b);
    }

    public void savePreMessage(String fid, String na, Map<String, Object> message, Optional<UUID> parentId){
        graphstate.putOrder(fid);

        graphstate.get(fid).nodePre = message;

        // If the flow have a parent, save him in his parent as a child
        // Each Parent have a list of his children
        if(parentId.isPresent()){
            String pid = parentId.get().toString();
            NodeItem ms2 = graphstate.get(pid);

            if(ms2 != null){
                if(!ms2.children.contains(pid)){
                    graphstate.get(pid).children.add(fid);
                }
            }
        }
    }

    public void savePostMessage(String fid, String na, Map<String, Object> message){
        graphstate.get(fid).nodePost = message;
    }

    public static Set<String> getBeforeBreakpoints() {
        return beforeBreakpoints;
    }

    public static Set<String> getAfterBreakpoints() {
        return afterBreakpoints;
    }
}

