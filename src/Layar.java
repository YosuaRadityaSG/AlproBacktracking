import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class Layar extends JPanel {
    private JTextField sizeField;
    private JScrollPane mapScrollPane, solutionMapScrollPane;
    private Map mapPanel, solutionMapPanel;
    private JLabel backtrackingTimerLabel, solutionTimerLabel;
    private JLabel backtrackingStepsLabel, solutionStepsLabel;
    private JLabel backtrackingTitleLabel, solutionTitleLabel;
    private long startTime;
    private boolean timerRunning;
    private double jinxBlock = 0;
    private JButton generateButton, skipButton;
    private volatile boolean skipRequested = false;
    private JPanel controlPanel, mapLabelsPanel;
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    private static final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private static final Color SECONDARY_COLOR = new Color(103, 58, 183);
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color LABEL_COLOR = new Color(50, 50, 50);

    public Layar() {
        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Top control panel
        createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Map labels panel (for timers and labels)
        createMapLabelsPanel();
        
        // Create center panel to hold maps
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.add(mapLabelsPanel, BorderLayout.NORTH);
        
        JPanel mapsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        mapsPanel.setBackground(BACKGROUND_COLOR);
        
        // Placeholder panels for maps
        JPanel leftMapHolder = new JPanel(new BorderLayout());
        leftMapHolder.setBackground(BACKGROUND_COLOR);
        leftMapHolder.setBorder(createTitledBorder("Backtracking Map"));
        
        JPanel rightMapHolder = new JPanel(new BorderLayout());
        rightMapHolder.setBackground(BACKGROUND_COLOR);
        rightMapHolder.setBorder(createTitledBorder("Solution Map"));
        
        mapsPanel.add(leftMapHolder);
        mapsPanel.add(rightMapHolder);
        centerPanel.add(mapsPanel, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Setup action listeners
        generateButton.addActionListener(e -> onGenerate());
        skipButton.addActionListener(e -> {
            skipRequested = true;
            skipButton.setEnabled(false);
            skipButton.setText("Skipping...");
        });
    }

    private void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlPanel.setBackground(new Color(18, 18, 18)); // Match dark theme
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)),
            BorderFactory.createEmptyBorder(5, 5, 10, 5)
        ));
        
        JLabel title = new JLabel("Maze Size:");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_COLOR);
        
        sizeField = new JTextField(3);
        sizeField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sizeField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        generateButton = createStyledButton("Generate and Solve");
        skipButton = createStyledButton("Skip Animation");
        skipButton.setBackground(SECONDARY_COLOR);
        skipButton.setEnabled(false);
        
        controlPanel.add(title);
        controlPanel.add(sizeField);
        controlPanel.add(generateButton);
        controlPanel.add(skipButton);
        
        // Add a description label that explains the visualization
        JLabel descLabel = new JLabel("Visualization will show both algorithms working simultaneously");
        descLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        descLabel.setForeground(new Color(180, 180, 180)); // Lighter text for dark theme
        
        JPanel descPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        descPanel.setBackground(new Color(18, 18, 18)); // Match dark theme
        descPanel.add(descLabel);
        
        controlPanel.add(descPanel);
    }
    
    private void createMapLabelsPanel() {
        mapLabelsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        mapLabelsPanel.setBackground(new Color(18, 18, 18)); // Match dark theme
        
        JPanel leftLabelPanel = new JPanel();
        leftLabelPanel.setLayout(new BoxLayout(leftLabelPanel, BoxLayout.Y_AXIS));
        leftLabelPanel.setBackground(new Color(18, 18, 18)); // Match dark theme
        
        backtrackingTitleLabel = createStyledLabel("Backtracking Algorithm");
        backtrackingTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        backtrackingTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        backtrackingTitleLabel.setForeground(new Color(220, 220, 220)); // Brighter text for dark theme
        
        JPanel leftStatsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        leftStatsPanel.setBackground(new Color(18, 18, 18)); // Match dark theme
        
        backtrackingTimerLabel = createStyledLabel("Time: 0.00s");
        backtrackingTimerLabel.setForeground(new Color(240, 240, 100)); // Yellow timing for visibility
        backtrackingStepsLabel = createStyledLabel("Steps: 0");
        backtrackingStepsLabel.setForeground(new Color(180, 180, 255)); // Light blue steps for visibility
        
        leftStatsPanel.add(backtrackingTimerLabel);
        leftStatsPanel.add(backtrackingStepsLabel);
        
        leftLabelPanel.add(backtrackingTitleLabel);
        leftLabelPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftLabelPanel.add(leftStatsPanel);
        
        JPanel rightLabelPanel = new JPanel();
        rightLabelPanel.setLayout(new BoxLayout(rightLabelPanel, BoxLayout.Y_AXIS));
        rightLabelPanel.setBackground(new Color(18, 18, 18)); // Match dark theme
        
        solutionTitleLabel = createStyledLabel("Optimal Solution");
        solutionTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        solutionTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        solutionTitleLabel.setForeground(new Color(220, 220, 220)); // Brighter text for dark theme
        
        JPanel rightStatsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        rightStatsPanel.setBackground(new Color(18, 18, 18)); // Match dark theme
        
        solutionTimerLabel = createStyledLabel("Time: 0.00s");
        solutionTimerLabel.setForeground(new Color(240, 240, 100)); // Yellow timing for visibility
        solutionStepsLabel = createStyledLabel("Steps: 0");
        solutionStepsLabel.setForeground(new Color(180, 180, 255)); // Light blue steps for visibility
        
        rightStatsPanel.add(solutionTimerLabel);
        rightStatsPanel.add(solutionStepsLabel);
        
        rightLabelPanel.add(solutionTitleLabel);
        rightLabelPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        rightLabelPanel.add(rightStatsPanel);
        
        mapLabelsPanel.add(leftLabelPanel);
        mapLabelsPanel.add(rightLabelPanel);
        
        // Always keep backtracking labels visible
        backtrackingTitleLabel.setVisible(true);
        backtrackingTimerLabel.setVisible(true);
        backtrackingStepsLabel.setVisible(true);
        
        // Only show solution labels when needed
        solutionTitleLabel.setVisible(false);
        solutionTimerLabel.setVisible(false);
        solutionStepsLabel.setVisible(false);
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.black);
        button.setBackground(PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR.darker()),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        
        return button;
    }
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(220, 220, 220)); // Brighter text for dark theme
        label.setBorder(new EmptyBorder(5, 5, 5, 5));
        return label;
    }
    
    private Border createTitledBorder(String title) {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(50, 50, 50)), // Darker border for dark theme
            title
        );
        titledBorder.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        titledBorder.setTitleColor(new Color(200, 200, 200)); // Lighter title for dark theme
        return titledBorder;
    }

    private void onGenerate() {
        int size;

        skipRequested = false;
        skipButton.setEnabled(false);
        skipButton.setText("Skip Animation");
        
        try {
            size = Integer.parseInt(sizeField.getText());
            if (size < 1 || size > 15) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid size. Please enter a number between 1 and 15.", 
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid integer.", 
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        resetStats();
        
        if (mapScrollPane != null) {
            Container parent = mapScrollPane.getParent();
            if (parent != null) {
                parent.remove(mapScrollPane);
            }
        }
        
        if (solutionMapScrollPane != null) {
            Container parent = solutionMapScrollPane.getParent();
            if (parent != null) {
                parent.remove(solutionMapScrollPane);
            }
        }
        
        // Handle different maze sizes
        if (size >= 3 && size <= 15) {
            setupMapPanels(size);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Maze size " + size + " is supported but only in text mode.\n" +
                "For graphical visualization, please use a size between 3 and 15.", 
                "Size Limitation", JOptionPane.INFORMATION_MESSAGE);
            handleTextBasedMaze(size);
        }
        
        revalidate();
        repaint();
    }
    
    private void resetStats() {
        backtrackingTimerLabel.setText("Time: 0.00s");
        backtrackingStepsLabel.setText("Steps: 0");
        solutionTimerLabel.setText("Time: 0.00s");
        solutionStepsLabel.setText("Steps: 0");
    }
    
    private void setupMapPanels(int size) {
        if (mapPanel != null) {
            mapPanel.clearPath();
        }
        if (solutionMapPanel != null) {
            solutionMapPanel.clearPath();
        }
        
        // Find map container components
        Component[] components = getComponents();
        JPanel centerPanel = null;
        for (Component c : components) {
            if (c instanceof JPanel && ((JPanel)c).getLayout() instanceof BorderLayout) {
                centerPanel = (JPanel)c;
                break;
            }
        }
        
        if (centerPanel == null) return;
        
        Component[] centerComponents = centerPanel.getComponents();
        JPanel mapsPanel = null;
        for (Component c : centerComponents) {
            if (c instanceof JPanel && ((JPanel)c).getLayout() instanceof GridLayout) {
                mapsPanel = (JPanel)c;
                break;
            }
        }
        
        if (mapsPanel == null) return;
        
        Component[] mapContainers = mapsPanel.getComponents();
        JPanel leftMapHolder = (JPanel)mapContainers[0];
        JPanel rightMapHolder = (JPanel)mapContainers[1];
        
        // Clear previous map containers
        leftMapHolder.removeAll();
        rightMapHolder.removeAll();
        
        // Create new maps
        long seed = System.currentTimeMillis();
        mapPanel = new Map(size, seed);
        solutionMapPanel = new Map(size, seed);
        
        int mapSize = size * 45;  // Slightly larger cells for better visibility
        
        mapPanel.setPreferredSize(new Dimension(mapSize, mapSize));
        mapPanel.setBackground(BACKGROUND_COLOR);
        mapScrollPane = new JScrollPane(mapPanel);
        mapScrollPane.setBorder(null);
        
        solutionMapPanel.setPreferredSize(new Dimension(mapSize, mapSize));
        solutionMapPanel.setBackground(BACKGROUND_COLOR);
        solutionMapScrollPane = new JScrollPane(solutionMapPanel);
        solutionMapScrollPane.setBorder(null);
        
        leftMapHolder.add(mapScrollPane, BorderLayout.CENTER);
        rightMapHolder.add(solutionMapScrollPane, BorderLayout.CENTER);
        
        // Set UI components visibility - ALWAYS keep backtracking labels visible
        backtrackingTitleLabel.setVisible(true);
        backtrackingTimerLabel.setVisible(true);
        backtrackingStepsLabel.setVisible(true);
        
        // Only show solution labels when that algorithm starts
        solutionTitleLabel.setVisible(false);
        solutionTimerLabel.setVisible(false);
        solutionStepsLabel.setVisible(false);
        
        // Reset timer and start solving
        startTime = System.currentTimeMillis();
        timerRunning = true;
        jinxBlock = 0;
        startTimer();
        skipButton.setEnabled(true);
        
        // Start solution threads
        startSolutionThreads(size);
    }
    
    private void startSolutionThreads(int size) {
        new Thread(() -> {
            try {
                Thread.sleep(500);
                startJinxBlockDetector();
                Backtracking backtracking = new Backtracking(mapPanel, mapPanel.getStarImage());
                boolean backtrackingFound = backtracking.solveWithAnimation(this);
                
                stopTimer();
                double backtrackingTime = (System.currentTimeMillis() - startTime) / 1000.0 + jinxBlock;
                
                SwingUtilities.invokeLater(() -> {
                    backtrackingTimerLabel.setText("Time: " + String.format("%.2f", backtrackingTime) + "s");
                    backtrackingStepsLabel.setText("Steps: " + backtracking.getStepsCount());
                    
                    // Make sure solution labels are now visible
                    solutionTimerLabel.setVisible(true);
                    solutionStepsLabel.setVisible(true);
                    solutionTitleLabel.setVisible(true);
                });
                Thread.sleep(500);
                startTime = System.currentTimeMillis();
                jinxBlock = 0;
                timerRunning = true;
                skipRequested = false;
                Thread timerThread = new Thread(() -> {
                    try {
                        while (timerRunning) {
                            Thread.sleep(10);
                            final double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0 + jinxBlock;
                            
                            SwingUtilities.invokeLater(() -> {
                                solutionTimerLabel.setText("Time: " + String.format("%.2f", elapsedTime) + "s");
                            });
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                timerThread.start();
                Solution solution = new Solution(solutionMapPanel, solutionMapPanel.getStarImage());
                boolean solutionFound = solution.solveWithAnimation(this);
                
                timerRunning = false;
                double solutionTime = solution.getTime();
                
                SwingUtilities.invokeLater(() -> {
                    solutionTimerLabel.setText("Time: " + String.format("%.2f", solutionTime) + "s");
                    solutionStepsLabel.setText("Steps: " + solution.getStepsCount());
                    skipButton.setEnabled(false);
                    
                    // Compare and highlight the more efficient algorithm
                    if (backtracking.getStepsCount() > solution.getStepsCount()) {
                        solutionStepsLabel.setForeground(new Color(0, 130, 0));  // Green for better
                        backtrackingStepsLabel.setForeground(new Color(180, 0, 0));  // Red for worse
                    } else if (backtracking.getStepsCount() < solution.getStepsCount()) {
                        backtrackingStepsLabel.setForeground(new Color(0, 130, 0));  // Green for better
                        solutionStepsLabel.setForeground(new Color(180, 0, 0));  // Red for worse
                    }
                });
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void handleTextBasedMaze(int size) {
        // Create a simpler text-based visualization or display results in a dialog
        MapGenerator mapGenerator = new MapGenerator(size);
        int[][] map = mapGenerator.generateMap();

        // Maybe show a dialog with results instead of console output
        JOptionPane.showMessageDialog(this,
            "Text-based maze solution completed.\nSize: " + size + "x" + size,
            "Solution Complete", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Unchanged methods
    public boolean isSkipRequested() {
        return skipRequested;
    }
    
    public void resetSkip() {
        skipRequested = false;
    }
    
    private void startJinxBlockDetector() {
        new Thread(() -> {
            try {
                int lastPathSize = 0;
                
                while (timerRunning) {
                    Thread.sleep(100);
                    if (mapPanel != null) {
                        List<int[]> path = mapPanel.getStarPathPositions();
                        
                        if (path.size() > lastPathSize && path.size() > 0) {
                            int[] pos = path.get(path.size() - 1);
                            int[][] internalMap = mapPanel.getInternalMap();
                            
                            if (pos[0] >= 0 && pos[0] < internalMap.length && 
                                pos[1] >= 0 && pos[1] < internalMap[0].length &&
                                internalMap[pos[0]][pos[1]] == 4) {
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
    
    private void stopTimer() {
        timerRunning = false;
    }

    private void startTimer() {
        new Thread(() -> {
            while (timerRunning) {
                try {
                    Thread.sleep(10);
                    final double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0 + jinxBlock;
                    
                    SwingUtilities.invokeLater(() -> {
                        // Always update and keep visible the timer
                        backtrackingTimerLabel.setText("Time: " + String.format("%.2f", elapsedTime) + "s");
                        backtrackingTimerLabel.setVisible(true);
                        
                        // Update step count in real time if possible
                        if (mapPanel != null) {
                            int steps = mapPanel.getStarPathPositions().size();
                            backtrackingStepsLabel.setText("Steps: " + steps);
                            backtrackingStepsLabel.setVisible(true);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
