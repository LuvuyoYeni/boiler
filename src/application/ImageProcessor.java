package application;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * Processes images to create graph representations for path finding in emergency services routing.
 * Implements different path finding algorithms for emergency response scenarios.
 */
public class ImageProcessor {
    
    private BufferedImage originalImage;
    private Graph graph;
    private int[][] pixelMap;
    private static final int ROAD_COLOR_THRESHOLD = 200; // Threshold for detecting roads (light colors)
    private static final int OBSTACLE_COLOR = 0; // Black represents obstacles
    
    // Emergency types
    public enum EmergencyType {
        URGENT,    // Critical emergency - use A* algorithm
        STANDARD,  // Regular emergency - use Dijkstra's algorithm
        ROUTINE    // Non-critical - use BFS
    }
    
    /**
     * Constructor that loads an image from a file
     * @param imagePath Path to the image file
     * @throws IOException If the image cannot be loaded
     */
    public ImageProcessor(String imagePath) throws IOException {
        loadImage(imagePath);
        processImage();
    }
    
    /**
     * Constructor that uses an existing BufferedImage
     * @param image BufferedImage to process
     */
    public ImageProcessor(BufferedImage image) {
        this.originalImage = image;
        processImage();
    }
    
    /**
     * Loads an image from a file path
     * @param imagePath Path to the image file
     * @throws IOException If the image cannot be loaded
     */
    public void loadImage(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        originalImage = ImageIO.read(imageFile);
    }
    
