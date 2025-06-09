
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Layar extends JPanel{
    private JTextField sizeField;
    private JTextArea output;
    private JScrollPane scrollPane, mapScrollPane;
    private Map mapPanel;

    public Layar(){
        setLayout(null);
        mapScrollPane = null;
        JLabel label = new JLabel("Map size (1-15): ");

        label.setBounds(10, 0, 100, 40);
        add(label);
        sizeField = new JTextField(2);
        sizeField.setBounds(105, 10, 30, 20);
        add(sizeField);
        JButton button = new JButton("Generate and Solve");

        button.setBounds(10, 30, 150, 25);
        add(button);
        output = new JTextArea();
        output.setEditable(false);
        output.setMargin(new Insets(0, 10, 0, 0));
        scrollPane = new JScrollPane(output);
        scrollPane.setBounds(0, 60, 1285, 1000);
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
                if (mapScrollPane != null) {
                    remove(mapScrollPane);
                    mapScrollPane = null;
                }
                mapPanel = new Map(size);
                int mapSize = size * 40;
                
                mapPanel.setPreferredSize(new Dimension(mapSize, mapSize));
                mapScrollPane = new JScrollPane(mapPanel);
                mapScrollPane.setBounds(10, 60, 685, 685);
                add(mapScrollPane);
                revalidate();
                repaint();
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        Backtracking backtracking = new Backtracking(mapPanel, mapPanel.getStarImage());
                        boolean found = backtracking.solveWithAnimation();
                        double time = backtracking.getTime();

                        javax.swing.SwingUtilities.invokeLater(() -> {
                            if (found) {
                                System.out.println("Time: " + time + " seconds.");
                            } else {
                                System.out.println("No path exists.");
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            break;
            default:
                if (scrollPane.getParent() == null) {
                    add(scrollPane);
                }
                MapGenerator mapGenerator = new MapGenerator(size);
                int[][] map = mapGenerator.generateMap();

                output.append("□ = Empty, ■ = Wall, S = Start, E = End\n");
                output.append(printMap(map));
                output.append("Applying backtracking...\n");
                Backtracker backtracker = new Backtracker(map);
                boolean solutionFound = backtracker.solve();

                if (solutionFound) {
                    output.append("Solution found!\n");
                    output.append("□ = Empty, ■ = Wall, S = Start, E = End, × = Path\n");
                    output.append(printMap(backtracker.getSolutionPath()));
                } else {
                    output.append("No solution exists.\n");
                    output.append(printMap(map));
                }
                break;
        }
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
