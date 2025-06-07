import java.util.Random;

public class MapGenerator {
    private int size;
    private Random random;
    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int START = 2;
    public static final int END = 3;
    
    public MapGenerator(int size) {
        this.size = size;
        this.random = new Random();
    }
    
    public int[][] generateMap() {
        int[][] map = new int[size+2][size+2];
        
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
