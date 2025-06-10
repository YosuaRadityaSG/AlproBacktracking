import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Layar extends JPanel {
    // UI components
    private JTextField sizeField;          // For maze size input
    private JTextArea output;              // Text output for small mazes
    private JScrollPane scrollPane;        // Scrollable view for text output
    private JScrollPane mapScrollPane;     // Scrollable view for backtracking visualization
    private JScrollPane solutionMapScrollPane; // Scrollable view for solution algorithm
    private Map mapPanel;                  // Visual panel for backtracking algorithm
    private Map solutionMapPanel;          // Visual panel for solution algorithm
    private JLabel backtrackingTimerLabel; // Display timer for backtracking
    private JLabel solutionTimerLabel;     // Display timer for solution algorithm
    
    // Timer variables
    private long startTime;                // Start time for performance measurement
    private boolean timerRunning;          // Flag to control timer threads
    private double jinxBlock = 0;          // Time penalty accumulator for JinxBlock

    // Constructor sets up the UI layout and components
    public Layar() {
        // Set up the panel with null layout
        setLayout(null);
        setBackground(new Color(173, 216, 230)); // Light blue background
        
        // Initialize scroll panes as null
        mapScrollPane = null;
        solutionMapScrollPane = null;
        
        // Create and position UI components
        // 1. Label and text field for maze size input
        JLabel label = new JLabel("Map size (1-15): ");
        label.setBounds(10, 0, 100, 40);
        label.setBackground(new Color(173, 216, 230));
        add(label);
        
        sizeField = new JTextField(2);
        sizeField.setBounds(105, 10, 30, 20);
        add(sizeField);
        
        // 2. Button to trigger maze generation and solving
        JButton button = new JButton("Generate and Solve");
        button.setBounds(10, 35, 150, 30);
        add(button);
        
        // 3. Timer labels for algorithm comparison
        backtrackingTimerLabel = new JLabel("");
        backtrackingTimerLabel.setBounds(535, 70, 100, 40);
        add(backtrackingTimerLabel);
        backtrackingTimerLabel.setVisible(false);
        
        solutionTimerLabel = new JLabel("");
        solutionTimerLabel.setBounds(1145, 70, 100, 40);
        solutionTimerLabel.setVisible(false);
        add(solutionTimerLabel);
        
        // 4. Text area for output display
        output = new JTextArea();
        output.setEditable(false);
        output.setMargin(new Insets(0, 10, 0, 0));
        output.setBackground(new Color(173, 216, 230));
        
        // 5. Scroll pane for text output
        scrollPane = new JScrollPane(output);
        scrollPane.setBounds(0, 70, 1285, 1000);
        scrollPane.getViewport().setBackground(new Color(173, 216, 230));
        add(scrollPane);
        
        // Add action listener to button
        button.addActionListener(e -> onGenerate());
    }

    // Handler for maze generation and solving
    private void onGenerate() {
        // Validate user input for maze size
        int size;
        output.setText("");
        try {
            size = Integer.parseInt(sizeField.getText());
            if (size < 1 || size > 15) {
                output.setText("Invalid size. Please enter a number between 1 and 15.\n");
                return;
            }
        } catch (NumberFormatException ex) {
            output.setText("Please enter a valid integer.\n");
            return;
        }
        
        // Begin maze generation
        output.append("Generating map...\n");
        output.append("Map generated with borders:\n");
        
        // Handle maze generation based on size
        switch (size) {
            // For sizes 3-15, use visual comparison
            case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11: case 12: case 13: case 14: case 15:
                // Clean up previous visualization if it exists
                if (mapPanel != null) {
                    mapPanel.clearPath();
                    mapPanel.repaint();
                }
                if (solutionMapPanel != null) {
                    solutionMapPanel.clearPath();
                    solutionMapPanel.repaint();
                }
                if (mapScrollPane != null) {
                    remove(mapScrollPane);
                    mapScrollPane = null;
                }
                if (solutionMapScrollPane != null) {
                    remove(solutionMapScrollPane);
                    solutionMapScrollPane = null;
                }
                
                // Generate two identical mazes with the same seed
                long seed = System.currentTimeMillis();
                
                // Setup left panel for backtracking visualization
                mapPanel = new Map(size, seed);
                int mapSize = size * 40;
                mapPanel.setPreferredSize(new Dimension(mapSize, mapSize));
                mapPanel.setBackground(new Color(173, 216, 230));
                mapScrollPane = new JScrollPane(mapPanel);
                mapScrollPane.setBounds(6, 105, 610, 610);
                add(mapScrollPane);
                
                // Setup right panel for solution algorithm visualization
                solutionMapPanel = new Map(size, seed);
                solutionMapPanel.setPreferredSize(new Dimension(mapSize, mapSize));
                solutionMapPanel.setBackground(new Color(173, 216, 230));
                solutionMapScrollPane = new JScrollPane(solutionMapPanel);
                solutionMapScrollPane.setBounds(615, 105, 610, 610);
                add(solutionMapScrollPane);
                
                // Initialize timer variables
                startTime = System.currentTimeMillis();
                timerRunning = true;
                jinxBlock = 0;
                backtrackingTimerLabel.setText("");
                backtrackingTimerLabel.setVisible(true);
                solutionTimerLabel.setText("");
                solutionTimerLabel.setVisible(false);
                
                // Start backtracking visualization timer
                startTimer();
                
                // Create a new thread for the algorithms to keep UI responsive
                new Thread(() -> {
                    try {
                        // Brief delay before starting
                        Thread.sleep(500);
                        
                        // Start JinxBlock detector to monitor time penalties
                        startJinxBlockDetector();
                        
                        // Run backtracking algorithm on left panel
                        Backtracking backtracking = new Backtracking(mapPanel, mapPanel.getStarImage());
                        boolean backtrackingFound = backtracking.solveWithAnimation();
                        
                        // Stop timer and calculate total time
                        stopTimer();
                        double backtrackingTime = (System.currentTimeMillis() - startTime) / 1000.0 + jinxBlock;
                        
                        // Update UI with backtracking results
                        SwingUtilities.invokeLater(() -> {
                            backtrackingTimerLabel.setText("Timer: " + String.format("%.2f", backtrackingTime) + "s");
                            solutionTimerLabel.setVisible(true);
                        });
                        
                        // Brief delay before starting solution algorithm
                        Thread.sleep(500);
                        
                        // Reset timer for solution algorithm
                        startTime = System.currentTimeMillis();
                        jinxBlock = 0;
                        timerRunning = true;
                        
                        // Create timer thread for solution algorithm
                        Thread timerThread = new Thread(() -> {
                            try {
                                while (timerRunning) {
                                    Thread.sleep(10);
                                    final double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0 + jinxBlock;
                                    
                                    SwingUtilities.invokeLater(() -> {
                                        solutionTimerLabel.setText("Timer: " + String.format("%.2f", elapsedTime) + "s");
                                    });
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                        timerThread.start();
                        
                        // Run solution algorithm on right panel
                        Solution solution = new Solution(solutionMapPanel, solutionMapPanel.getStarImage());
                        boolean solutionFound = solution.solveWithAnimation();
                        
                        // Stop timer and display final time
                        timerRunning = false;
                        double solutionTime = solution.getTime();
                        
                        SwingUtilities.invokeLater(() -> {
                            solutionTimerLabel.setText("Timer: " + String.format("%.2f", solutionTime) + "s");
                        });
                        
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                break;
                
            // For smaller sizes, use text display
            default:
                backtrackingTimerLabel.setVisible(false);
                if (scrollPane.getParent() == null) {
                    add(scrollPane);
                }
                
                // Generate simple text-based maze
                MapGenerator mapGenerator = new MapGenerator(size);
                int[][] map = mapGenerator.generateMap();

                // Display the initial maze
                output.append("□ = Empty, ■ = Wall, S = Start, E = End\n");
                output.append(printMap(map));
                
                // Run backtracking algorithm
                output.append("Applying backtracking...\n");
                startTime = System.currentTimeMillis();
                Backtracker backtracker = new Backtracker(map);
                boolean solutionFound = backtracker.solve();
                double solveTime = (System.currentTimeMillis() - startTime) / 1000.0;

                // Display solution or failure message
                if (solutionFound) {
                    output.append("□ = Empty, ■ = Wall, S = Start, E = End, × = Path\n");
                    output.append(printMap(backtracker.getSolutionPath()));
                } else {
                    output.append("No solution exists.\n");
                    output.append(printMap(map));
                }
                break;
        }
    }
    
    // Monitors for JinxBlock encounters and adds time penalties
    private void startJinxBlockDetector() {
        new Thread(() -> {
            try {
                int lastPathSize = 0;
                
                // While the timer is running, check for JinxBlock encounters
                while (timerRunning) {
                    Thread.sleep(100);
                    if (mapPanel != null) {
                        List<int[]> path = mapPanel.getStarPathPositions();
                        
                        // Check if new position has been added to path
                        if (path.size() > lastPathSize && path.size() > 0) {
                            int[] pos = path.get(path.size() - 1);
                            int[][] internalMap = mapPanel.getInternalMap();
                            
                            // Check if new position is a JinxBlock
                            if (pos[0] >= 0 && pos[0] < internalMap.length && 
                                pos[1] >= 0 && pos[1] < internalMap[0].length &&
                                internalMap[pos[0]][pos[1]] == 4) {
                                // Add 4-second penalty and pause for 2 seconds
                                jinxBlock += 4.0;
                                Thread.sleep(2000);
                            }
                            lastPathSize = path.size();
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    // Stops the timer
    private void stopTimer() {
        timerRunning = false;
    }

    // Starts a timer thread to update timer label
    private void startTimer() {
        new Thread(() -> {
            while (timerRunning) {
                try {
                    Thread.sleep(10);
                    final double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0 + jinxBlock;
                    
                    SwingUtilities.invokeLater(() -> {
                        backtrackingTimerLabel.setText("Timer: " + String.format("%.2f", elapsedTime) + "s");
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Converts maze array to pretty-printed text representation
    private String printMap(int[][] map) {
        String[] symbols = {"□ ", "■ ", "S ", "E ", "× "};
        StringBuilder stringBuilder = new StringBuilder();

        // For each cell, append appropriate symbol
        for (int[] row : map) {
            for (int value : row) {
                if (value >= 0 && value < symbols.length) {
                    stringBuilder.append(symbols[value]);
                } else {
                    stringBuilder.append(value).append(" ");
                }
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}

// 1. Creates UI components (text fields, buttons, panels, labels)
// 2. Handles user input for maze size (1-15)
// 3. Generates maze based on user input
// 4. Displays two maze panels side by side:
//    - Left panel: Backtracking algorithm visualization
//    - Right panel: Solution algorithm visualization
// 5. Tracks and displays solving time for both algorithms
// 6. Manages special game elements like JinxBlock that adds time penalties
// 7. Coordinates the maze solving process on separate threads
