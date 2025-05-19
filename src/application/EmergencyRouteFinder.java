package application;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application class for the Emergency Route Finder
 */
public class EmergencyRouteFinder {
    /**
     * Main method
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }
        
        // Launch the application
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                EmergencyRouteFinderUI app = new EmergencyRouteFinderUI();
                app.setVisible(true);
            }
        });
    }
}
