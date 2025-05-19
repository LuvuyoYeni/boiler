package application;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

public class ImageProcessorDebug extends JFrame {
    private static final long serialVersionUID = 1L;
    private BufferedImage originalImage;
    private BufferedImage processedImage;
    private JLabel imageLabel;
    private JButton loadButton, processButton, toggleViewButton;
    private boolean showingProcessed = false;
    
    public ImageProcessorDebug() {
        setTitle("Image Processor Debug View");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel controlPanel = new JPanel();
        loadButton = new JButton("Load Image");
        processButton = new JButton("Process Image");
        toggleViewButton = new JButton("Toggle View");
        toggleViewButton.setEnabled(false);
        
        controlPanel.add(loadButton);
        controlPanel.add(processButton);
        controlPanel.add(toggleViewButton);
        
        imageLabel = new JLabel();
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        File selectedFile = fileChooser.getSelectedFile();
                        originalImage = ImageIO.read(selectedFile);
                        displayImage(originalImage);
                        processButton.setEnabled(true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error loading image: " + ex.getMessage());
                    }
                }
            }
        });
        
        processButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (originalImage != null) {
                    processedImage = processImage(originalImage);
                    JOptionPane.showMessageDialog(null, "Image processed successfully!");
                    toggleViewButton.setEnabled(true);
                }
            }
        });
        
        toggleViewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showingProcessed = !showingProcessed;
                if (showingProcessed) {
                    displayImage(processedImage);
                    toggleViewButton.setText("Show Original");
                } else {
                    displayImage(originalImage);
                    toggleViewButton.setText("Show Processed");
                }
            }
        });
        
        processButton.setEnabled(false);
    }
    
    private void displayImage(BufferedImage img) {
        if (img != null) {
            ImageIcon icon = new ImageIcon(img);
            imageLabel.setIcon(icon);
            imageLabel.revalidate();
            imageLabel.repaint();
        }
    }
    
    // Process the image to create a binary version (roads/obstacles)
    private BufferedImage processImage(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage processed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // Brightness threshold for determining road vs. obstacle
        int threshold = 128;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = original.getRGB(x, y);
                Color color = new Color(rgb);
                int brightness = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                
                if (brightness > threshold) {
                    // Road (white)
                    processed.setRGB(x, y, Color.WHITE.getRGB());
                } else {
                    // Obstacle (black)
                    processed.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }
        
        return processed;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ImageProcessorDebug debug = new ImageProcessorDebug();
            debug.setVisible(true);
        });
    }
}