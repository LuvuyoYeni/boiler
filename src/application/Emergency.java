package application;

import java.awt.Color;

/**
 * Represents an emergency event on the map
 */
public class Emergency {
    private final int x;
    private final int y;
    private final ImageProcessor.EmergencyType type;
    private final String description;
    private boolean resolved;
    
    /**
     * Constructor
     * @param x X coordinate
     * @param y Y coordinate
     * @param type Type of emergency
     * @param description Description of the emergency
     */
    public Emergency(int x, int y, ImageProcessor.EmergencyType type, String description) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.description = description;
        this.resolved = false;
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
    
    /**
     * Get emergency type
     * @return Emergency type
     */
    public ImageProcessor.EmergencyType getType() {
        return type;
    }
    
    /**
     * Get description
     * @return Description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if emergency is resolved
     * @return true if resolved
     */
    public boolean isResolved() {
        return resolved;
    }
    
    /**
     * Set emergency as resolved
     * @param resolved true to mark as resolved
     */
    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
    
    /**
     * Get color associated with this emergency type
     * @return Color object
     */
    public Color getColor() {
        switch (type) {
            case URGENT:
                return Color.RED;
            case STANDARD:
                return Color.ORANGE;
            case ROUTINE:
                return Color.YELLOW;
            default:
                return Color.GRAY;
        }
    }
    
    /**
     * Get algorithm name for this emergency type
     * @return String name of algorithm
     */
    public String getAlgorithmName() {
        switch (type) {
            case URGENT:
                return "A* Algorithm";
            case STANDARD:
                return "Dijkstra's Algorithm";
            case ROUTINE:
                return "BFS Algorithm";
            default:
                return "Unknown Algorithm";
        }
    }
    
    @Override
    public String toString() {
        String status = resolved ? "RESOLVED" : "ACTIVE";
        return String.format("%s Emergency at (%d, %d): %s [%s]", 
                type, x, y, description, status);
    }
}
