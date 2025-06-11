import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Map extends JPanel{
    private int size;  // Dimensi labirin
    // Gambar untuk berbagai elemen labirin
    private BufferedImage mapImage, windImage, portal1Image, portal2Image, jinxBlockImage, starImage, wallImage;
    private int[][] map;  // Peta internal untuk elemen khusus
    private int[][] mapGenerator;  // Struktur labirin dasar
    private Random random = new Random();
    // Konstanta untuk jenis sel yang berbeda dalam labirin
    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int START = 2;
    public static final int END = 3;
    public static final int ANGIN = 4;
    public static final int PORTAL1 = 5;
    public static final int PORTAL2 = 6;
    public static final int JINXBLOCK = 7;
    private List<int[]> starPathPositions = new ArrayList<>();  // Posisi bintang untuk visualisasi

    // Konstruktor menginisialisasi peta dengan ukuran tertentu dan seed acak
    public Map(int size, long seed) {
        this.size = size;
        this.random = new Random(seed);
        
        // Memuat semua aset gambar untuk elemen labirin
        try{
            windImage = ImageIO.read(new File("Aplro/Asset/Angin.png"));
            portal1Image = ImageIO.read(new File("Aplro/Asset/Portal1.png"));
            portal2Image = ImageIO.read(new File("Aplro/Asset/Portal2.png"));
            jinxBlockImage = ImageIO.read(new File("Aplro/Asset/JinxBlock.png"));
            starImage = ImageIO.read(new File("Aplro/Asset/Bintang.png"));
            wallImage = ImageIO.read(new File("Aplro/Asset/Wall.png"));
        }catch(IOException e){
            System.out.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
        }
        loadImage(size);
        mapGenerator = mapGenerator();
        randomAssetsGenerator();
        repaint();
    }

    // Memuat gambar peta latar belakang berdasarkan ukuran
    private void loadImage(int size) {
        try {
            mapImage = ImageIO.read(new File("Aplro/Asset/map_" + size + "x" + size + "_noborder.png"));
        } catch (IOException e) {
            System.out.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Menempatkan elemen khusus secara acak dalam labirin
    public void randomAssetsGenerator(){
        // Menginisialisasi peta internal untuk elemen khusus
        map = new int[size][size];
        List<Integer> positions = new ArrayList<>();
        
        // Mengumpulkan posisi yang valid untuk elemen khusus (sel kosong tidak dekat awal/akhir)
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int mapRow = i + 1, mapCol = j + 1, cellValue = mapGenerator[mapRow][mapCol];
                boolean isSpecialEmptyPos = (mapRow == size - 1 && mapCol == 1) || (mapRow == size && mapCol == 2) || (mapRow == 2 && mapCol == size) || (mapRow == 1 && mapCol == size - 1);

                if (cellValue == EMPTY && !isSpecialEmptyPos) {
                    positions.add(i * size + j);
                }
            }
        }
        int[] assets;
        
        // Memilih elemen mana yang akan ditempatkan berdasarkan ukuran labirin
        if (size == 3) {
            assets = new int[] {1, 4};  // Angin dan JinxBlock untuk labirin kecil
        } else {
            assets = new int[] {1, 2, 3, 4};  // Semua elemen khusus untuk labirin yang lebih besar
        }

        // Menempatkan setiap elemen khusus pada posisi acak yang valid
        for (int assetType : assets) {
            if (positions.isEmpty()) {
                break;
            }
            int index = random.nextInt(positions.size()), pos = positions.get(index);
            positions.remove(index);
            int row = pos / size, col = pos % size;

            map[row][col] = assetType;
        }
    }

    // Menghasilkan struktur labirin dasar dengan dinding
    public int[][] mapGenerator(){
        int[][] data = new int[size + 2][size + 2];

        // Membuat dinding di sekitar batas
        for (int i = 0; i < size + 2; i++) {
            for (int j = 0; j < size + 2; j++) {
                if (i == 0 || j == 0 || i == size + 1 || j == size + 1) {
                    data[i][j] = WALL;
                } else {
                    data[i][j] = (random.nextDouble() < 0.1) ? WALL : EMPTY;
                }
            }
        }
        // Mengatur posisi awal dan akhir
        data[size][1] = START;
        data[1][size] = END;
        if (size > 1) {
            // Memastikan jalur di sekitar awal/akhir bersih
            data[size - 1][1] = EMPTY;
            data[size][2] = EMPTY;
            data[2][size] = EMPTY;
            data[1][size - 1] = EMPTY;
        }
        return data;
    }

    // Metode aksesor untuk kelas lain
    public int[][] getMapGenerator() {
        return mapGenerator;
    }

     public BufferedImage getStarImage() {
        return starImage;
    }

    // Menambahkan bintang untuk memvisualisasikan jalur saat ini
    public void addStarPathPosition(int row, int col) {
        // Memeriksa apakah posisi sudah memiliki bintang
        for (int[] pos : starPathPositions) {
            if (pos[0] == row && pos[1] == col) {
                return;
            }
        }
        // Menambahkan posisi baru dan memicu repaint
        starPathPositions.add(new int[]{row, col});
        repaint();
    }

    // Menghapus semua posisi bintang untuk visualisasi baru
    public void clearStarPathPositions() {
        starPathPositions.clear();
        repaint();
    }

    public int[][] getInternalMap() {
        return map;
    }

    public List<int[]> getStarPathPositions() {
        return new ArrayList<>(starPathPositions);
    }

    // Mengatur ulang visualisasi jalur
    public void clearPath() {
        getStarPathPositions().clear();
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 4) {
                    map[i][j] = 0;
                }
            }
        }
        repaint();
    }

    // Menggambar labirin dan semua elemennya
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (mapImage == null) {
            return;
        }
        
        // Menghitung posisi untuk memusatkan labirin
        int x = (getWidth() - mapImage.getWidth(this)) / 2;
        int y = (getHeight() - mapImage.getHeight(this)) / 2;
        
        // Menggambar gambar peta dasar
        g.drawImage(mapImage, x, y, this);
        
        // Menginisialisasi peta jika diperlukan
        if (map == null) {
            randomAssetsGenerator();
        }
        
        // Menghitung ukuran sel untuk penskalaan yang tepat
        int cellSize = mapImage.getWidth() / size;

        // Menggambar dinding dan elemen khusus
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (mapGenerator[row + 1][col + 1] == WALL) {
                    if (wallImage != null) {
                        g.drawImage(wallImage, x + col * cellSize, y + row * cellSize, cellSize, cellSize, this);
                    }
                } else if (mapGenerator[row + 1][col + 1] == EMPTY) {
                    BufferedImage assetImage = null;
                    switch (map[row][col]) {
                        case 1:
                            assetImage = windImage;
                            break;
                        case 2:
                            assetImage = portal1Image;
                            break;
                        case 3:
                            assetImage = portal2Image;
                            break;
                        case 4:
                            assetImage = jinxBlockImage;
                            break;
                    }
                    if (assetImage != null) {
                        g.drawImage(assetImage, x + col * cellSize, y + row * cellSize, cellSize, cellSize, this);
                    }
                }
                // Menggambar bintang yang menunjukkan jalur
                for (int[] pos : starPathPositions) {
                    int rowStar = pos[0], colStar = pos[1];

                    g.drawImage(starImage, x + colStar * cellSize, y + rowStar * cellSize, cellSize, cellSize, this);
                }
            }
        }
    }
}

// 1. Memuat dan mengelola gambar labirin dan gambar aset khusus
// 2. Mendefinisikan konstanta untuk elemen labirin (EMPTY, WALL, START, END, dll.)
// 3. Menghasilkan grid labirin dengan dinding batas
// 4. Menempatkan elemen khusus secara acak (angin, portal, jinx block)
// 5. Melacak posisi elemen jalur (bintang) untuk memvisualisasikan solusi
// 6. Menangani penggambaran labirin dan semua elemen di layar
// 7. Menyediakan metode agar kelas lain dapat berinteraksi dengan data labirin
