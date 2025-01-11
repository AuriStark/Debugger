package scraper.plugins.core.debugger.DTO;

import scraper.api.GraphAddress;
import scraper.api.Node;
import scraper.api.NodeContainer;
import scraper.core.AbstractNode;

import java.util.Map;

@SuppressWarnings("unused") // DTO
public class NodeDTO {
    private final Map<String, ?> nodeConfiguration;
    private final String address;
    private final boolean forward;
    private final GraphAddress graphKey;

    public Map<String, ?> getNodeConfiguration() { return nodeConfiguration; }
    public String getAddress() { return address; }
    public boolean getForward() { return forward; }
    public GraphAddress getGraphKey() { return graphKey; }

    public NodeDTO(NodeContainer<? extends Node> nodeContainer) {
        this.nodeConfiguration = nodeContainer.getNodeConfiguration();
        this.address = nodeContainer.getAddress().toString();
        this.forward = nodeContainer.isForward();

        AbstractNode absN = (AbstractNode) nodeContainer;
        this.graphKey = absN.getGraphKey();
    }
}
