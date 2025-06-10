import javax.swing.JFrame;

public class App {
    public static void main(String[] args) throws Exception {
        // Creates the main application window (JFrame)
        // Sets the window title to "Backtracking Maze Solver"
        // Sets the window size to 800x600 pixels
        // Adds an instance of Layar (the main panel) to the window
        // Makes the window visible to the user
        JFrame frame = new JFrame("Backtracking Maze Solver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(new Layar());
        frame.setVisible(true);
    }
}
