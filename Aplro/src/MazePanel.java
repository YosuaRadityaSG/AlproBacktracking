import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MazePanel extends JPanel {
    private int[][] maze;
    private int[][] solution;
    private Point currentPosition;
    private List<Point> visitedCells;
    private List<Point> pathCells;
    
    // Colors for different cell types
    private static final Color EMPTY_COLOR = Color.WHITE;
    private static final Color WALL_COLOR = Color.BLACK;
    private static final Color START_COLOR = Color.GREEN;
    private static final Color END_COLOR = Color.RED;
    private static final Color PATH_COLOR = Color.BLUE;
    private static final Color CURRENT_COLOR = new Color(0, 191, 255); // Deep Sky Blue
    private static final Color VISITED_COLOR = new Color(230, 230, 250); // Lavender
    private static final Color BACKTRACKED_COLOR = new Color(255, 182, 193); // Light Pink
    
    public MazePanel() {
        setBackground(Color.WHITE);
        visitedCells = new ArrayList<>();
        pathCells = new ArrayList<>();
    }
    
    public void setMaze(int[][] maze, int[][] solution) {
        this.maze = maze;
        this.solution = solution;
        this.currentPosition = null;
        this.visitedCells = new ArrayList<>();
        this.pathCells = new ArrayList<>();
        repaint();
    }
    
    public void setCurrentPosition(int row, int col) {
        currentPosition = new Point(col, row);
        repaint();
    }
    
    public void addVisitedCell(int row, int col) {
        visitedCells.add(new Point(col, row));
        repaint();
    }
    
    public void addPathCell(int row, int col) {
        pathCells.add(new Point(col, row));
        repaint();
    }
    
    public void clearCurrentPosition() {
        currentPosition = null;
        repaint();
    }
    
    public void resetAnimation() {
        currentPosition = null;
        visitedCells.clear();
        pathCells.clear();
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (maze == null) return;
        
        int width = getWidth();
        int height = getHeight();
        int rows = maze.length;
        int cols = maze[0].length;
        
        int cellWidth = width / cols;
        int cellHeight = height / rows;
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw the maze cells
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int x = j * cellWidth;
                int y = i * cellHeight;
                
                // Default to using the maze array
                int cellValue = maze[i][j];
                
                // Set color based on cell value and animation state
                Color cellColor = getCellColor(i, j, cellValue);
                
                // Fill cell
                g2d.setColor(cellColor);
                g2d.fillRect(x, y, cellWidth, cellHeight);
                
                // Draw cell border
                g2d.setColor(Color.GRAY);
                g2d.drawRect(x, y, cellWidth, cellHeight);
                
                // Add labels for start and end
                if (cellValue == MapGenerator.START) {
                    drawCellText(g2d, "S", x, y, cellWidth, cellHeight);
                } else if (cellValue == MapGenerator.END) {
                    drawCellText(g2d, "E", x, y, cellWidth, cellHeight);
                }
            }
        }
    }
    
    private Color getCellColor(int row, int col, int cellValue) {
        // Check if this is the current position in the animation
        if (currentPosition != null && 
            currentPosition.x == col && currentPosition.y == row) {
            return CURRENT_COLOR;
        }
        
        // Check if this cell is part of the final solution path
        for (Point p : pathCells) {
            if (p.x == col && p.y == row) {
                return PATH_COLOR;
            }
        }
        
        // Check if this cell has been visited during the animation
        for (Point p : visitedCells) {
            if (p.x == col && p.y == row) {
                return VISITED_COLOR;
            }
        }
        
        // If solution exists and this cell is part of the path
        if (solution != null && solution[row][col] == 4) {
            return PATH_COLOR;
        }
        
        // Default colors based on cell type
        switch (cellValue) {
            case MapGenerator.EMPTY: return EMPTY_COLOR;
            case MapGenerator.WALL: return WALL_COLOR;
            case MapGenerator.START: return START_COLOR;
            case MapGenerator.END: return END_COLOR;
            case 4: return PATH_COLOR; // Path
            default: return Color.GRAY;
        }
    }
    
    private void drawCellText(Graphics2D g2d, String text, int x, int y, int width, int height) {
        g2d.setColor(Color.BLACK);
        Font originalFont = g2d.getFont();
        Font largerFont = originalFont.deriveFont(Font.BOLD, 14.0f);
        g2d.setFont(largerFont);
        
        FontMetrics metrics = g2d.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();
        
        // Center the text in the cell
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - textHeight) / 2 + metrics.getAscent();
        
        g2d.drawString(text, textX, textY);
        g2d.setFont(originalFont);
    }
    
    @Override
    public Dimension getPreferredSize() {
        if (maze == null) {
            return new Dimension(400, 400);
        }
        return new Dimension(maze[0].length * 20, maze.length * 20);
    }
}
