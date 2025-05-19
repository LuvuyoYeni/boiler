package application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a graph data structure for the map
 */
public class Graph {
    private final Map<String, Node> nodes;
    private final Map<Node, List<Edge>> adjacencyList;
    
    /**
     * Constructor
     */
    public Graph() {
        nodes = new HashMap<>();
        adjacencyList = new HashMap<>();
    }
    
    /**
     * Add a node to the graph
     * @param node Node to add
     */
    public void addNode(Node node) {
        String key = nodeKey(node.getX(), node.getY());
        nodes.put(key, node);
        adjacencyList.putIfAbsent(node, new ArrayList<>());
    }
    
    /**
     * Get a node by coordinates
     * @param x X coordinate
     * @param y Y coordinate
     * @return The node at those coordinates, or null if not found
     */
    public Node getNode(int x, int y) {
        return nodes.get(nodeKey(x, y));
    }
    
    /**
     * Generate a key for the nodes map
     * @param x X coordinate
     * @param y Y coordinate
     * @return String key
     */
    private String nodeKey(int x, int y) {
        return x + "," + y;
    }
    
    /**
     * Add an edge between two nodes
     * @param source Source node
     * @param destination Destination node
     * @param weight Weight of the edge
     */
    public void addEdge(Node source, Node destination, double weight) {
        Edge edge = new Edge(source, destination, weight);
        adjacencyList.get(source).add(edge);
    }
    
    /**
     * Get all edges from a node
     * @param node Source node
     * @return List of edges
     */
    public List<Edge> getEdges(Node node) {
        return adjacencyList.getOrDefault(node, new ArrayList<>());
    }
    
    /**
     * Get all nodes in the graph
     * @return Collection of all nodes
     */
    public Collection<Node> getAllNodes() {
        return nodes.values();
    }
    
    /**
     * Get count of nodes
     * @return Number of nodes
     */
    public int getNodeCount() {
        return nodes.size();
    }
    
    /**
     * Get count of edges
     * @return Number of edges
     */
    public int getEdgeCount() {
        int count = 0;
        for (List<Edge> edges : adjacencyList.values()) {
            count += edges.size();
        }
        return count;
    }
    
    /**
     * Check if a node exists in the graph
     * @param x X coordinate
     * @param y Y coordinate
     * @return true if the node exists
     */
    public boolean nodeExists(int x, int y) {
        return nodes.containsKey(nodeKey(x, y));
    }
    
    /**
     * Find neighboring nodes of a given node
     * @param node The node to find neighbors for
     * @return Set of neighboring nodes
     */
    public Set<Node> getNeighbors(Node node) {
        Set<Node> neighbors = new HashSet<>();
        List<Edge> edges = getEdges(node);
        
        for (Edge edge : edges) {
            neighbors.add(edge.getDestination());
        }
        
        return neighbors;
    }
    
    /**
     * Find the shortest path between two nodes using breadth-first search
     * @param start Starting node
     * @param end End node
     * @return List of nodes in the path, or null if no path exists
     */
    public List<Node> shortestPath(Node start, Node end) {
        if (start == null || end == null) {
            return null;
        }
        
        Map<Node, Node> parentMap = new HashMap<>();
        Set<Node> visited = new HashSet<>();
        List<Node> queue = new ArrayList<>();
        
        visited.add(start);
        queue.add(start);
        
        while (!queue.isEmpty()) {
            Node current = queue.remove(0);
            
            if (current.equals(end)) {
                // Path found, reconstruct it
                return reconstructPath(parentMap, start, end);
            }
            
            for (Edge edge : getEdges(current)) {
                Node neighbor = edge.getDestination();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        
        // No path found
        return null;
    }
    
    /**
     * Reconstruct a path from the parent map
     * @param parentMap Map of child to parent nodes
     * @param start Starting node
     * @param end End node
     * @return List of nodes in the path
     */
    private List<Node> reconstructPath(Map<Node, Node> parentMap, Node start, Node end) {
        List<Node> path = new ArrayList<>();
        Node current = end;
        
        while (current != null && !current.equals(start)) {
            path.add(0, current);
            current = parentMap.get(current);
        }
        
        if (current != null) {
            path.add(0, start);
            return path;
        } else {
            // Path reconstruction failed
            return null;
        }
    }
}
