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
        setLayout(new BorderLayout(0, 0));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(5, 10, 5, 10));
        createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        JPanel mainContentPanel = new JPanel(new BorderLayout(0, 0));

        mainContentPanel.setBackground(BACKGROUND_COLOR);
        createMapLabelsPanel();
        mainContentPanel.add(mapLabelsPanel, BorderLayout.NORTH);
        JPanel mapsPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        mapsPanel.setBackground(BACKGROUND_COLOR);
        JPanel leftMapHolder = new JPanel(new BorderLayout(0, 0));

        leftMapHolder.setBackground(BACKGROUND_COLOR);
        leftMapHolder.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Backtracking Map"
        ));
        JPanel rightMapHolder = new JPanel(new BorderLayout(0, 0));

        rightMapHolder.setBackground(BACKGROUND_COLOR);
        rightMapHolder.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Solution Map"
        ));
        mapsPanel.add(leftMapHolder);
        mapsPanel.add(rightMapHolder);
        mainContentPanel.add(mapsPanel, BorderLayout.CENTER);
        add(mainContentPanel, BorderLayout.CENTER);
        generateButton.addActionListener(e -> onGenerate());
        skipButton.addActionListener(e -> {
            skipRequested = true;
            skipButton.setEnabled(false);
            skipButton.setText("Skipping...");
        });
    }

    private void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlPanel.setBackground(BACKGROUND_COLOR);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(2, 5, 5, 5)
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
        JLabel descLabel = new JLabel("Visualization will show both algorithms working simultaneously");

        descLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        descLabel.setForeground(new Color(100, 100, 100));
        JPanel descPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        descPanel.setBackground(BACKGROUND_COLOR);
        descPanel.add(descLabel);
        controlPanel.add(descPanel);
    }
    
    private void createMapLabelsPanel() {
        mapLabelsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        mapLabelsPanel.setBackground(BACKGROUND_COLOR);
        JPanel leftLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

        leftLabelPanel.setBackground(BACKGROUND_COLOR);
        JPanel leftTitleAndStatsPanel = new JPanel();

        leftTitleAndStatsPanel.setLayout(new BoxLayout(leftTitleAndStatsPanel, BoxLayout.Y_AXIS));
        leftTitleAndStatsPanel.setBackground(BACKGROUND_COLOR);
        backtrackingTitleLabel = createStyledLabel("Backtracking Algorithm");
        backtrackingTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        backtrackingTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JPanel leftStatsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

        leftStatsPanel.setBackground(BACKGROUND_COLOR);
        backtrackingTimerLabel = createStyledLabel("Time: 0.00s");
        backtrackingStepsLabel = createStyledLabel("Steps: 0");
        leftStatsPanel.add(backtrackingTimerLabel);
        leftStatsPanel.add(backtrackingStepsLabel);
        leftTitleAndStatsPanel.add(backtrackingTitleLabel);
        leftTitleAndStatsPanel.add(leftStatsPanel);
        leftLabelPanel.add(leftTitleAndStatsPanel);
        JPanel rightLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

        rightLabelPanel.setBackground(BACKGROUND_COLOR);
        JPanel rightTitleAndStatsPanel = new JPanel();

        rightTitleAndStatsPanel.setLayout(new BoxLayout(rightTitleAndStatsPanel, BoxLayout.Y_AXIS));
        rightTitleAndStatsPanel.setBackground(BACKGROUND_COLOR);
        solutionTitleLabel = createStyledLabel("Optimal Solution");
        solutionTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        solutionTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JPanel rightStatsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

        rightStatsPanel.setBackground(BACKGROUND_COLOR);
        solutionTimerLabel = createStyledLabel("Time: 0.00s");
        solutionStepsLabel = createStyledLabel("Steps: 0");
        rightStatsPanel.add(solutionTimerLabel);
        rightStatsPanel.add(solutionStepsLabel);
        rightTitleAndStatsPanel.add(solutionTitleLabel);
        rightTitleAndStatsPanel.add(rightStatsPanel);
        rightLabelPanel.add(rightTitleAndStatsPanel);
        mapLabelsPanel.add(leftLabelPanel);
        mapLabelsPanel.add(rightLabelPanel);
        solutionTitleLabel.setVisible(true);
        solutionTimerLabel.setVisible(true);
        solutionStepsLabel.setVisible(true);
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
        label.setForeground(LABEL_COLOR);
        label.setBorder(new EmptyBorder(2, 5, 2, 5));
        return label;
    }
    
    private Border createTitledBorder(String title) {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            title
        );
        titledBorder.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        titledBorder.setTitleColor(LABEL_COLOR);
        return BorderFactory.createCompoundBorder(
            titledBorder,
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );
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
        JPanel mainContentPanel = (JPanel) getComponent(1), mapsPanel = (JPanel) mainContentPanel.getComponent(1), leftMapHolder = (JPanel) mapsPanel.getComponent(0), rightMapHolder = (JPanel) mapsPanel.getComponent(1);
        
        leftMapHolder.removeAll();
        rightMapHolder.removeAll();
        long seed = System.currentTimeMillis();

        mapPanel = new Map(size, seed);
        solutionMapPanel = new Map(size, seed);
        int mapSize = size * 45;
        
        mapPanel.setPreferredSize(new Dimension(mapSize, mapSize));
        mapPanel.setBackground(BACKGROUND_COLOR);
        solutionMapPanel.setPreferredSize(new Dimension(mapSize, mapSize));
        solutionMapPanel.setBackground(BACKGROUND_COLOR);
        mapScrollPane = new JScrollPane(mapPanel);
        mapScrollPane.setBorder(null);
        mapScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mapScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        solutionMapScrollPane = new JScrollPane(solutionMapPanel);
        solutionMapScrollPane.setBorder(null);
        solutionMapScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        solutionMapScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        leftMapHolder.add(mapScrollPane, BorderLayout.CENTER);
        rightMapHolder.add(solutionMapScrollPane, BorderLayout.CENTER);
        backtrackingTitleLabel.setVisible(true);
        solutionTitleLabel.setVisible(true);
        backtrackingTimerLabel.setVisible(true);
        backtrackingStepsLabel.setVisible(true);
        solutionTimerLabel.setVisible(true);
        solutionStepsLabel.setVisible(true);
        startTime = System.currentTimeMillis();
        timerRunning = true;
        jinxBlock = 0;
        startTimer();
        skipButton.setEnabled(true);
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
                    if (backtracking.getStepsCount() > solution.getStepsCount()) {
                        solutionStepsLabel.setForeground(new Color(0, 130, 0));
                        backtrackingStepsLabel.setForeground(new Color(180, 0, 0));
                    } else if (backtracking.getStepsCount() < solution.getStepsCount()) {
                        backtrackingStepsLabel.setForeground(new Color(0, 130, 0));
                        solutionStepsLabel.setForeground(new Color(180, 0, 0));
                    }
                });
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void handleTextBasedMaze(int size) {
        MapGenerator mapGenerator = new MapGenerator(size);
        int[][] map = mapGenerator.generateMap();

        JOptionPane.showMessageDialog(this,
            "Text-based maze solution completed.\nSize: " + size + "x" + size,
            "Solution Complete", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
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
                        backtrackingTimerLabel.setText("Time: " + String.format("%.2f", elapsedTime) + "s");
                        if (mapPanel != null) {
                            int steps = mapPanel.getStarPathPositions().size();
                            backtrackingStepsLabel.setText("Steps: " + steps);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
