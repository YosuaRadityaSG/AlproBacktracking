import java.util.Random;

public class MapGenerator {
    private int size;
    private double wallDensity;
    private Random random;
    
    // Constants for map values
    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int START = 2;
    public static final int END = 3;
    
    public MapGenerator(int size) {
        this(size, 0.3); // Default wall density of 30%
    }
    
    public MapGenerator(int size, double wallDensity) {
        this.size = size;
        this.wallDensity = wallDensity;
        this.random = new Random();
    }
    
    public int[][] generateMap() {
        // Create a map with borders (size+2 x size+2)
        int[][] map = new int[size+2][size+2];
        
        // First step: Build the borders and fill the inner area
        for (int i = 0; i < size+2; i++) {
            for (int j = 0; j < size+2; j++) {
                // Set border walls
                if (i == 0 || j == 0 || i == size+1 || j == size+1) {
                    map[i][j] = WALL;
                } 
                // Fill internal area with random content
                else {
                    map[i][j] = (random.nextDouble() < wallDensity) ? WALL : EMPTY;
                }
            }
        }
        
        // Second step: Place start and end positions
        // Set start position (bottom-left)
        map[size][1] = START;
        
        // Set end position (top-right)
        map[1][size] = END;
        
        // Third step: Ensure start and end are accessible
        if (size > 1) {
            // Clear paths around start
            map[size-1][1] = EMPTY; // Above start
            map[size][2] = EMPTY;   // Right of start
            
            // Clear paths around end
            map[2][size] = EMPTY;   // Below end
            map[1][size-1] = EMPTY; // Left of end
        }
        
        return map;
    }
}
