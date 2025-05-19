package application;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Main application UI for the Emergency Service Route Finder
 */
public class EmergencyRouteFinderUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private ImageProcessor imageProcessor;
    private BufferedImage displayImage;
    
    // UI Components
    private JPanel imagePanel;
    private JTextArea logTextArea;
    private JButton loadImageButton;
    private JButton clearButton;
    private JButton processImageButton;
    private JButton visualizeGraphButton;
    private JComboBox<String> emergencyTypeComboBox;
    private JRadioButton addEmergencyRadio;
    private JRadioButton addServiceCenterRadio;
    private JRadioButton resolveEmergencyRadio;
    
    // State variables
    private Point serviceCenterLocation;
    private List<Emergency> emergencies;
    private Emergency selectedEmergency;
    
    /**
     * Constructor
     */
    public EmergencyRouteFinderUI() {
        super("Emergency Service Route Finder");
        emergencies = new ArrayList<>();
        
        initUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }
    
    /**
     * Initialize UI components
     */
    private void initUI() {
        // Main layout
        setLayout(new BorderLayout());
        
        // Create image panel
        imagePanel = new JPanel() {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (displayImage != null) {
                    g.drawImage(displayImage, 0, 0, this);
                    
                    // Draw service center
                    if (serviceCenterLocation != null) {
                        g.setColor(Color.BLUE);
                        g.fillOval(serviceCenterLocation.x - 10, serviceCenterLocation.y - 10, 20, 20);
                        g.setColor(Color.WHITE);
                        g.drawString("SC", serviceCenterLocation.x - 7, serviceCenterLocation.y + 5);
                    }
                    
                    // Draw emergencies
                    for (Emergency emergency : emergencies) {
                        g.setColor(emergency.getColor());
                        int size = emergency.isResolved() ? 8 : 15;
                        g.fillOval(emergency.getX() - size/2, emergency.getY() - size/2, size, size);
                        
                        if (!emergency.isResolved()) {
                            g.setColor(Color.WHITE);
                            g.drawString(emergency.getType().toString().substring(0, 1), 
                                         emergency.getX() - 3, emergency.getY() + 4);
                        }
                    }
                    
                    // Highlight selected emergency
                    if (selectedEmergency != null) {
                        g.setColor(Color.CYAN);
                        g.drawOval(selectedEmergency.getX() - 12, selectedEmergency.getY() - 12, 24, 24);
                    }
                }
            }
        };
        
        imagePanel.setPreferredSize(new Dimension(800, 600));
        imagePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        // Add mouse listener to image panel
        imagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleImageClick(e.getPoint());
            }
        });
        
        // Create control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        loadImageButton = new JButton("Load Image");
        loadImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadImage();
            }
        });
        
        processImageButton = new JButton("Process Image");
        processImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processImage();
            }
        });
        processImageButton.setEnabled(false);
        
        visualizeGraphButton = new JButton("Visualize Graph");
        visualizeGraphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                visualizeGraph();
            }
        });
        visualizeGraphButton.setEnabled(false);
        
        clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAll();
            }
        });
        
        buttonsPanel.add(loadImageButton);
        buttonsPanel.add(processImageButton);
        buttonsPanel.add(visualizeGraphButton);
        buttonsPanel.add(clearButton);
        
        // Create interaction mode panel
        JPanel modePanel = new JPanel();
        modePanel.setBorder(BorderFactory.createTitledBorder("Mode"));
        modePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        ButtonGroup modeGroup = new ButtonGroup();
        addServiceCenterRadio = new JRadioButton("Add Service Center", true);
        addEmergencyRadio = new JRadioButton("Add Emergency");
        resolveEmergencyRadio = new JRadioButton("Resolve Emergency");
        
        modeGroup.add(addServiceCenterRadio);
        modeGroup.add(addEmergencyRadio);
        modeGroup.add(resolveEmergencyRadio);
        
        modePanel.add(addServiceCenterRadio);
        modePanel.add(addEmergencyRadio);
        modePanel.add(resolveEmergencyRadio);
        
        // Create emergency type selection
        JPanel emergencyTypePanel = new JPanel();
        emergencyTypePanel.setBorder(BorderFactory.createTitledBorder("Emergency Type"));
        
        emergencyTypeComboBox = new JComboBox<>(
                new String[]{"URGENT (A*)", "STANDARD (Dijkstra)", "ROUTINE (BFS)"});
        emergencyTypePanel.add(emergencyTypeComboBox);
        
        // Add control components to control panel
        JPanel topControlPanel = new JPanel(new BorderLayout());
        topControlPanel.add(buttonsPanel, BorderLayout.NORTH);
        topControlPanel.add(modePanel, BorderLayout.CENTER);
        topControlPanel.add(emergencyTypePanel, BorderLayout.SOUTH);
        
        controlPanel.add(topControlPanel, BorderLayout.NORTH);
        
        // Create log panel
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log"));
        logScrollPane.setPreferredSize(new Dimension(300, 200));
        
        controlPanel.add(logScrollPane, BorderLayout.CENTER);
        
        // Create split pane
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, 
                new JScrollPane(imagePanel), 
                controlPanel);
        splitPane.setDividerLocation(800);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Add status bar
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        JLabel statusLabel = new JLabel("Ready");
        statusPanel.add(statusLabel);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Handle clicks on the image panel
     * @param point The clicked point
     */
    private void handleImageClick(Point point) {
        if (imageProcessor == null) {
            log("Please load and process an image first.");
            return;
        }
        
        int x = point.x;
        int y = point.y;
        
        // Check if this is a valid road pixel
        if (!isValidRoadPixel(x, y)) {
            log("Selected point (" + x + ", " + y + ") is not a valid road location.");
            return;
        }
        
        if (addServiceCenterRadio.isSelected()) {
            // Add service center
            serviceCenterLocation = point;
            log("Service Center placed at (" + x + ", " + y + ")");
            repaintImagePanel();
            
        } else if (addEmergencyRadio.isSelected()) {
            // Add emergency
            String description = JOptionPane.showInputDialog(this, 
                    "Enter emergency description:", 
                    "Add Emergency", 
                    JOptionPane.QUESTION_MESSAGE);
            
            if (description != null && !description.trim().isEmpty()) {
                ImageProcessor.EmergencyType type = getSelectedEmergencyType();
                Emergency emergency = new Emergency(x, y, type, description);
                emergencies.add(emergency);
                log("Added " + emergency);
                repaintImagePanel();
            }
            
        } else if (resolveEmergencyRadio.isSelected()) {
            // Find nearest unresolved emergency
            selectedEmergency = findNearestEmergency(x, y);
            
            if (selectedEmergency != null && !selectedEmergency.isResolved()) {
                if (serviceCenterLocation == null) {
                    log("Please place a service center first.");
                    return;
                }
                
                log("Selected emergency: " + selectedEmergency);
                log("Finding route using " + selectedEmergency.getAlgorithmName() + "...");
                
                // Find path from service center to emergency
                List<Node> path = imageProcessor.findPath(
                        serviceCenterLocation.x, 
                        serviceCenterLocation.y,
                        selectedEmergency.getX(),
                        selectedEmergency.getY(),
                        selectedEmergency.getType());
                
                if (path != null && !path.isEmpty()) {
                    log("Path found with " + path.size() + " nodes.");
                    
                    // Visualize the path
                    displayImage = imageProcessor.visualizePath(path, selectedEmergency.getColor());
                    
                    // Mark emergency as resolved
                    selectedEmergency.setResolved(true);
                    log("Emergency resolved!");
                } else {
                    log("No path found between service center and emergency!");
                }
                
                repaintImagePanel();
            } else {
                log("No unresolved emergency found near this point.");
            }
        }
    }
    
    /**
     * Find the nearest emergency to a given point
     * @param x X coordinate
     * @param y Y coordinate
     * @return The nearest emergency, or null if none exists
     */
    private Emergency findNearestEmergency(int x, int y) {
        Emergency nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Emergency emergency : emergencies) {
            if (!emergency.isResolved()) {
                double distance = Math.sqrt(
                        Math.pow(emergency.getX() - x, 2) + 
                        Math.pow(emergency.getY() - y, 2));
                
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = emergency;
                }
            }
        }
        
        return nearest;
    }
    
    /**
     * Check if a point is a valid road pixel
     * @param x X coordinate
     * @param y Y coordinate
     * @return true if a valid road location
     */
    private boolean isValidRoadPixel(int x, int y) {
        if (imageProcessor == null) {
            return false;
        }
        
        int[][] pixelMap = imageProcessor.getPixelMap();
        if (x < 0 || y < 0 || x >= pixelMap.length || y >= pixelMap[0].length) {
            return false;
        }
        
        return pixelMap[x][y] == 1;
    }
    
    /**
     * Get the currently selected emergency type
     * @return EmergencyType enum value
     */
    private ImageProcessor.EmergencyType getSelectedEmergencyType() {
        int selectedIndex = emergencyTypeComboBox.getSelectedIndex();
        switch (selectedIndex) {
            case 0:
                return ImageProcessor.EmergencyType.URGENT;
            case 1:
                return ImageProcessor.EmergencyType.STANDARD;
            case 2:
                return ImageProcessor.EmergencyType.ROUTINE;
            default:
                return ImageProcessor.EmergencyType.STANDARD;
        }
    }
    
    /**
     * Load an image file
     */
    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Image Map");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "bmp", "gif"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // Load and display the image
                BufferedImage loadedImage = ImageIO.read(selectedFile);
                displayImage = loadedImage;
                
                // Resize imagePanel to match image dimensions
                imagePanel.setPreferredSize(new Dimension(
                        loadedImage.getWidth(), loadedImage.getHeight()));
                imagePanel.revalidate();
                
                // Enable processing
                processImageButton.setEnabled(true);
                
                log("Image loaded: " + selectedFile.getName() + 
                        " (" + loadedImage.getWidth() + "x" + loadedImage.getHeight() + ")");
                
                repaintImagePanel();
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                        "Error loading image: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                log("Error loading image: " + e.getMessage());
            }
        }
    }
    
    /**
     * Process the loaded image
     */
    private void processImage() {
        if (displayImage == null) {
            log("No image loaded.");
            return;
        }
        
        try {
            log("Processing image...");
            imageProcessor = new ImageProcessor(displayImage);
            log("Image processed. Created graph with " + 
                    imageProcessor.getGraph().getNodeCount() + " nodes and " + 
                    imageProcessor.getGraph().getEdgeCount() + " edges.");
            
            // Enable visualization
            visualizeGraphButton.setEnabled(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Error processing image: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            log("Error processing image: " + e.getMessage());
        }
    }
    
    /**
     * Visualize the graph structure
     */
    private void visualizeGraph() {
        if (imageProcessor == null) {
            log("No processed image.");
            return;
        }
        
        log("Visualizing graph structure...");
        displayImage = imageProcessor.visualizeGraph();
        repaintImagePanel();
    }
    
    /**
     * Clear all data and reset the UI
     */
    private void clearAll() {
        displayImage = null;
        imageProcessor = null;
        serviceCenterLocation = null;
        emergencies.clear();
        selectedEmergency = null;
        
        processImageButton.setEnabled(false);
        visualizeGraphButton.setEnabled(false);
        
        logTextArea.setText("");
        log("All data cleared.");
        
        repaintImagePanel();
    }
    
    /**
     * Log a message
     * @param message Message to log
     */
    private void log(String message) {
        logTextArea.append(message + "\n");
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
    }
    
    /**
     * Repaint the image panel
     */
    private void repaintImagePanel() {
        if (imagePanel != null) {
            imagePanel.repaint();
        }
    }
    
    /**
     * Main method
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                EmergencyRouteFinderUI app = new EmergencyRouteFinderUI();
                app.setVisible(true);
            }
        });
    }
}
