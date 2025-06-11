// Mengimpor library yang diperlukan untuk program
import java.awt.image.BufferedImage;  // Untuk mengelola gambar bintang sebagai penanda jalur
import java.lang.reflect.Field;       // Untuk mengakses atribut private di kelas lain melalui reflection
import java.util.List;                // Untuk menyimpan daftar posisi bintang pada jalur
import java.util.Random;              // Untuk mengacak arah gerakan dalam mencari jalur
import java.util.Stack;               // Untuk melacak jalur yang sudah dikunjungi dan backtracking

/**
 * Kelas Backtracking mengimplementasikan algoritma backtracking untuk mencari jalur
 * dalam labirin dengan berbagai fitur khusus seperti portal, angin, dan jinxblock.
 */
public class Backtracking {
    // Variabel untuk menyimpan data peta dan solusi
    private int[][] map;         // Menyimpan struktur labirin (0=jalan, 1=dinding, dll)
    private int[][] solution;    // Menyimpan jalur solusi yang ditemukan
    private Map mapPanel;        // Referensi ke panel visual untuk menampilkan labirin
    private BufferedImage starImage;  // Gambar bintang untuk visualisasi jalur
    private int size, ctr;       // Ukuran labirin dan penghitung langkah solusi
    
    // Mendefinisikan konstanta dan array untuk navigasi dalam labirin
    private static final int PATH = 8;  // Nilai untuk menandai sel yang menjadi bagian jalur solusi
    private static final int[] ROW_MOVES = {-1, 0, 1, 0}; // Pergeseran baris: Atas, kanan, bawah, kiri
    private static final int[] COL_MOVES = {0, 1, 0, -1}; // Pergeseran kolom: Atas, kanan, bawah, kiri
    
    // Variabel untuk pelacakan jalur dan pengacakan gerakan
    private Stack<int[]> pathStack = new Stack<>();  // Stack untuk menyimpan jalur saat ini dan backtracking
    private Random random = new Random();  // Objek untuk mengacak arah pencarian

    /**
     * Konstruktor untuk kelas Backtracking
     * @param mapPanel Panel yang menampilkan labirin
     * @param starImage Gambar bintang untuk visualisasi jalur
     */
    public Backtracking(Map mapPanel, BufferedImage starImage) {
        // Menyimpan referensi ke panel peta dan gambar bintang
        this.mapPanel = mapPanel;  // Menyimpan referensi ke panel peta untuk visualisasi
        this.starImage = starImage;  // Menyimpan gambar bintang untuk menandai jalur
        
        // Mengambil data labirin lengkap dari panel peta
        int[][] fullMap = mapPanel.getMapGenerator();  // Mendapatkan data labirin dari panel Map

        // Menginisialisasi array peta dan solusi (tidak termasuk batas luar)
        this.size = fullMap.length - 2;  // Ukuran peta internal (tanpa batas luar)
        this.map = new int[size][size];  // Membuat array untuk menyimpan struktur labirin
        this.solution = new int[size][size];  // Membuat array untuk menyimpan jalur solusi
        
        // Menyalin data labirin dari fullMap ke map internal (menghilangkan batas)
        for (int i = 0; i < size; i++) {  // Loop untuk setiap baris
            for (int j = 0; j < size; j++) {  // Loop untuk setiap kolom
                // Menyalin nilai sel dengan offset +1 untuk menghilangkan batas
                this.map[i][j] = fullMap[i + 1][j + 1];  // Menyalin nilai sel ke map
                this.solution[i][j] = fullMap[i + 1][j + 1];  // Menyalin nilai sel ke solution
            }
        }
        
        // Tandai elemen khusus dalam peta
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
        
        // Bersihkan visualisasi jalur yang ada
        mapPanel.clearStarPathPositions();
    }

