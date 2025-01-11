package scraper.plugins.core.debugger.Graph;

import scraper.api.FlowMap;
import scraper.api.Node;
import scraper.api.NodeContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The whole Process execution will be saved as an "adjacent List"
 * That mean each Node know his children (have a list of the ids of his children)
 * And each Node has a State
 */
public class Graph {
    //List of all Node in the Graph
    public final Map<String, NodeItem> map = new ConcurrentHashMap<>();

    //List of ids, which give the order of the execution of each Node
    public final List<String> order = new ArrayList<>();

    public NodeItem get(String key){
        return map.get(key);
    }

    public void put(String key, NodeItem value){
        map.put(key, value);
    }

    public void putOrder(String key){
        order.add(key);
    }

    /**
     * Put a new Node in the Graph
     * @param n
     * @param o
     */
    public void expand(NodeContainer<? extends Node> n, FlowMap o){
        String nodeType = (String) n.getKeySpec("type").get();
        String flowId = o.getId().toString();

        NodeItem node = new NodeItem("forward", o.getId().toString(), n.getAddress().toString());
        map.put(flowId, node);
    }
}
