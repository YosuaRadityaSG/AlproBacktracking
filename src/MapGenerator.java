import java.util.Random;

/**
 * MapGenerator is a class that generates a random maze of a given size.
 * The maze is represented as a 2D array of integers, where 0 represents an empty space,
 * 1 represents a wall, 2 represents the start position, and 3 represents the end position.
 */
public class MapGenerator {
    private int size;
    private Random random;
    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int START = 2;
    public static final int END = 3;
    
    /**
     * Constructs a MapGenerator with the specified size.
     * @param size the size of the maze
     */
    public MapGenerator(int size) {
        this.size = size;
        this.random = new Random();
    }
    
    /**
     * Generates a random maze.
     * @return a 2D array representing the maze
     */
    public int[][] generateMap() {
        int[][] map = new int[size+2][size+2];
        
        // 1. Generates a maze of specified size with borders
        // 2. Places walls randomly throughout the maze (30% probability)
        // 3. Sets the start position at the bottom left
        // 4. Sets the end position at the top right
        // 5. Ensures paths around start and end positions are clear
        for (int i = 0; i < size+2; i++) {
            for (int j = 0; j < size+2; j++) {
                if (i == 0 || j == 0 || i == size+1 || j == size+1) {
                    map[i][j] = WALL;
                } 
                else {
                    map[i][j] = (random.nextDouble() < 0.3) ? WALL : EMPTY;
                }
            }
        }
        map[size][1] = START;
        map[1][size] = END;
        if (size > 1) {
            map[size-1][1] = EMPTY;
            map[size][2] = EMPTY;
            map[2][size] = EMPTY;
            map[1][size-1] = EMPTY;
        }
        return map;
    }
}
