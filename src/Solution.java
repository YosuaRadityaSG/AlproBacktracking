import java.awt.image.BufferedImage;
import java.util.*;

public class Solution {
    // Private fields for maze data, visualization, and algorithm tracking
    private int[][] map;            // Internal maze structure (without border)
    private int[][] solution;       // Stores the solution path
    private Map mapPanel;           // Reference to visualization panel
    private BufferedImage starImage; // Image for path visualization
    private int size, ctr;          // Maze size and step counter
    private static final int PATH = 8; // Constant for marking the solution path
    // Direction arrays for 4-way movement (up, right, down, left)
    private static final int[] ROW_MOVES = {-1, 0, 1, 0};
    private static final int[] COL_MOVES = {0, 1, 0, -1};
    private long startTime, endTime; // Timing the solution

    // Constructor - initializes solution finder with map and visualization resources
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

    // Main solving method - starts timer and finds path from START position
    public boolean solveWithAnimation() {
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

    // Core pathfinding algorithm - implements Dijkstra's shortest path with visualization
    private boolean findPath(int startRow, int startCol) {
        // Initialize distance array (shortest distance from start to each cell)
        int[][] distance = new int[size][size];
        // Initialize visited array (tracks which cells have been fully processed)
        boolean[][] visited = new boolean[size][size];
        // Initialize parent pointers (to reconstruct path later)
        int[][][] parent = new int[size][size][2];
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                distance[i][j] = Integer.MAX_VALUE;
                parent[i][j][0] = -1;
                parent[i][j][1] = -1;
            }
        }
        // Set distance to start position as 0
        distance[startRow][startCol] = 0;
        boolean pathFound = false;
        
        // Main loop: while path not found, select unvisited cell with minimum distance
        while (!pathFound) {
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
                if (isJinxBlock) {
                    delay(2000);
                } else {
                    delay(150);
                }
            }
            if (map[row][col] == Map.END) {
                pathFound = true;
                reconstructPathWithVisualization(parent, row, col);
                return true;
            }
            if (map[row][col] == Map.PORTAL1 || map[row][col] == Map.PORTAL2) {
                handlePortal(row, col, distance, visited, parent);
                continue;
            }
            if (map[row][col] == Map.ANGIN) {
                continue;
            }
            // Process selected cell: check if end reached, handle special elements, explore neighbors
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
    
    // Handle teleportation between paired portals
    private void handlePortal(int row, int col, int[][] distance, boolean[][] visited, int[][][] parent) {
        // Find the matching portal in the maze
        int portalValue = mapPanel.getInternalMap()[row][col], otherPortalValue = (portalValue == 2) ? 3 : 2;
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if ((i != row || j != col) && mapPanel.getInternalMap()[i][j] == otherPortalValue && !visited[i][j]) {
                    int newDist = distance[row][col] + 1;
                    if (newDist < distance[i][j]) {
                        distance[i][j] = newDist;
                        parent[i][j][0] = row;
                        parent[i][j][1] = col;
                        
                        mapPanel.addStarPathPosition(i, j);
                        ctr++;
                        mapPanel.repaint();
                        delay(150);
                    }
                    return;
                }
            }
        }
    }
    
    // Build solution path from parent pointers (without visualization)
    private void reconstructPath(int[][][] parent, int endRow, int endCol) {
        // Create list to store path cells
        List<int[]> path = new ArrayList<>();
        // Start from end position and follow parent pointers to start
        int[] current = {endRow, endCol};
        
        while (current[0] != -1 && current[1] != -1) {
            path.add(current);
            int tempRow = current[0];
            int tempCol = current[1];
            current = new int[]{parent[tempRow][tempCol][0], parent[tempRow][tempCol][1]};
        }
        // Mark path cells in solution array
        for (int i = path.size() - 1; i >= 0; i--) {
            int r = path.get(i)[0], c = path.get(i)[1];
            if (map[r][c] != Map.START && map[r][c] != Map.END) {
                solution[r][c] = PATH;
            }
        }
    }
    
    // Build solution path and visualize it with animation
    private void reconstructPathWithVisualization(int[][][] parent, int endRow, int endCol) {
        // Create list to store path cells
        List<int[]> path = new ArrayList<>();
        // Start from end position and follow parent pointers to start
        int[] current = {endRow, endCol};
        
        while (current[0] != -1 && current[1] != -1) {
            path.add(current);
            int tempRow = current[0];
            int tempCol = current[1];
            current = new int[]{parent[tempRow][tempCol][0], parent[tempRow][tempCol][1]};
        }
        reconstructPath(parent, endRow, endCol);
        // Visualize path with stars moving from start to end
        for (int i = path.size() - 1; i >= 0; i--) {
            int r = path.get(i)[0], c = path.get(i)[1];
            mapPanel.addStarPathPosition(r, c);
            mapPanel.repaint();
            delay(150);
        }
    }
    
    // Check if a position is valid to move to
    private boolean isValid(int row, int col) {
        // Check boundaries and walls
        if (row < 0 || col < 0 || row >= size || col >= size) {
            return false;
        }
        return map[row][col] != Map.WALL;
    }
    
    // Pause execution for visualization
    private void delay(int ms) {
        // Sleep thread for specified milliseconds
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Accessor methods
    public int[][] getSolutionPath() {
        return solution;
    }

    public int getStepsCount() {
        return ctr;
    }

    public double getTime() {
        return (endTime - startTime) / 1000.0;
    }

    // Check if current position is a JinxBlock (adds time penalty)
    private boolean isJinxBlock(int row, int col) {
        int[][] internalMap = mapPanel.getInternalMap();
        return row >= 0 && row < internalMap.length && 
               col >= 0 && col < internalMap[0].length && 
               internalMap[row][col] == 4;
    }
}