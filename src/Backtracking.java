import java.awt.image.BufferedImage;

public class Backtracking {
    private int[][] map;
    private int[][] solution;
    private Map mapPanel;
    private BufferedImage starImage;
    private int size, ctr;
    private static final int PATH = 4;
    private static final int[] ROW_MOVES = {-1, 0, 1, 0};
    private static final int[] COL_MOVES = {0, 1, 0, -1};

    public Backtracking(Map mapPanel, BufferedImage starImage) {
        this.mapPanel = mapPanel;
        this.starImage = starImage;
        int[][] fullMap = mapPanel.getMapGenerator();

        this.size = fullMap.length - 2;
        this.map = new int[size][size];
        this.solution = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.map[i][j] = fullMap[i + 1][j + 1];
                this.solution[i][j] = fullMap[i + 1][j + 1];
            }
        }
        mapPanel.clearStarPathPositions();
    }

    public boolean solveWithAnimation() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == Map.START) {
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
        if (map[row][col] == Map.END) {
            mapPanel.addStarPathPosition(row, col);
            ctr++;
            return true;
        }
        if (map[row][col] != Map.START) {
            solution[row][col] = PATH;
        }
        mapPanel.addStarPathPosition(row, col);
        ctr++;
        mapPanel.repaint();
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 4; i++) {
            int newRow = row + ROW_MOVES[i], newCol = col + COL_MOVES[i];

            if (findPath(newRow, newCol)) {
                return true;
            }
        }
        if (map[row][col] != Map.START) {
            solution[row][col] = Map.EMPTY;
        }
        return false;
    }

    private boolean isValid(int row, int col) {
        if (row < 0 || col < 0 || row >= size || col >= size) {
            return false;
        }
        if (map[row][col] == Map.WALL || map[row][col] == PATH) {
            return false;
        }
        return true;
    }

    public int[][] getSolutionPath() {
        return solution;
    }

    public int getStepsCount() {
        return ctr;
    }

    public double getTime(){
        return (ctr * 150L) / 1000.0;
    }
}
