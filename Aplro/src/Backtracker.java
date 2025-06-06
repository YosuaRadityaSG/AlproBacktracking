public class Backtracker {
    private int[][] map;
    private int[][] solution;
    private int size;
    private static final int PATH = 4;
    
    // Possible movement directions: up, right, down, left
    private static final int[] ROW_MOVES = {-1, 0, 1, 0};
    private static final int[] COL_MOVES = {0, 1, 0, -1};
    
    public Backtracker(int[][] map) {
        this.map = map;
        this.size = map.length;
        this.solution = new int[size][size];
        
        // Initialize solution array
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                solution[i][j] = map[i][j];
            }
        }
    }
    
    public boolean solve() {
        // Find the start position (bottom-left)
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == MapGenerator.START) {
                    return findPath(i, j);
                }
            }
        }
        return false;
    }
    
    private boolean findPath(int row, int col) {
        // Check if current position is valid
        if (!isValid(row, col)) {
            return false;
        }
        
        // Check if we've reached the end
        if (map[row][col] == MapGenerator.END) {
            return true;
        }
        
        // Mark current cell as part of the solution path
        if (map[row][col] != MapGenerator.START) {
            solution[row][col] = PATH;
        }
        
        // Try all four directions
        for (int i = 0; i < 4; i++) {
            int newRow = row + ROW_MOVES[i];
            int newCol = col + COL_MOVES[i];
            if (findPath(newRow, newCol)) {
                return true;
            }
        }
        
        // If no direction leads to a solution, backtrack
        if (map[row][col] != MapGenerator.START) {
            solution[row][col] = MapGenerator.EMPTY;
        }
        
        return false;
    }
    
    private boolean isValid(int row, int col) {
        // Check if position is within bounds
        if (row < 0 || col < 0 || row >= size || col >= size) {
            return false;
        }
        
        // Check if the position is not a wall and not already in path
        if (map[row][col] == MapGenerator.WALL || solution[row][col] == PATH) {
            return false;
        }
        
        return true;
    }
    
    public int[][] getSolutionPath() {
        return solution;
    }
}
