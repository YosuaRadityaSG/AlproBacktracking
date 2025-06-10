public class Backtracker {
    // Private fields for maze data and solution tracking
    private int[][] map;         // Original maze structure
    private int[][] solution;    // Solution path being built
    private int size;            // Size of the maze
    private static final int PATH = 4; // Constant for marking the path
    // Direction arrays for 4-way movement (up, right, down, left)
    private static final int[] ROW_MOVES = {-1, 0, 1, 0};
    private static final int[] COL_MOVES = {0, 1, 0, -1};
    
    // Constructor - copies maze data and initializes solution array
    public Backtracker(int[][] map) {
        this.map = map;
        this.size = map.length;
        this.solution = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                solution[i][j] = map[i][j];
            }
        }
    }
    
    // Main solving method - finds START position and initiates recursive search
    public boolean solve() {
        // Find the starting position
        // Begin depth-first search from start
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == MapGenerator.START) {
                    return findPath(i, j);
                }
            }
        }
        return false;
    }
    
    // Recursive depth-first search with backtracking
    private boolean findPath(int row, int col) {
        // Check if position is valid
        if (!isValid(row, col)) {
            return false;
        }
        // Check if reached END position
        if (map[row][col] == MapGenerator.END) {
            return true;
        }
        // Mark current position as part of path
        if (map[row][col] != MapGenerator.START) {
            solution[row][col] = PATH;
        }
        // Try all four directions (up, right, down, left)
        for (int i = 0; i < 4; i++) {
            int newRow = row + ROW_MOVES[i], newCol = col + COL_MOVES[i];

            if (findPath(newRow, newCol)) {
                return true;
            }
        }
        // If no solution found in any direction, backtrack by unmarking current position
        if (map[row][col] != MapGenerator.START) {
            solution[row][col] = MapGenerator.EMPTY;
        }
        return false;
    }
    
    // Check if a position is valid to visit
    private boolean isValid(int row, int col) {
        // Check boundaries
        if (row < 0 || col < 0 || row >= size || col >= size) {
            return false;
        }
        // Check for walls and already visited cells
        if (map[row][col] == MapGenerator.WALL || solution[row][col] == PATH) {
            return false;
        }
        return true;
    }
    
    // Return the solution path
    public int[][] getSolutionPath() {
        return solution;
    }
}
