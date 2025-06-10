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
    private JTextField sizeField;
    private JTextArea output;
    private JScrollPane scrollPane, mapScrollPane, solutionMapScrollPane;
    private Map mapPanel, solutionMapPanel;
    private JLabel backtrackingTimerLabel, solutionTimerLabel;
    private long startTime;
    private boolean timerRunning;
    private double jinxBlock = 0;

    public Layar() {
        setLayout(null);
        setBackground(new Color(173, 216, 230));
        mapScrollPane = null;
        solutionMapScrollPane = null;
        JLabel label = new JLabel("Map size (1-15): ");

        label.setBounds(10, 0, 100, 40);
        label.setBackground(new Color(173, 216, 230));
        add(label);
        sizeField = new JTextField(2);
        sizeField.setBounds(105, 10, 30, 20);
        add(sizeField);
        JButton button = new JButton("Generate and Solve");

        button.setBounds(10, 35, 150, 30);
        add(button);
        backtrackingTimerLabel = new JLabel("");
        backtrackingTimerLabel.setBounds(535, 70, 100, 40);
        add(backtrackingTimerLabel);
        backtrackingTimerLabel.setVisible(false);
        solutionTimerLabel = new JLabel("");
        solutionTimerLabel.setBounds(1145, 70, 100, 40);
        solutionTimerLabel.setVisible(false);
        add(solutionTimerLabel);
        output = new JTextArea();
        output.setEditable(false);
        output.setMargin(new Insets(0, 10, 0, 0));
        output.setBackground(new Color(173, 216, 230));
        scrollPane = new JScrollPane(output);
        scrollPane.setBounds(0, 70, 1285, 1000);
        scrollPane.getViewport().setBackground(new Color(173, 216, 230));
        add(scrollPane);
        button.addActionListener(e -> onGenerate());
    }

    private void onGenerate() {
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
        output.append("Generating map...\n");
        output.append("Map generated with borders:\n");
        switch (size) {
            case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11: case 12: case 13: case 14: case 15:
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
                long seed = System.currentTimeMillis();
                
                mapPanel = new Map(size, seed);
                int mapSize = size * 40;
                
                mapPanel.setPreferredSize(new Dimension(mapSize, mapSize));
                mapPanel.setBackground(new Color(173, 216, 230));
                mapScrollPane = new JScrollPane(mapPanel);
                mapScrollPane.setBounds(6, 105, 610, 610);
                add(mapScrollPane);
                solutionMapPanel = new Map(size, seed);
                solutionMapPanel.setPreferredSize(new Dimension(mapSize, mapSize));
                solutionMapPanel.setBackground(new Color(173, 216, 230));
                solutionMapScrollPane = new JScrollPane(solutionMapPanel);
                solutionMapScrollPane.setBounds(615, 105, 610, 610);
                add(solutionMapScrollPane);
                startTime = System.currentTimeMillis();
                timerRunning = true;
                jinxBlock = 0;
                backtrackingTimerLabel.setText("");
                backtrackingTimerLabel.setVisible(true);
                solutionTimerLabel.setText("");
                solutionTimerLabel.setVisible(false);
                startTimer();
                
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        startJinxBlockDetector();
                        Backtracking backtracking = new Backtracking(mapPanel, mapPanel.getStarImage());
                        boolean backtrackingFound = backtracking.solveWithAnimation();
                        
                        stopTimer();
                        double backtrackingTime = (System.currentTimeMillis() - startTime) / 1000.0 + jinxBlock;
                        
                        SwingUtilities.invokeLater(() -> {
                            backtrackingTimerLabel.setText("Timer: " + String.format("%.2f", backtrackingTime) + "s");
                            solutionTimerLabel.setVisible(true);
                        });
                        Thread.sleep(500);
                        startTime = System.currentTimeMillis();
                        jinxBlock = 0;
                        timerRunning = true;
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
                        Solution solution = new Solution(solutionMapPanel, solutionMapPanel.getStarImage());
                        boolean solutionFound = solution.solveWithAnimation();
                        
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
                
            default:
                backtrackingTimerLabel.setVisible(false);
                if (scrollPane.getParent() == null) {
                    add(scrollPane);
                }
                MapGenerator mapGenerator = new MapGenerator(size);
                int[][] map = mapGenerator.generateMap();

                output.append("□ = Empty, ■ = Wall, S = Start, E = End\n");
                output.append(printMap(map));
                output.append("Applying backtracking...\n");
                startTime = System.currentTimeMillis();
                Backtracker backtracker = new Backtracker(map);
                boolean solutionFound = backtracker.solve();
                double solveTime = (System.currentTimeMillis() - startTime) / 1000.0;

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
                        backtrackingTimerLabel.setText("Timer: " + String.format("%.2f", elapsedTime) + "s");
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String printMap(int[][] map) {
        String[] symbols = {"□ ", "■ ", "S ", "E ", "× "};
        StringBuilder stringBuilder = new StringBuilder();

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
