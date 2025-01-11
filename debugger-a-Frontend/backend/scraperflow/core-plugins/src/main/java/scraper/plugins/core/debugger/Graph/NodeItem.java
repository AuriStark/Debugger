package scraper.plugins.core.debugger.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NodeItem {
    public String type = "forward";

    public Map<String, Object> nodePre;
    public Map<String, Object> nodePost;
    public List<String> children = new ArrayList<>();

    public String flowId;
    public String nodeAddress;

    public NodeItem(String type, String fid, String na){
        this.type = type;
        this.flowId = fid;
        this.nodeAddress = na;
    }
}
