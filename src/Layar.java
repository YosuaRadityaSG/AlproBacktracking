
import java.awt.BorderLayout;
import java.awt.FlowLayout;
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

    public Layar(){
        setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel();

        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JLabel label = new JLabel("Map size (1-15): ");

        rowPanel.add(label);
        sizeField = new JTextField(2);

        rowPanel.add(sizeField);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        JButton button = new JButton("Generate and Solve");

        buttonPanel.add(button);
        inputPanel.add(rowPanel);
        inputPanel.add(buttonPanel);
        output = new JTextArea(18, 40);
        output.setEditable(false);
        output.setBorder(new EmptyBorder(0, 10, 0, 0));
        JScrollPane scrollPane = new JScrollPane(output);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        button.addActionListener(e -> onGenerate());
    }

    private void onGenerate() {
        int size;

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
        MapGenerator mapGenerator = new MapGenerator(size);
        int[][] map = mapGenerator.generateMap();

        output.append("Map generated with borders:\n");
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