    // Metode penyelesaian utama yang dimulai dari posisi awal
    public boolean solveWithAnimation() {
        boolean result = false;

        // Temukan posisi awal dan mulai pencarian rekursif
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

    // Pencarian depth-first rekursif dengan backtracking
    private boolean findPath(int row, int col) {
        // Periksa apakah posisi saat ini valid
        if (!isValid(row, col)) {
            return false;
        }
        
        // Periksa JinxBlock khusus yang menambahkan penalti waktu
        boolean isJinxBlock = isJinxBlock(row, col);
        
        // Periksa apakah kita mencapai akhir
        if (map[row][col] == Map.END) {
            mapPanel.addStarPathPosition(row, col);
            pathStack.push(new int[]{row, col});
            ctr++;
            return true;
        }
        
        // Tandai sel saat ini sebagai bagian dari jalur
        if (map[row][col] != Map.START) {
            solution[row][col] = PATH;
        }
        
        // Visualisasikan posisi saat ini dalam jalur
        mapPanel.addStarPathPosition(row, col);
        pathStack.push(new int[]{row, col});
        ctr++;
        mapPanel.repaint();
        
        // Tambahkan penundaan untuk visualisasi (lebih lama untuk JinxBlock)
        try {
            if (isJinxBlock) {
                Thread.sleep(2000);
            } else {
                Thread.sleep(150);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Tangani elemen angin yang mendorong kembali
        if (map[row][col] == Map.ANGIN) {
            removeLastNStarPositions(2);
            if (!pathStack.isEmpty()) pathStack.pop();
            mapPanel.repaint();
            return false;
        }
        
        // Tangani teleportasi portal
        if (map[row][col] == Map.PORTAL1 || map[row][col] == Map.PORTAL2) {
            int[][] internalMap = mapPanel.getInternalMap();
            int otherPortalValue = (internalMap[row][col] == 2) ? 3 : 2;
        
            // Temukan portal lainnya
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if ((i != row || j != col) && internalMap[i][j] == otherPortalValue) {
                        // Teleport ke portal lainnya
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
                        
                        // Coba semua arah dari posisi portal baru
                        int[] randomDirection = getRandomDirection();
                        for (int dir : randomDirection) {
                            int newRow = i + ROW_MOVES[dir];
                            int newCol = j + COL_MOVES[dir];
                            
                            if (findPath(newRow, newCol)) {
                                return true;
                            }
                        }
                        
                        // Jika tidak ada jalur ditemukan dari tujuan teleport, mundur
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
        
        // Coba keempat arah dalam urutan acak
        int[] randomDirections = getRandomDirection();
        for (int dir : randomDirections) {
            int newRow = row + ROW_MOVES[dir], newCol = col + COL_MOVES[dir];
            
            // Periksa apakah posisi sudah ada di jalur saat ini
            boolean alreadyVisited = false;
            for (int[] pos : pathStack) {
                if (pos[0] == newRow && pos[1] == newCol) {
                    alreadyVisited = true;
                    break;
                }
            }
            
            // Jika belum dikunjungi dan jalur ditemukan, kembalikan sukses
            if (!alreadyVisited && findPath(newRow, newCol)) {
                return true;
            }
        }
        
        // Jika tidak ada jalur ditemukan di arah mana pun, mundur
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
    
    // Mengacak urutan arah untuk variasi
    private int[] getRandomDirection() {
        int[] directions = {0, 1, 2, 3};  // Atas, kanan, bawah, kiri
        
        // Pengacakan Fisher-Yates
        for (int i = directions.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1), temp = directions[i];

            directions[i] = directions[j];
            directions[j] = temp;
        }
        return directions;
    }
    
    // Menghapus bintang dari visualisasi saat backtracking
    private void removeLastNStarPositions(int n) {
        try {
            // Gunakan refleksi untuk mengakses field private di kelas Map
            Field field = mapPanel.getClass().getDeclaredField("starPathPositions");

            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<int[]> starPathPositions = (List<int[]>) field.get(mapPanel);
            
            // Hapus n bintang terakhir
            for (int i = 0; i < n && !starPathPositions.isEmpty(); i++) {
                starPathPositions.remove(starPathPositions.size() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Periksa apakah posisi valid untuk digerakkan
    private boolean isValid(int row, int col) {
        // Periksa batas
        if (row < 0 || col < 0 || row >= size || col >= size) {
            return false;
        }
        // Periksa untuk dinding
        if (map[row][col] == Map.WALL) {
            return false;
        }
        // Periksa untuk sel yang sudah dikunjungi (kecuali titik akhir)
        for (int[] pos : pathStack) {
            if (pos[0] == row && pos[1] == col && !(map[row][col] == Map.END)) {
                return false;
            }
        }
        return true;
    }

    // Periksa apakah sel saat ini adalah JinxBlock
    private boolean isJinxBlock(int row, int col) {
        int[][] internalMap = mapPanel.getInternalMap();
        return row >= 0 && row < internalMap.length && col >= 0 && col < internalMap[0].length && 
               internalMap[row][col] == 4;
    }

    // Metode aksesor
    public int[][] getSolutionPath() {
        return solution;
    }

    public int getStepsCount() {
        return ctr;
    }
}

// 1. Menggunakan stack untuk melacak jalur saat ini
// 2. Menjelajahi jalur yang mungkin dengan pemilihan arah acak
// 3. Menangani elemen labirin khusus:
//    - Portal: teleport ke lokasi lain
//    - Angin: mendorong kembali dari jalur
//    - JinxBlock: menambahkan penalti waktu
// 4. Memvisualisasikan proses pencarian dengan menambahkan gambar bintang
// 5. Mengimplementasikan backtracking saat mencapai jalan buntu
// 6. Mengembalikan true saat menemukan titik akhir
