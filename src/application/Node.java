package application;

/**
 * Represents a node in the graph (a point on the map)
 */
public class Node {
    private final int x;
    private final int y;
    
    /**
     * Constructor
     * @param x X coordinate
     * @param y Y coordinate
     */
    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Get X coordinate
     * @return X coordinate
     */
    public int getX() {
        return x;
    }
    
    /**
     * Get Y coordinate
     * @return Y coordinate
     */
    public int getY() {
        return y;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Node other = (Node) obj;
        return x == other.x && y == other.y;
    }
    
    @Override
    public int hashCode() {
        return 31 * x + y;
    }
    
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
