import java.util.Random;

/**
 * MapGenerator adalah kelas yang menghasilkan labirin acak dengan ukuran tertentu.
 * Labirin direpresentasikan sebagai array 2D integer, dimana 0 mewakili ruang kosong,
 * 1 mewakili dinding, 2 mewakili posisi awal, dan 3 mewakili posisi akhir.
 */
public class MapGenerator {
    private int size;
    private Random random;
    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int START = 2;
    public static final int END = 3;
    
    /**
     * Membangun MapGenerator dengan ukuran yang ditentukan.
     * @param size ukuran labirin
     */
    public MapGenerator(int size) {
        this.size = size;
        this.random = new Random();
    }
    
    /**
     * Menghasilkan labirin acak.
     * @return array 2D yang mewakili labirin
     */
    public int[][] generateMap() {
        int[][] map = new int[size+2][size+2];
        
        // 1. Menghasilkan labirin dengan ukuran yang ditentukan dengan batas
        // 2. Menempatkan dinding secara acak di seluruh labirin (30% probabilitas)
        // 3. Mengatur posisi awal di kiri bawah
        // 4. Mengatur posisi akhir di kanan atas
        // 5. Memastikan jalur di sekitar posisi awal dan akhir bersih
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
