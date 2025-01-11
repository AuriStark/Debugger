package scraper.plugins.core.debugger.DTO;

import scraper.plugins.core.debugger.Graph.NodeItem;

import java.util.List;
import java.util.Map;

public class MessageStackDTO {
    private final Map<String, Object> nodePre;
    private final Map<String, Object> nodePost;
    private final List<String> children;
    private final String flowId;
    private final String nodeAddress;

    public Map<String, Object> getNodePre() {
        return nodePre;
    }
    public Map<String, Object> getNodePost() {
        return nodePost;
    }
    public String getNodeAddress() {
        return nodeAddress;
    }
    public String getFlowId() {
        return flowId;
    }
    public List<String> getChildren() {
        return children;
    }

    public MessageStackDTO(NodeItem message){
        this.nodePre = message.nodePre;
        this.nodePost = message.nodePost;
        this.children = message.children;
        this.flowId = message.flowId;
        this.nodeAddress = message.nodeAddress;
    }
}
