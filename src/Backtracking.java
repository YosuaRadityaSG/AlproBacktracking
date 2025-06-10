import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class Backtracking {
    private int[][] map;
    private int[][] solution;
    private Map mapPanel;
    private BufferedImage starImage;
    private int size, ctr;
    private static final int PATH = 8;
    private static final int[] ROW_MOVES = {-1, 0, 1, 0};
    private static final int[] COL_MOVES = {0, 1, 0, -1};
    private Stack<int[]> pathStack = new Stack<>();
    private Random random = new Random();

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
        int[][] internalMap = mapPanel.getInternalMap();
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (internalMap[i][j] == 2) {
                    this.map[i][j] = Map.PORTAL1;
                    this.solution[i][j] = Map.PORTAL1;
                } else if (internalMap[i][j] == 3) {
                    this.map[i][j] = Map.PORTAL2;
                    this.solution[i][j] = Map.PORTAL2;
                } else if (internalMap[i][j] == 1) {
                    this.map[i][j] = Map.ANGIN;
                    this.solution[i][j] = Map.ANGIN;
                }
            }
        }
        mapPanel.clearStarPathPositions();
    }

    public boolean solveWithAnimation() {
        boolean result = false;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == Map.START) {
                    result = findPath(i, j);
                    break;
                }
            }
        }
        return result;
    }

    private boolean findPath(int row, int col) {
        if (!isValid(row, col)) {
            return false;
        }
        boolean isJinxBlock = isJinxBlock(row, col);
        
        if (map[row][col] == Map.END) {
            mapPanel.addStarPathPosition(row, col);
            pathStack.push(new int[]{row, col});
            ctr++;
            return true;
        }
        if (map[row][col] != Map.START) {
            solution[row][col] = PATH;
        }
        mapPanel.addStarPathPosition(row, col);
        pathStack.push(new int[]{row, col});
        ctr++;
        mapPanel.repaint();
        try {
            if (isJinxBlock) {
                Thread.sleep(2000);
            } else {
                Thread.sleep(150);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (map[row][col] == Map.ANGIN) {
            removeLastNStarPositions(2);
            if (!pathStack.isEmpty()) pathStack.pop();
            mapPanel.repaint();
            return false;
        }
        if (map[row][col] == Map.PORTAL1 || map[row][col] == Map.PORTAL2) {
            int[][] internalMap = mapPanel.getInternalMap();
            int otherPortalValue = (internalMap[row][col] == 2) ? 3 : 2;
        
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if ((i != row || j != col) && internalMap[i][j] == otherPortalValue) {
                        mapPanel.addStarPathPosition(i, j);
                        pathStack.push(new int[]{i, j});
                        solution[i][j] = PATH;
                        ctr++;
                        mapPanel.repaint();
                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        int[] randomDirection = getRandomDirection();

                        for (int dir : randomDirection) {
                            int newRow = i + ROW_MOVES[dir];
                            int newCol = j + COL_MOVES[dir];
                            
                            if (findPath(newRow, newCol)) {
                                return true;
                            }
                        }
                        solution[i][j] = (map[i][j] == Map.PORTAL1) ? Map.PORTAL1 : Map.PORTAL2;
                        if (!pathStack.isEmpty()) {
                            pathStack.pop();
                        }
                        removeLastNStarPositions(1);
                        mapPanel.repaint();
                        return false;
                    }
                }
            }
        }
        int[] randomDirections = getRandomDirection();

        for (int dir : randomDirections) {
            int newRow = row + ROW_MOVES[dir], newCol = col + COL_MOVES[dir];
            boolean alreadyVisited = false;

            for (int[] pos : pathStack) {
                if (pos[0] == newRow && pos[1] == newCol) {
                    alreadyVisited = true;
                    break;
                }
            }
            if (!alreadyVisited && findPath(newRow, newCol)) {
                return true;
            }
        }
        if (map[row][col] != Map.START) {
            solution[row][col] = map[row][col];
        }
        if (!pathStack.isEmpty()) {
            pathStack.pop();
        }
        removeLastNStarPositions(1);
        mapPanel.repaint();
        return false;
    }
    
    private int[] getRandomDirection() {
        int[] directions = {0, 1, 2, 3};
        
        for (int i = directions.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1), temp = directions[i];

            directions[i] = directions[j];
            directions[j] = temp;
        }
        return directions;
    }
    
    private void removeLastNStarPositions(int n) {
        try {
            Field field = mapPanel.getClass().getDeclaredField("starPathPositions");

            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<int[]> starPathPositions = (List<int[]>) field.get(mapPanel);
            for (int i = 0; i < n && !starPathPositions.isEmpty(); i++) {
                starPathPositions.remove(starPathPositions.size() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isValid(int row, int col) {
        if (row < 0 || col < 0 || row >= size || col >= size) {
            return false;
        }
        if (map[row][col] == Map.WALL) {
            return false;
        }
        for (int[] pos : pathStack) {
            if (pos[0] == row && pos[1] == col && !(map[row][col] == Map.END)) {
                return false;
            }
        }
        return true;
    }

    private boolean isJinxBlock(int row, int col) {
        int[][] internalMap = mapPanel.getInternalMap();
        return row >= 0 && row < internalMap.length && 
               col >= 0 && col < internalMap[0].length && 
               internalMap[row][col] == 4;
    }

    public int[][] getSolutionPath() {
        return solution;
    }

    public int getStepsCount() {
        return ctr;
    }
}
