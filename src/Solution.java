import java.awt.image.BufferedImage;
import java.util.*;

public class Solution {
    private int[][] map;
    private int[][] solution;
    private Map mapPanel;
    private BufferedImage starImage;
    private int size, ctr;
    private static final int PATH = 8;
    private static final int[] ROW_MOVES = {-1, 0, 1, 0};
    private static final int[] COL_MOVES = {0, 1, 0, -1};
    private long startTime, endTime;
    private Layar layar;
    private boolean skipped = false;

    public Solution(Map mapPanel, BufferedImage starImage) {
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

    public boolean solveWithAnimation(Layar layar) {
        this.layar = layar;
        this.startTime = System.currentTimeMillis();
        boolean result = false;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == Map.START) {
                    result = findPath(i, j);
                    break;
                }
            }
        }
        this.endTime = System.currentTimeMillis();
        return result;
    }

    public boolean solveWithAnimation() {
        return solveWithAnimation(null);
    }

    private boolean findPath(int startRow, int startCol) {
        int[][] distance = new int[size][size];
        boolean[][] visited = new boolean[size][size];
        int[][][] parent = new int[size][size][2];
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                distance[i][j] = Integer.MAX_VALUE;
                parent[i][j][0] = -1;
                parent[i][j][1] = -1;
            }
        }
        distance[startRow][startCol] = 0;
        boolean pathFound = false;
        
        while (!pathFound) {
            // Check for skip request
            if (layar != null && layar.isSkipRequested() && !skipped) {
                skipped = true;
                // Complete the solution quickly
                boolean quickResult = completeSolutionQuickly(startRow, startCol);
                return quickResult;
            }
            
            int minDist = Integer.MAX_VALUE, row = -1, col = -1;
            
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (!visited[i][j] && distance[i][j] < minDist) {
                        minDist = distance[i][j];
                        row = i;
                        col = j;
                    }
                }
            }
            if (row == -1 || minDist == Integer.MAX_VALUE) {
                break;
            }
            visited[row][col] = true;
            boolean isJinxBlock = isJinxBlock(row, col);
            
            if (row == startRow && col == startCol) {
                mapPanel.addStarPathPosition(row, col);
                mapPanel.repaint();
                if (!skipped) {
                    if (isJinxBlock) {
                        delay(2000);
                    } else {
                        delay(150);
                    }
                }
            }
            if (map[row][col] == Map.END) {
                pathFound = true;
                if (skipped) {
                    reconstructPath(parent, row, col);
                    displayFinalPath();
                } else {
                    reconstructPathWithVisualization(parent, row, col);
                }
                return true;
            }
            if (map[row][col] == Map.PORTAL1 || map[row][col] == Map.PORTAL2) {
                handlePortal(row, col, distance, visited, parent);
                continue;
            }
            if (map[row][col] == Map.ANGIN) {
                continue;
            }
            for (int i = 0; i < 4; i++) {
                int newRow = row + ROW_MOVES[i], newCol = col + COL_MOVES[i];
                
                if (isValid(newRow, newCol) && !visited[newRow][newCol]) {
                    int newDist = distance[row][col] + 1;
                    if (newDist < distance[newRow][newCol]) {
                        distance[newRow][newCol] = newDist;
                        parent[newRow][newCol][0] = row;
                        parent[newRow][newCol][1] = col;
                    }
                }
            }
        }
        return false;
    }
    
    private boolean completeSolutionQuickly(int startRow, int startCol) {
        // Use a simpler algorithm to quickly find the solution without animation
        int[][] distance = new int[size][size];
        boolean[][] visited = new boolean[size][size];
        int[][][] parent = new int[size][size][2];
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                distance[i][j] = Integer.MAX_VALUE;
                parent[i][j][0] = -1;
                parent[i][j][1] = -1;
            }
        }
        
        distance[startRow][startCol] = 0;
        
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;
        
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int row = current[0], col = current[1];
            
            if (map[row][col] == Map.END) {
                reconstructPath(parent, row, col);
                displayFinalPath();
                return true;
            }
            
            if (map[row][col] == Map.PORTAL1 || map[row][col] == Map.PORTAL2) {
                int[][] internalMap = mapPanel.getInternalMap();
                int portalValue = internalMap[row][col], otherPortalValue = (portalValue == 2) ? 3 : 2;
                
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        if ((i != row || j != col) && internalMap[i][j] == otherPortalValue && !visited[i][j]) {
                            visited[i][j] = true;
                            distance[i][j] = distance[row][col] + 1;
                            parent[i][j][0] = row;
                            parent[i][j][1] = col;
                            queue.add(new int[]{i, j});
                        }
                    }
                }
                continue;
            }
            
            if (map[row][col] == Map.ANGIN) {
                continue;
            }
            
            for (int i = 0; i < 4; i++) {
                int newRow = row + ROW_MOVES[i], newCol = col + COL_MOVES[i];
                
                if (isValid(newRow, newCol) && !visited[newRow][newCol]) {
                    visited[newRow][newCol] = true;
                    distance[newRow][newCol] = distance[row][col] + 1;
                    parent[newRow][newCol][0] = row;
                    parent[newRow][newCol][1] = col;
                    queue.add(new int[]{newRow, newCol});
                }
            }
        }
        
        return false;
    }
    
    private void displayFinalPath() {
        mapPanel.clearStarPathPositions();
        
        // Reconstruct the path from the solution
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (solution[i][j] == PATH) {
                    mapPanel.addStarPathPosition(i, j);
                }
            }
        }
        
        // Add start and end positions
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == Map.START || map[i][j] == Map.END) {
                    mapPanel.addStarPathPosition(i, j);
                }
            }
        }
        
        mapPanel.repaint();
    }
    
    private void handlePortal(int row, int col, int[][] distance, boolean[][] visited, int[][][] parent) {
        int[][] internalMap = mapPanel.getInternalMap();
        int portalValue = internalMap[row][col], otherPortalValue = (portalValue == 2) ? 3 : 2;
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if ((i != row || j != col) && internalMap[i][j] == otherPortalValue && !visited[i][j]) {
                    int newDist = distance[row][col] + 1;
                    if (newDist < distance[i][j]) {
                        distance[i][j] = newDist;
                        parent[i][j][0] = row;
                        parent[i][j][1] = col;
                        
                        mapPanel.addStarPathPosition(i, j);
                        ctr++;
                        mapPanel.repaint();
                        if (!skipped) {
                            delay(150);
                        }
                    }
                    return;
                }
            }
        }
    }
    
    private void reconstructPath(int[][][] parent, int endRow, int endCol) {
        List<int[]> path = new ArrayList<>();
        int[] current = {endRow, endCol};
        
        while (current[0] != -1 && current[1] != -1) {
            path.add(current);
            int tempRow = current[0];
            int tempCol = current[1];
            current = new int[]{parent[tempRow][tempCol][0], parent[tempRow][tempCol][1]};
        }
        for (int i = path.size() - 1; i >= 0; i--) {
            int r = path.get(i)[0], c = path.get(i)[1];
            if (map[r][c] != Map.START && map[r][c] != Map.END) {
                solution[r][c] = PATH;
            }
        }
    }
    
    private void reconstructPathWithVisualization(int[][][] parent, int endRow, int endCol) {
        List<int[]> path = new ArrayList<>();
        int[] current = {endRow, endCol};
        
        while (current[0] != -1 && current[1] != -1) {
            path.add(current);
            int tempRow = current[0];
            int tempCol = current[1];
            current = new int[]{parent[tempRow][tempCol][0], parent[tempRow][tempCol][1]};
        }
        reconstructPath(parent, endRow, endCol);
        for (int i = path.size() - 1; i >= 0; i--) {
            int r = path.get(i)[0], c = path.get(i)[1];
            mapPanel.addStarPathPosition(r, c);
            mapPanel.repaint();
            if (!skipped) {
                delay(150);
            }
        }
    }
    
    private boolean isValid(int row, int col) {
        if (row < 0 || col < 0 || row >= size || col >= size) {
            return false;
        }
        return map[row][col] != Map.WALL;
    }
    
    private void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int[][] getSolutionPath() {
        return solution;
    }

    public int getStepsCount() {
        return ctr;
    }

    public double getTime() {
        return (endTime - startTime) / 1000.0;
    }

    private boolean isJinxBlock(int row, int col) {
        int[][] internalMap = mapPanel.getInternalMap();
        return row >= 0 && row < internalMap.length && 
               col >= 0 && col < internalMap[0].length && 
               internalMap[row][col] == 4;
    }
}