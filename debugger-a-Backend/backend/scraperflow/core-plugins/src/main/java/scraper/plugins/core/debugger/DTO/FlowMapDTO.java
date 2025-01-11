package scraper.plugins.core.debugger.DTO;

import scraper.api.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class FlowMapDTO {
    private final Map<String, Object> content = new HashMap<>();

    private final String flowId;
    private final int sequence;

    private final String parentId;
    private final Integer parentSequence;
    private final ConcurrentMap<String, Object> context;

    private final boolean successful;
    //
    public Map<String, ?> getContent() { return content; }
    public String getParentId() { return parentId; }
    public Integer getSequence() { return sequence; }
    public String getFlowId() { return flowId; }
    public Integer getParentSequence() { return parentSequence; }
    public boolean getSuccessful() { return successful; }
    public ConcurrentMap<String, Object> getContext() { return context; }

    public FlowMapDTO(FlowMap o ) {
        //o.keySet().forEach(k -> content.put(k, o.eval(k).orElse(null)));
        o.forEach((k, v) -> content.put(k.getTarget().toString(), v));
        if(o.getParentId().isPresent()) {
            parentId = o.getParentId().get().toString();
            this.parentSequence = o.getParentSequence().get();
        } else {
            parentId = null;
            parentSequence = null;
        }

        this.flowId = o.getId().toString();
        this.sequence = o.getSequence();
        this.successful = o.getSuccessful();

        FlowMapImpl flowImpl = (FlowMapImpl) o;
        this.context = flowImpl.getPrivateMap();
    }
}
