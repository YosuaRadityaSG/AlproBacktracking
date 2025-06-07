import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

public class MazeGUI extends JFrame {
    // UI Components
    private JPanel controlPanel;
    private MazePanel mazePanel;
    private JButton generateButton;
    private JButton solveButton;
    private JButton stepButton;
    private JButton resetButton;
    private JLabel statusLabel;
    private JSpinner sizeSpinner;
    private JSlider wallDensitySlider;
    private JCheckBox animateSolvingCheckBox;
    private JSlider animationSpeedSlider;
    
    // Maze data
    private int[][] map;
    private int[][] solution;
    private int size = 10; // Default size
    private double wallDensity = 0.3; // Default wall density
    private boolean animateSolving = true;
    private int animationSpeed = 50; // ms delay
    
    // Backtracker reference for step-by-step solving
    private AnimatedBacktracker backtracker;
    private Timer animationTimer;
    
    public MazeGUI() {
        // Set up the JFrame
        setTitle("Maze Backtracking Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create components
        createControlPanel();
        createMazePanel();
        createStatusPanel();
        
        // Generate initial maze
        generateMaze();
        
        // Set frame properties
        setSize(800, 800);
        setMinimumSize(new Dimension(600, 600));
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void createControlPanel() {
        // Main control panel
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        
        // Upper panel with size settings
        JPanel upperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel sizeLabel = new JLabel("Maze Size:");
        SpinnerNumberModel sizeModel = new SpinnerNumberModel(10, 5, 30, 1);
        sizeSpinner = new JSpinner(sizeModel);
        sizeSpinner.setPreferredSize(new Dimension(60, 25));
        
        JLabel densityLabel = new JLabel("Wall Density:");
        wallDensitySlider = new JSlider(10, 70, 30);  // 10% to 70%, default 30%
        wallDensitySlider.setMajorTickSpacing(10);
        wallDensitySlider.setMinorTickSpacing(5);
        wallDensitySlider.setPaintTicks(true);
        wallDensitySlider.setPaintLabels(true);
        wallDensitySlider.setPreferredSize(new Dimension(200, 45));
        
        upperPanel.add(sizeLabel);
        upperPanel.add(sizeSpinner);
        upperPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        upperPanel.add(densityLabel);
        upperPanel.add(wallDensitySlider);
        
        // Lower panel with buttons
        JPanel lowerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        generateButton = new JButton("Generate New Maze");
        solveButton = new JButton("Solve Maze");
        stepButton = new JButton("Step");
        resetButton = new JButton("Reset Solution");
        
        // Animation controls
        JPanel animationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        animateSolvingCheckBox = new JCheckBox("Animate Solving", true);
        JLabel speedLabel = new JLabel("Speed:");
        animationSpeedSlider = new JSlider(5, 200, 50);  // 5ms to 200ms, default 50ms
        animationSpeedSlider.setPreferredSize(new Dimension(150, 30));
        
        animationPanel.add(animateSolvingCheckBox);
        animationPanel.add(speedLabel);
        animationPanel.add(animationSpeedSlider);
        
        lowerPanel.add(generateButton);
        lowerPanel.add(solveButton);
        lowerPanel.add(stepButton);
        lowerPanel.add(resetButton);
        
        // Add action listeners
        generateButton.addActionListener(e -> generateMaze());
        solveButton.addActionListener(e -> solveMaze());
        stepButton.addActionListener(e -> stepSolveMaze());
        resetButton.addActionListener(e -> resetSolution());
        
        stepButton.setEnabled(false);
        resetButton.setEnabled(false);
        
        // Add panels to control panel
        controlPanel.add(upperPanel);
        controlPanel.add(lowerPanel);
        controlPanel.add(animationPanel);
        
        // Add to frame
        add(controlPanel, BorderLayout.NORTH);
    }
    
    private void createMazePanel() {
        // Create maze panel with border
        mazePanel = new MazePanel();
        mazePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        JScrollPane scrollPane = new JScrollPane(mazePanel);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createStatusPanel() {
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        // Add legend
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addLegendItem(legendPanel, Color.WHITE, "Empty");
        addLegendItem(legendPanel, Color.BLACK, "Wall");
        addLegendItem(legendPanel, Color.GREEN, "Start");
        addLegendItem(legendPanel, Color.RED, "End");
        addLegendItem(legendPanel, Color.BLUE, "Path");
        statusPanel.add(legendPanel, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void addLegendItem(JPanel panel, Color color, String text) {
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(15, 15));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        panel.add(colorBox);
        panel.add(new JLabel(text));
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
    }
    
    private void generateMaze() {
        // Get selected size and density
        size = (int) sizeSpinner.getValue();
        wallDensity = wallDensitySlider.getValue() / 100.0;
        
        // Generate the maze
        MapGenerator mapGenerator = new MapGenerator(size, wallDensity);
        map = mapGenerator.generateMap();
        solution = null;
        
        if (backtracker != null) {
            stopAnimation();
        }
        
        // Update the maze panel
        mazePanel.setMaze(map, null);
        statusLabel.setText("New maze generated. Click 'Solve Maze' to find a solution.");
        
        // Update button states
        solveButton.setEnabled(true);
        stepButton.setEnabled(false);
        resetButton.setEnabled(false);
        
        // Repaint
        mazePanel.repaint();
    }
    
    private void solveMaze() {
        if (map == null) {
            statusLabel.setText("Generate a maze first!");
            return;
        }
        
        resetSolution();
        
        // Check if animation is enabled
        boolean animate = animateSolvingCheckBox.isSelected();
        animationSpeed = animationSpeedSlider.getValue();
        
        // Prepare for solving
        statusLabel.setText("Solving maze...");
        backtracker = new AnimatedBacktracker(map, mazePanel);
        
        if (animate) {
            // Disable buttons during animation
            solveButton.setEnabled(false);
            generateButton.setEnabled(false);
            stepButton.setEnabled(false);
            
            // Start animation
            animationTimer = new Timer(animationSpeed, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean stillSolving = backtracker.step();
                    if (!stillSolving) {
                        completeSolving();
                    }
                }
            });
            animationTimer.start();
        } else {
            // Solve immediately without animation
            boolean solved = backtracker.solve();
            solution = backtracker.getSolutionPath();
            mazePanel.setMaze(map, solution);
            
            // Update status
            if (solved) {
                statusLabel.setText("Solution found!");
            } else {
                statusLabel.setText("No solution exists for this maze!");
            }
            
            resetButton.setEnabled(true);
        }
    }
    
    private void stepSolveMaze() {
        if (backtracker == null) {
            backtracker = new AnimatedBacktracker(map, mazePanel);
        }
        
        boolean stillSolving = backtracker.step();
        if (!stillSolving) {
            completeSolving();
        }
    }
    
    private void completeSolving() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        
        solution = backtracker.getSolutionPath();
        boolean solved = backtracker.isSolved();
        
        // Re-enable buttons
        generateButton.setEnabled(true);
        solveButton.setEnabled(true);
        stepButton.setEnabled(false);
        resetButton.setEnabled(true);
        
        // Update status
        if (solved) {
            statusLabel.setText("Solution found!");
        } else {
            statusLabel.setText("No solution exists for this maze!");
        }
    }
    
    private void stopAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        
        // Re-enable buttons
        generateButton.setEnabled(true);
        solveButton.setEnabled(true);
    }
    
    private void resetSolution() {
        if (map == null) return;
        
        // Stop any ongoing animation
        stopAnimation();
        
        // Reset the solution
        solution = null;
        backtracker = null;
        
        // Update the maze panel
        mazePanel.setMaze(map, null);
        statusLabel.setText("Solution reset. Ready to solve.");
        
        // Update button states
        solveButton.setEnabled(true);
        stepButton.setEnabled(false);
        resetButton.setEnabled(false);
        
        // Repaint
        mazePanel.repaint();
    }
    
    public static void main(String[] args) {
        try {
            // Set cross-platform look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new MazeGUI());
    }
}
