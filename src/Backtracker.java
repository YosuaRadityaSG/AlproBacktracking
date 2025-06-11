public class Backtracker {
    // Field privat untuk data labirin dan pelacakan solusi
    private int[][] map;         // Struktur labirin asli
    private int[][] solution;    // Jalur solusi yang sedang dibangun
    private int size;            // Ukuran labirin
    private static final int PATH = 4; // Konstanta untuk menandai jalur
    // Array arah untuk pergerakan 4-arah (atas, kanan, bawah, kiri)
    private static final int[] ROW_MOVES = {-1, 0, 1, 0};
    private static final int[] COL_MOVES = {0, 1, 0, -1};
    
    // Konstruktor - menyalin data labirin dan menginisialisasi array solusi
    public Backtracker(int[][] map) {
        this.map = map;
        this.size = map.length;
        this.solution = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                solution[i][j] = map[i][j];
            }
        }
    }
    
    // Metode penyelesaian utama - menemukan posisi AWAL dan memulai pencarian rekursif
    public boolean solve() {
        // Temukan posisi awal
        // Mulai pencarian depth-first dari awal
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == MapGenerator.START) {
                    return findPath(i, j);
                }
            }
        }
        return false;
    }
    
    // Pencarian depth-first rekursif dengan backtracking
    private boolean findPath(int row, int col) {
        // Periksa apakah posisi valid
        if (!isValid(row, col)) {
            return false;
        }
        // Periksa apakah mencapai posisi AKHIR
        if (map[row][col] == MapGenerator.END) {
            return true;
        }
        // Tandai posisi saat ini sebagai bagian dari jalur
        if (map[row][col] != MapGenerator.START) {
            solution[row][col] = PATH;
        }
        // Coba keempat arah (atas, kanan, bawah, kiri)
        for (int i = 0; i < 4; i++) {
            int newRow = row + ROW_MOVES[i], newCol = col + COL_MOVES[i];

            if (findPath(newRow, newCol)) {
                return true;
            }
        }
        // Jika tidak ada solusi ditemukan di arah mana pun, mundur dengan menandai posisi saat ini
        if (map[row][col] != MapGenerator.START) {
            solution[row][col] = MapGenerator.EMPTY;
        }
        return false;
    }
    
    // Periksa apakah posisi valid untuk dikunjungi
    private boolean isValid(int row, int col) {
        // Periksa batas
        if (row < 0 || col < 0 || row >= size || col >= size) {
            return false;
        }
        // Periksa untuk dinding dan sel yang sudah dikunjungi
        if (map[row][col] == MapGenerator.WALL || solution[row][col] == PATH) {
            return false;
        }
        return true;
    }
    
    // Mengembalikan jalur solusi
    public int[][] getSolutionPath() {
        return solution;
    }
}
