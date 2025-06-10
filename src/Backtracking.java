import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class Backtracking {
    // Map data structures
    private int[][] map;         // The maze structure
    private int[][] solution;    // The solution path
    private Map mapPanel;        // Reference to visual panel
    private BufferedImage starImage;  // For path visualization
    private int size, ctr;       // Size of maze and step counter
    
    // Constants and direction arrays
    private static final int PATH = 8;  // Value to mark path cells
    private static final int[] ROW_MOVES = {-1, 0, 1, 0}; // Up, right, down, left (row)
    private static final int[] COL_MOVES = {0, 1, 0, -1}; // Up, right, down, left (column)
    
    // Path tracking and randomization
    private Stack<int[]> pathStack = new Stack<>();  // Stack for backtracking
    private Random random = new Random();  // For randomizing direction

    // Constructor initializes the solver with map panel and star image
    public Backtracking(Map mapPanel, BufferedImage starImage) {
        this.mapPanel = mapPanel;
        this.starImage = starImage;
        
        // Extract maze data from the map panel
        int[][] fullMap = mapPanel.getMapGenerator();

        // Initialize internal map array (excluding border)
        this.size = fullMap.length - 2;
        this.map = new int[size][size];
        this.solution = new int[size][size];
        
        // Copy maze cells excluding the border
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.map[i][j] = fullMap[i + 1][j + 1];
                this.solution[i][j] = fullMap[i + 1][j + 1];
            }
        }
        
        // Mark special elements in the map
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
        
        // Clear any existing path visualization
        mapPanel.clearStarPathPositions();
    }

    // Main solving method that starts from the start position
    public boolean solveWithAnimation() {
        boolean result = false;

        // Find start position and begin recursive search
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

    // Recursive depth-first search with backtracking
    private boolean findPath(int row, int col) {
        // Check if current position is valid
        if (!isValid(row, col)) {
            return false;
        }
        
        // Check for special JinxBlock that adds time penalty
        boolean isJinxBlock = isJinxBlock(row, col);
        
        // Check if we reached the end
        if (map[row][col] == Map.END) {
            mapPanel.addStarPathPosition(row, col);
            pathStack.push(new int[]{row, col});
            ctr++;
            return true;
        }
        
        // Mark current cell as part of path
        if (map[row][col] != Map.START) {
            solution[row][col] = PATH;
        }
        
        // Visualize current position in path
        mapPanel.addStarPathPosition(row, col);
        pathStack.push(new int[]{row, col});
        ctr++;
        mapPanel.repaint();
        
        // Add delay for visualization (longer for JinxBlock)
        try {
            if (isJinxBlock) {
                Thread.sleep(2000);
            } else {
                Thread.sleep(150);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Handle wind element which pushes back
        if (map[row][col] == Map.ANGIN) {
            removeLastNStarPositions(2);
            if (!pathStack.isEmpty()) pathStack.pop();
            mapPanel.repaint();
            return false;
        }
        
        // Handle portal teleportation
        if (map[row][col] == Map.PORTAL1 || map[row][col] == Map.PORTAL2) {
            int[][] internalMap = mapPanel.getInternalMap();
            int otherPortalValue = (internalMap[row][col] == 2) ? 3 : 2;
        
            // Find other portal
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if ((i != row || j != col) && internalMap[i][j] == otherPortalValue) {
                        // Teleport to the other portal
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
                        
                        // Try all directions from new portal position
                        int[] randomDirection = getRandomDirection();
                        for (int dir : randomDirection) {
                            int newRow = i + ROW_MOVES[dir];
                            int newCol = j + COL_MOVES[dir];
                            
                            if (findPath(newRow, newCol)) {
                                return true;
                            }
                        }
                        
                        // If no path found from teleport destination, backtrack
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
        
        // Try all four directions in random order
        int[] randomDirections = getRandomDirection();
        for (int dir : randomDirections) {
            int newRow = row + ROW_MOVES[dir], newCol = col + COL_MOVES[dir];
            
            // Check if position is already in current path
            boolean alreadyVisited = false;
            for (int[] pos : pathStack) {
                if (pos[0] == newRow && pos[1] == newCol) {
                    alreadyVisited = true;
                    break;
                }
            }
            
            // If not visited and path found, return success
            if (!alreadyVisited && findPath(newRow, newCol)) {
                return true;
            }
        }
        
        // If no path found in any direction, backtrack
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
    
    // Randomize direction order for variety
    private int[] getRandomDirection() {
        int[] directions = {0, 1, 2, 3};  // Up, right, down, left
        
        // Fisher-Yates shuffle
        for (int i = directions.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1), temp = directions[i];

            directions[i] = directions[j];
            directions[j] = temp;
        }
        return directions;
    }
    
    // Remove stars from visualization when backtracking
    private void removeLastNStarPositions(int n) {
        try {
            // Use reflection to access private field in Map class
            Field field = mapPanel.getClass().getDeclaredField("starPathPositions");

            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<int[]> starPathPositions = (List<int[]>) field.get(mapPanel);
            
            // Remove the last n stars
            for (int i = 0; i < n && !starPathPositions.isEmpty(); i++) {
                starPathPositions.remove(starPathPositions.size() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Check if a position is valid to move to
    private boolean isValid(int row, int col) {
        // Check bounds
        if (row < 0 || col < 0 || row >= size || col >= size) {
            return false;
        }
        // Check for wall
        if (map[row][col] == Map.WALL) {
            return false;
        }
        // Check for already visited cell (except endpoint)
        for (int[] pos : pathStack) {
            if (pos[0] == row && pos[1] == col && !(map[row][col] == Map.END)) {
                return false;
            }
        }
        return true;
    }

    // Check if current cell is a JinxBlock
    private boolean isJinxBlock(int row, int col) {
        int[][] internalMap = mapPanel.getInternalMap();
        return row >= 0 && row < internalMap.length && 
               col >= 0 && col < internalMap[0].length && 
               internalMap[row][col] == 4;
    }

    // Accessor methods
    public int[][] getSolutionPath() {
        return solution;
    }

    public int getStepsCount() {
        return ctr;
    }
}

// 1. Uses a stack to keep track of the current path
// 2. Explores possible paths with randomized direction selection
// 3. Handles special maze elements:
//    - Portals: teleports to another location
//    - Wind: pushes back from the path
//    - JinxBlock: adds time penalty
// 4. Visualizes the search process by adding star images
// 5. Implements backtracking when reaching dead ends
// 6. Returns true when finding the end point
