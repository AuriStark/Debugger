package scraper.plugins.core.debugger.Graph;

import scraper.api.Address;
import scraper.api.Node;
import scraper.api.NodeContainer;
import scraper.plugins.core.debugger.DebuggerHookAddon;
import scraper.plugins.core.debugger.DebuggerState;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class NodeState {
    private static final System.Logger l = System.getLogger("Debugger");

    //determine if it is a stepwise execution or not
    private final AtomicBoolean stepwise = new AtomicBoolean(false);

    // flows will wait on this object
    private final AtomicBoolean breaking = new AtomicBoolean(false);

    public NodeState(Boolean breaking, Boolean stepwise){
        this.breaking.set(breaking);
        this.stepwise.set(stepwise);
    }

    public NodeState createChildState(){
        return new NodeState(this.breaking.get(), this.stepwise.get());
    }


    public void waitIfBeforeBreakpoint(NodeContainer<? extends Node> n, Runnable onWait, Runnable onContinue) {
        for (String breakpoint : DebuggerState.getBeforeBreakpoints()) {
            boolean result = waitIfBreakpoint(n, onWait, onContinue, breakpoint);
            if(result){
                break;
            }
        }
    }

    public void waitIfAfterBreakpoint(NodeContainer<? extends Node> n, Runnable onWait, Runnable onContinue) {
        for (String breakpoint : DebuggerState.getAfterBreakpoints()) {
            boolean result = waitIfBreakpoint(n, onWait, onContinue, breakpoint);
            if(result){
                break;
            }
        }
    }

    public boolean waitIfBreakpoint(NodeContainer<? extends Node> n, Runnable onWait, Runnable onContinue, String breakpoint){
        Address addr = n.addressOf(breakpoint);
        if(n.getAddress().equals(addr)) {
            l.log(System.Logger.Level.INFO, "BREAKPOINT TRIGGERED: {0} <-> {1}", breakpoint, n.getAddress().getRepresentation());
            synchronized (breaking) {
                try {
                    onWait.run();
                    breaking.wait();
                    return true;
                } catch (InterruptedException e) {
                    l.log(System.Logger.Level.ERROR, "Continuing because interrupt");
                    e.printStackTrace();
                } finally {
                    onContinue.run();
                }
                return false;
            }
        }
        return false;
    }

    public void waitNextStep() {
        l.log(System.Logger.Level.INFO, "Waiting for next step");
        synchronized (breaking) {
            try {
                breaking.wait();
            } catch (InterruptedException e) {
                l.log(System.Logger.Level.ERROR, "Continuing because interrupt");
                e.printStackTrace();
            }
        }
    }

    public void setContinue(boolean b) {
        synchronized (breaking) {
            breaking.set(true);
            this.stepwise.set(false);

            // if ready, notify every flow to wake up
            if(b) breaking.notifyAll();
        }
    }

    public void setStepwise(boolean b) {
        synchronized (breaking) {
            breaking.set(true);
            this.stepwise.set(b);

            // if ready, notify every flow to wake up
            if(b) breaking.notifyAll();
        }
    }

    public boolean getStepwise(){
        return this.stepwise.get();
    }
}
