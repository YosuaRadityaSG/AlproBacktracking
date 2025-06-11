import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel or custom look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Customize UI components with dark theme
                UIManager.put("Panel.background", new Color(18, 18, 18));
                UIManager.put("Button.background", new Color(75, 95, 175));
                UIManager.put("Button.foreground", Color.black);
                UIManager.put("Label.foreground", new Color(240, 240, 240));
                UIManager.put("TextField.background", new Color(30, 30, 30));
                UIManager.put("TextField.foreground", new Color(240, 240, 240));
                UIManager.put("ScrollPane.background", new Color(18, 18, 18));
                UIManager.put("OptionPane.background", new Color(18, 18, 18));
                UIManager.put("OptionPane.messageForeground", Color.BLACK);
                
                JFrame frame = new JFrame("Dark Mode Maze Solver");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1200, 800);
                frame.add(new Layar());
                frame.setLocationRelativeTo(null); // Center on screen
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
