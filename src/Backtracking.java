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
    private int size, stepCount, exploreCount;
    private static final int PATH = 8;
    private static final int[] ROW_MOVES = {-1, 0, 1, 0};
    private static final int[] COL_MOVES = {0, 1, 0, -1};
    private Stack<int[]> pathStack = new Stack<>();
    private Random random = new Random();
    private Layar layar;
    private boolean skipped = false;
    private long startTime, endTime;

    public Backtracking(Map mapPanel, BufferedImage starImage) {
        this.mapPanel = mapPanel;
        this.starImage = starImage;
        this.stepCount = 0;
        this.exploreCount = 0;
        this.startTime = System.currentTimeMillis();
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

    public boolean solveWithAnimation(Layar layar) {
        this.layar = layar;
        boolean result = false;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == Map.START) {
                    result = findPath(i, j);
                    if (skipped) {
                        displayFinalPath();
                    }
                    break;
                }
            }
        }
        this.endTime = System.currentTimeMillis();
        return result;
    }

    private void displayFinalPath() {
        mapPanel.clearStarPathPositions();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (solution[i][j] == PATH) {
                    mapPanel.addStarPathPosition(i, j);
                    stepCount++;
                }
            }
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == Map.START || map[i][j] == Map.END) {
                    mapPanel.addStarPathPosition(i, j);
                }
            }
        }
        mapPanel.repaint();
    }

    private boolean findPath(int row, int col) {
        if (layar != null && layar.isSkipRequested() && !skipped) {
            skipped = true;
            return false;
        }
        if (!isValid(row, col)) {
            return false;
        }
        exploreCount++;
        boolean isJinxBlock = isJinxBlock(row, col);
        
        if (map[row][col] == Map.END) {
            mapPanel.addStarPathPosition(row, col);
            pathStack.push(new int[]{row, col});
            stepCount++;
            return true;
        }
        if (map[row][col] != Map.START) {
            solution[row][col] = PATH;
        }
        mapPanel.addStarPathPosition(row, col);
        pathStack.push(new int[]{row, col});
        stepCount++;
        mapPanel.repaint();
        if (!skipped) {
            try {
                if (isJinxBlock) {
                    Thread.sleep(2000);
                } else {
                    Thread.sleep(150);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (map[row][col] == Map.ANGIN) {
            if (!pathStack.isEmpty()) {
                pathStack.pop();
            }
            removeLastNStarPositions(1);
            if (!pathStack.isEmpty()) {
                int[] prevPos = pathStack.pop();
                
                solution[prevPos[0]][prevPos[1]] = map[prevPos[0]][prevPos[1]];
                removeLastNStarPositions(1);
            }
            mapPanel.repaint();
            return false;
        }
        
        if (map[row][col] == Map.PORTAL1 || map[row][col] == Map.PORTAL2) {
            return handlePortal(row, col);
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
            if (skipped) {
                return false;
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
    
    private boolean handlePortal(int row, int col) {
        int[][] internalMap = mapPanel.getInternalMap();
        int otherPortalValue = (internalMap[row][col] == 2) ? 3 : 2;
    
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if ((i != row || j != col) && internalMap[i][j] == otherPortalValue) {
                    mapPanel.addStarPathPosition(i, j);
                    pathStack.push(new int[]{i, j});
                    solution[i][j] = PATH;
                    stepCount++;
                    mapPanel.repaint();
                    if (!skipped) {
                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (layar != null && layar.isSkipRequested()) {
                        skipped = true;
                        return false;
                    }
                    int[] randomDirection = getRandomDirection();

                    for (int dir : randomDirection) {
                        int newRow = i + ROW_MOVES[dir], newCol = j + COL_MOVES[dir];
                        
                        if (findPath(newRow, newCol)) {
                            return true;
                        }
                        if (skipped) {
                            return false;
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
        return row >= 0 && row < internalMap.length && col >= 0 && col < internalMap[0].length && internalMap[row][col] == 4;
    }

    public int[][] getSolutionPath() {
        return solution;
    }

    public int getStepsCount() {
        return stepCount;
    }
    
    public int getExploredCount() {
        return exploreCount;
    }
    
    public double getTime() {
        return (endTime - startTime) / 1000.0;
    }
}
