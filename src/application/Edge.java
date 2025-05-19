package application;

/**
 * Represents an edge in the graph (a connection between two points on the map)
 */
public class Edge {
    private final Node source;
    private final Node destination;
    private final double weight;
    
    /**
     * Constructor
     * @param source Source node
     * @param destination Destination node
     * @param weight Weight of the edge (distance/cost)
     */
    public Edge(Node source, Node destination, double weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }
    
    /**
     * Get source node
     * @return Source node
     */
    public Node getSource() {
        return source;
    }
    
    /**
     * Get destination node
     * @return Destination node
     */
    public Node getDestination() {
        return destination;
    }
    
    /**
     * Get weight of the edge
     * @return Weight
     */
    public double getWeight() {
        return weight;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Edge other = (Edge) obj;
        return source.equals(other.source) && 
               destination.equals(other.destination) && 
               Double.compare(weight, other.weight) == 0;
    }
    
    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + destination.hashCode();
        result = 31 * result + (int) (Double.doubleToLongBits(weight) ^ (Double.doubleToLongBits(weight) >>> 32));
        return result;
    }
    
    @Override
    public String toString() {
        return source + " -> " + destination + " (" + weight + ")";
    }
}