    /**
     * Process the image to create a graph representation
     */
    private void processImage() {
        if (originalImage == null) {
            throw new IllegalStateException("No image loaded");
        }
        
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        pixelMap = new int[width][height];
        
        // Create a pixel map where:
        // - 0 represents obstacles (buildings, etc.)
        // - 1 represents passable roads
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color pixelColor = new Color(originalImage.getRGB(x, y));
                int brightness = (pixelColor.getRed() + pixelColor.getGreen() + pixelColor.getBlue()) / 3;
                
                if (brightness > ROAD_COLOR_THRESHOLD) {
                    pixelMap[x][y] = 1; // Road
                } else {
                    pixelMap[x][y] = 0; // Obstacle
                }
            }
        }
        
        // Create a graph from the pixel map
        createGraph();
    }
    
    /**
     * Creates a graph representation from the pixel map
     */
    private void createGraph() {
        graph = new Graph();
        int width = pixelMap.length;
        int height = pixelMap[0].length;
        
        // Add nodes for each road pixel
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (pixelMap[x][y] == 1) {
                    Node node = new Node(x, y);
                    graph.addNode(node);
                }
            }
        }
        
        // Connect adjacent road pixels with edges
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (pixelMap[x][y] == 1) {
                    Node current = graph.getNode(x, y);
                    
                    // Check 8 adjacent pixels
                    int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
                    int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};
                    
                    for (int i = 0; i < 8; i++) {
                        int nx = x + dx[i];
                        int ny = y + dy[i];
                        
                        // Ensure we're within bounds
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height && pixelMap[nx][ny] == 1) {
                            Node neighbor = graph.getNode(nx, ny);
                            
                            // Calculate distance (1 for orthogonal, sqrt(2) for diagonal)
                            double weight = (dx[i] == 0 || dy[i] == 0) ? 1.0 : Math.sqrt(2);
                            graph.addEdge(current, neighbor, weight);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Find a path using a specific algorithm based on emergency type
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param targetX Target X coordinate
     * @param targetY Target Y coordinate
     * @param type Type of emergency determining algorithm choice
     * @return List of nodes representing the path
     */
    public List<Node> findPath(int startX, int startY, int targetX, int targetY, EmergencyType type) {
        // Ensure the coordinates are valid road pixels
        if (!isValidRoadPixel(startX, startY) || !isValidRoadPixel(targetX, targetY)) {
            return null;
        }
        
        Node start = graph.getNode(startX, startY);
        Node target = graph.getNode(targetX, targetY);
        
        switch (type) {
            case URGENT:
                return findPathAStar(start, target);
            case STANDARD:
                return findPathDijkstra(start, target);
            case ROUTINE:
                return findPathBFS(start, target);
            default:
                return findPathDijkstra(start, target); // Default to Dijkstra
        }
    }
    
    /**
     * Check if coordinates represent a valid road pixel
     * @param x X coordinate
     * @param y Y coordinate
     * @return true if valid road pixel
     */
    private boolean isValidRoadPixel(int x, int y) {
        if (x < 0 || x >= pixelMap.length || y < 0 || y >= pixelMap[0].length) {
            return false;
        }
        return pixelMap[x][y] == 1;
    }
    
    /**
     * Breadth-First Search algorithm for path finding (good for routine calls)
     * @param start Starting node
     * @param target Target node
     * @return List of nodes representing the path
     */
    private List<Node> findPathBFS(Node start, Node target) {
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();
        Map<Node, Node> predecessors = new HashMap<>();
        
        queue.add(start);
        visited.add(start);
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            
            if (current.equals(target)) {
                // Path found, reconstruct it
                return reconstructPath(predecessors, target);
            }
            
            for (Edge edge : graph.getEdges(current)) {
                Node neighbor = edge.getDestination();
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                    visited.add(neighbor);
                    predecessors.put(neighbor, current);
                }
            }
        }
        
        // No path found
        return null;
    }
    
    /**
     * Dijkstra's algorithm for path finding (good for standard emergencies)
     * @param start Starting node
     * @param target Target node
     * @return List of nodes representing the path
     */
    private List<Node> findPathDijkstra(Node start, Node target) {
        Map<Node, Double> distance = new HashMap<>();
        Map<Node, Node> predecessors = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(distance::get));
        Set<Node> settled = new HashSet<>();
        
        // Initialize distances
        for (Node node : graph.getAllNodes()) {
            distance.put(node, Double.POSITIVE_INFINITY);
        }
        distance.put(start, 0.0);
        queue.add(start);
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            
            if (current.equals(target)) {
                // Path found, reconstruct it
                return reconstructPath(predecessors, target);
            }
            
            if (settled.contains(current)) {
                continue;
            }
            
            settled.add(current);
            
            for (Edge edge : graph.getEdges(current)) {
                Node neighbor = edge.getDestination();
                if (!settled.contains(neighbor)) {
                    double newDistance = distance.get(current) + edge.getWeight();
                    
                    if (newDistance < distance.get(neighbor)) {
                        distance.put(neighbor, newDistance);
                        predecessors.put(neighbor, current);
                        
                        // Re-add to queue with updated priority
                        queue.remove(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
        
        // No path found
        return null;
    }
    
    /**
     * A* algorithm for path finding (optimal for urgent emergencies)
     * @param start Starting node
     * @param target Target node
     * @return List of nodes representing the path
     */
    private List<Node> findPathAStar(Node start, Node target) {
        Map<Node, Double> gScore = new HashMap<>(); // Cost from start to current node
        Map<Node, Double> fScore = new HashMap<>(); // Estimated total cost from start to goal through current node
        Map<Node, Node> predecessors = new HashMap<>();
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(fScore::get));
        Set<Node> closedSet = new HashSet<>();
        
        // Initialize scores
        for (Node node : graph.getAllNodes()) {
            gScore.put(node, Double.POSITIVE_INFINITY);
            fScore.put(node, Double.POSITIVE_INFINITY);
        }
        
        gScore.put(start, 0.0);
        fScore.put(start, heuristic(start, target));
        openSet.add(start);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            
            if (current.equals(target)) {
                // Path found, reconstruct it
                return reconstructPath(predecessors, target);
            }
            
            closedSet.add(current);
            
            for (Edge edge : graph.getEdges(current)) {
                Node neighbor = edge.getDestination();
                
                if (closedSet.contains(neighbor)) {
                    continue;
                }
                
                double tentativeGScore = gScore.get(current) + edge.getWeight();
                
                if (tentativeGScore < gScore.get(neighbor)) {
                    predecessors.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, gScore.get(neighbor) + heuristic(neighbor, target));
                    
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }
        
        // No path found
        return null;
    }
    
    /**
     * Heuristic function for A* algorithm (Euclidean distance)
     * @param a First node
     * @param b Second node
     * @return Euclidean distance between nodes
     */
    private double heuristic(Node a, Node b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    }
    
    /**
     * Reconstruct the path from the predecessors map
     * @param predecessors Map of node predecessors
     * @param target Target node
     * @return List of nodes representing the path
     */
    private List<Node> reconstructPath(Map<Node, Node> predecessors, Node target) {
        List<Node> path = new ArrayList<>();
        Node current = target;
        
        while (current != null) {
            path.add(0, current);
            current = predecessors.get(current);
        }
        
        return path;
    }
    
    /**
     * Generate an image with the found path visualized
     * @param path List of nodes in the path
     * @param pathColor Color to use for visualizing the path
     * @return BufferedImage with the path visualization
     */
    public BufferedImage visualizePath(List<Node> path, Color pathColor) {
        if (path == null || path.isEmpty()) {
            return originalImage;
        }
        
        // Create a copy of the original image
        BufferedImage resultImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2d = resultImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.setColor(pathColor);
        
        // Draw the path
        for (Node node : path) {
            resultImage.setRGB(node.getX(), node.getY(), pathColor.getRGB());
            
            // Make the path thicker for better visibility
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int x = node.getX() + dx;
                    int y = node.getY() + dy;
                    
                    if (x >= 0 && x < resultImage.getWidth() && y >= 0 && y < resultImage.getHeight()) {
                        resultImage.setRGB(x, y, pathColor.getRGB());
                    }
                }
            }
        }
        
        // Draw start point (green)
        g2d.setColor(Color.GREEN);
        Node start = path.get(0);
        g2d.fillOval(start.getX() - 5, start.getY() - 5, 10, 10);
        
        // Draw end point (red)
        g2d.setColor(Color.RED);
        Node end = path.get(path.size() - 1);
        g2d.fillOval(end.getX() - 5, end.getY() - 5, 10, 10);
        
        g2d.dispose();
        return resultImage;
    }
    
    /**
     * Get the original image
     * @return The original image
     */
    public BufferedImage getOriginalImage() {
        return originalImage;
    }
    
    /**
     * Get the pixel map
     * @return 2D array representing the pixel map
     */
    public int[][] getPixelMap() {
        return pixelMap;
    }
    
    /**
     * Get the graph
     * @return The graph representation
     */
    public Graph getGraph() {
        return graph;
    }
    
    /**
     * Generate a visualization of the graph structure
     * @return BufferedImage showing the graph nodes and edges
     */
    public BufferedImage visualizeGraph() {
        BufferedImage resultImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2d = resultImage.createGraphics();
        
        // Fill with white background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, resultImage.getWidth(), resultImage.getHeight());
        
        // Draw edges
        g2d.setColor(Color.LIGHT_GRAY);
        for (Node node : graph.getAllNodes()) {
            int x1 = node.getX();
            int y1 = node.getY();
            
            for (Edge edge : graph.getEdges(node)) {
                Node neighbor = edge.getDestination();
                int x2 = neighbor.getX();
                int y2 = neighbor.getY();
                
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
        
        // Draw nodes
        g2d.setColor(Color.BLUE);
        for (Node node : graph.getAllNodes()) {
            g2d.fillRect(node.getX() - 1, node.getY() - 1, 3, 3);
        }
        
        g2d.dispose();
        return resultImage;
    }
    
    /**
     * Create a visualization of available road pixels
     * @return BufferedImage showing road pixels
     */
    public BufferedImage visualizeRoads() {
        BufferedImage resultImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        
        for (int x = 0; x < pixelMap.length; x++) {
            for (int y = 0; y < pixelMap[0].length; y++) {
                if (pixelMap[x][y] == 1) {
                    resultImage.setRGB(x, y, Color.WHITE.getRGB());
                } else {
                    resultImage.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }
        
        return resultImage;
    }
}