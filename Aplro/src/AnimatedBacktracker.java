import java.util.Stack;

public class AnimatedBacktracker {
    private int[][] map;
    private int[][] solution;
    private int size;
    private MazePanel panel;
    private boolean solved = false;
    private boolean noSolutionExists = false;
    
    // Path marker
    private static final int PATH = 4;
    
    // Possible movement directions: up, right, down, left
    private static final int[] ROW_MOVES = {-1, 0, 1, 0};
    private static final int[] COL_MOVES = {0, 1, 0, -1};
    
    // Stack for tracking the solution path
    private Stack<Position> stack = new Stack<>();
    private Position startPosition;
    
    public AnimatedBacktracker(int[][] map, MazePanel panel) {
        this.map = map;
        this.size = map.length;
        this.solution = new int[size][size];
        this.panel = panel;
        
        // Initialize solution array
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                solution[i][j] = map[i][j];
                
                // Find start position
                if (map[i][j] == MapGenerator.START) {
                    startPosition = new Position(i, j, -1); // No direction yet
                }
            }
        }
        
        // Initialize the stack with the start position
        if (startPosition != null) {
            stack.push(startPosition);
            panel.setCurrentPosition(startPosition.row, startPosition.col);
        }
    }
    
    // Perform a single step of the backtracking algorithm
    public boolean step() {
        if (solved || noSolutionExists || stack.isEmpty()) {
            return false;
        }
        
        Position current = stack.peek();
        
        // If we've reached the end, we're done
        if (map[current.row][current.col] == MapGenerator.END) {
            // Mark the path in the solution
            while (!stack.isEmpty()) {
                Position pos = stack.pop();
                if (map[pos.row][pos.col] != MapGenerator.START && 
                    map[pos.row][pos.col] != MapGenerator.END) {
                    solution[pos.row][pos.col] = PATH;
                    panel.addPathCell(pos.row, pos.col);
                }
            }
            
            solved = true;
            panel.clearCurrentPosition();
            return false; // No more steps needed
        }
        
        // Try the next direction
        current.direction++;
        
        // If we've tried all directions, backtrack
        if (current.direction >= ROW_MOVES.length) {
            stack.pop();
            
            // Mark cell as visited (backtracked)
            panel.addVisitedCell(current.row, current.col);
            
            // Update current position visual
            if (!stack.isEmpty()) {
                Position next = stack.peek();
                panel.setCurrentPosition(next.row, next.col);
            } else {
                panel.clearCurrentPosition();
                noSolutionExists = true;
                return false; // No solution
            }
            
            return true; // Continue stepping
        }
        
        // Get next position based on current direction
        int nextRow = current.row + ROW_MOVES[current.direction];
        int nextCol = current.col + COL_MOVES[current.direction];
        
        // Check if the next position is valid
        if (isValid(nextRow, nextCol)) {
            // Push next position to stack
            stack.push(new Position(nextRow, nextCol, -1));
            
            // Update visualization
            panel.setCurrentPosition(nextRow, nextCol);
            
            return true; // Continue stepping
        }
        
        // If not valid, continue with next step (try next direction)
        return true;
    }
    
    // Run the entire algorithm without animation
    public boolean solve() {
        while (step()) {
            // Keep stepping until done
        }
        return solved;
    }
    
    private boolean isValid(int row, int col) {
        // Check if position is within bounds
        if (row < 0 || col < 0 || row >= size || col >= size) {
            return false;
        }
        
        // Check if the position is not a wall
        if (map[row][col] == MapGenerator.WALL) {
            return false;
        }
        
        // Check if position is already in the path (in the stack)
        for (Position pos : stack) {
            if (pos.row == row && pos.col == col) {
                return false;
            }
        }
        
        return true;
    }
    
    public int[][] getSolutionPath() {
        return solution;
    }
    
    public boolean isSolved() {
        return solved;
    }
    
    // Helper class to track positions and the current direction being explored
    private class Position {
        int row, col, direction;
        
        Position(int row, int col, int direction) {
            this.row = row;
            this.col = col;
            this.direction = direction;
        }
    }
}
