
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Layar extends JPanel{
    private JTextField sizeField;
    private JTextArea output;
    private JPanel mapPanel, centerPanel;
    private JLabel timeLabel;

    public Layar(){
        setLayout(new BorderLayout());
        JPanel panelContainer = new JPanel();

        panelContainer.setLayout(new BoxLayout(panelContainer, BoxLayout.Y_AXIS));
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        rowPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel label = new JLabel("Map size (1-15): ");

        rowPanel.add(label);
        sizeField = new JTextField(2);
        rowPanel.add(sizeField);
        panelContainer.add(rowPanel);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        buttonPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        JButton button = new JButton("Generate and Solve");

        buttonPanel.add(button);
        panelContainer.add(buttonPanel);
        output = new JTextArea(18, 40);
        output.setEditable(false);
        output.setBorder(new EmptyBorder(0, 10, 0, 0));
        mapPanel = new BacktrackMap();
        JScrollPane scrollPane = new JScrollPane(output);

        centerPanel = new JPanel(new CardLayout());
        centerPanel.add(scrollPane, "TEXT");
        timeLabel = new JLabel("");
        JPanel timePanel = new JPanel(new BorderLayout());

        timePanel.setBorder(new EmptyBorder(0, 10, 10, 0));
        timePanel.add(mapPanel, BorderLayout.CENTER);
        timePanel.add(timeLabel, BorderLayout.SOUTH);
        centerPanel.add(timePanel, "MAP");
        add(panelContainer, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        button.addActionListener(e -> onGenerate());
    }

    private void onGenerate() {
        int size;

        try {
            size = Integer.parseInt(sizeField.getText());
            if (size < 1 || size > 15) {
                output.setText("Invalid size. Please enter a number between 1 and 15.\n");
                ((CardLayout) centerPanel.getLayout()).show(centerPanel, "TEXT");
                return;
            }
        } catch (NumberFormatException ex) {
            output.setText("Please enter a valid integer.\n");
            ((CardLayout) centerPanel.getLayout()).show(centerPanel, "TEXT");
            return;
        }
        output.setText("");
        output.append("Generating map...\n");
        MapGenerator mapGenerator = new MapGenerator(size);
        int[][] map = mapGenerator.generateMap();

        output.append("Map generated with borders:\n");
        switch (size) {
            case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11: case 12: case 13: case 14: case 15:
                ((BacktrackMap) mapPanel).setMap(map, size);
                ((CardLayout) centerPanel.getLayout()).show(centerPanel, "MAP");
                List<int[]> path = ((BacktrackMap) mapPanel).findPathBacktrack();

                if (path == null || path.isEmpty()) {
                    timeLabel.setText("No path exists.\n");
                    return;
                }
                int time = ((BacktrackMap) mapPanel).getPathTime(path);

                ((BacktrackMap) mapPanel).animatePath(path, time, () -> {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        double seconds = path.size() * 0.3;
                        timeLabel.setText("Time: " + String.format("%.1f", seconds) + " seconds.\n");
                    });
                });
                break;
            default:
                mapPanel.setVisible(false);
                output.setVisible(true);
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
        StringBuilder sb = new StringBuilder();
        for (int[] row : map) {
            for (int value : row) {
                if (value >= 0 && value < symbols.length) {
                    sb.append(symbols[value]);
                } else {
                    sb.append(value).append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
