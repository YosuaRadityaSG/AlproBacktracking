public class Backtracker {
    private int[][] map;
    private int[][] solution;
    private int size;
    private static final int PATH = 4;
    private static final int[] ROW_MOVES = {-1, 0, 1, 0};
    private static final int[] COL_MOVES = {0, 1, 0, -1};
    
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
    
    public boolean solve() {
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
        if (!isValid(row, col)) {
            return false;
        }
        if (map[row][col] == MapGenerator.END) {
            return true;
        }
        if (map[row][col] != MapGenerator.START) {
            solution[row][col] = PATH;
        }
        for (int i = 0; i < 4; i++) {
            int newRow = row + ROW_MOVES[i], newCol = col + COL_MOVES[i];

            if (findPath(newRow, newCol)) {
                return true;
            }
        }
        if (map[row][col] != MapGenerator.START) {
            solution[row][col] = MapGenerator.EMPTY;
        }
        return false;
    }
    
    private boolean isValid(int row, int col) {
        if (row < 0 || col < 0 || row >= size || col >= size) {
            return false;
        }
        if (map[row][col] == MapGenerator.WALL || solution[row][col] == PATH) {
            return false;
        }
        return true;
    }
    
    public int[][] getSolutionPath() {
        return solution;
    }
}
