import java.awt.image.BufferedImage;
import java.util.*;

public class Solution {
    // Field privat untuk data labirin, visualisasi, dan pelacakan algoritma
    private int[][] map;            // Struktur labirin internal (tanpa batas)
    private int[][] solution;       // Menyimpan jalur solusi
    private Map mapPanel;           // Referensi ke panel visualisasi
    private BufferedImage starImage; // Gambar untuk visualisasi jalur
    private int size, ctr;          // Ukuran labirin dan penghitung langkah
    private static final int PATH = 8; // Konstanta untuk menandai jalur solusi
    // Array arah untuk pergerakan 4-arah (atas, kanan, bawah, kiri)
    private static final int[] ROW_MOVES = {-1, 0, 1, 0};
    private static final int[] COL_MOVES = {0, 1, 0, -1};
    private long startTime, endTime; // Waktu eksekusi solusi

    // Konstruktor - menginisialisasi pencari solusi dengan peta dan sumber daya visualisasi
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

    // Metode utama untuk menyelesaikan - memulai timer dan mencari jalur dari posisi AWAL
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

    // Algoritma pencari jalur inti - mengimplementasikan jalur terpendek Dijkstra dengan visualisasi
    private boolean findPath(int startRow, int startCol) {
        // Menginisialisasi array jarak (jarak terpendek dari awal ke setiap sel)
        int[][] distance = new int[size][size];
        // Menginisialisasi array yang dikunjungi (melacak sel mana yang telah diproses sepenuhnya)
        boolean[][] visited = new boolean[size][size];
        // Menginisialisasi penunjuk induk (untuk merekonstruksi jalur nanti)
        int[][][] parent = new int[size][size][2];
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                distance[i][j] = Integer.MAX_VALUE;
                parent[i][j][0] = -1;
                parent[i][j][1] = -1;
            }
        }
        // Mengatur jarak ke posisi awal sebagai 0
        distance[startRow][startCol] = 0;
        boolean pathFound = false;
        
        // Loop utama: selama jalur belum ditemukan, pilih sel yang belum dikunjungi dengan jarak minimum
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
            // Proses sel yang dipilih: periksa apakah akhir telah dicapai, tangani elemen khusus, jelajahi tetangga
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
    
    // Menangani teleportasi antara portal berpasangan
    private void handlePortal(int row, int col, int[][] distance, boolean[][] visited, int[][][] parent) {
        // Mencari portal yang cocok di labirin
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
    
    // Membangun jalur solusi dari penunjuk induk (tanpa visualisasi)
    private void reconstructPath(int[][][] parent, int endRow, int endCol) {
        // Membuat daftar untuk menyimpan sel jalur
        List<int[]> path = new ArrayList<>();
        // Mulai dari posisi akhir dan ikuti penunjuk induk ke awal
        int[] current = {endRow, endCol};
        
        while (current[0] != -1 && current[1] != -1) {
            path.add(current);
            int tempRow = current[0];
            int tempCol = current[1];
            current = new int[]{parent[tempRow][tempCol][0], parent[tempRow][tempCol][1]};
        }
        // Tandai sel jalur di array solusi
        for (int i = path.size() - 1; i >= 0; i--) {
            int r = path.get(i)[0], c = path.get(i)[1];
            if (map[r][c] != Map.START && map[r][c] != Map.END) {
                solution[r][c] = PATH;
            }
        }
    }
    
    // Membangun jalur solusi dan memvisualisasikannya dengan animasi
    private void reconstructPathWithVisualization(int[][][] parent, int endRow, int endCol) {
        // Membuat daftar untuk menyimpan sel jalur
        List<int[]> path = new ArrayList<>();
        // Mulai dari posisi akhir dan ikuti penunjuk induk ke awal
        int[] current = {endRow, endCol};
        
        while (current[0] != -1 && current[1] != -1) {
            path.add(current);
            int tempRow = current[0];
            int tempCol = current[1];
            current = new int[]{parent[tempRow][tempCol][0], parent[tempRow][tempCol][1]};
        }
        reconstructPath(parent, endRow, endCol);
        // Visualisasikan jalur dengan bintang bergerak dari awal ke akhir
        for (int i = path.size() - 1; i >= 0; i--) {
            int r = path.get(i)[0], c = path.get(i)[1];
            mapPanel.addStarPathPosition(r, c);
            mapPanel.repaint();
            delay(150);
        }
    }
    
    // Memeriksa apakah posisi valid untuk digerakkan
    private boolean isValid(int row, int col) {
        // Memeriksa batas dan dinding
        if (row < 0 || col < 0 || row >= size || col >= size) {
            return false;
        }
        return map[row][col] != Map.WALL;
    }
    
    // Menjeda eksekusi untuk visualisasi
    private void delay(int ms) {
        // Menjeda thread untuk milidetik yang ditentukan
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Metode aksesor
    public int[][] getSolutionPath() {
        return solution;
    }

    public int getStepsCount() {
        return ctr;
    }

    public double getTime() {
        return (endTime - startTime) / 1000.0;
    }

    // Memeriksa apakah posisi saat ini adalah JinxBlock (menambah penalti waktu)
    private boolean isJinxBlock(int row, int col) {
        int[][] internalMap = mapPanel.getInternalMap();
        return row >= 0 && row < internalMap.length && 
               col >= 0 && col < internalMap[0].length && 
               internalMap[row][col] == 4;
    }
}