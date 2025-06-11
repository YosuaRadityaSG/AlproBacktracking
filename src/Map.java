import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Map extends JPanel {
    private int size;
    private BufferedImage mapImage, windImage, portal1Image, portal2Image, jinxBlockImage, starImage, wallImage;
    private int[][] map;
    private int[][] mapGenerator;
    private Random random = new Random();
    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int START = 2;
    public static final int END = 3;
    public static final int ANGIN = 4;
    public static final int PORTAL1 = 5;
    public static final int PORTAL2 = 6;
    public static final int JINXBLOCK = 7;
    private List<int[]> starPathPositions = new ArrayList<>();
    
    // Dark theme colors
    private Color startColor = new Color(76, 175, 80, 180); // Green
    private Color endColor = new Color(244, 67, 54, 180); // Red
    private Color emptyColor = new Color(30, 30, 30); // Dark gray for empty cells
    private Color wallColor = new Color(60, 60, 60); // Slightly lighter gray for walls
    private Color gridColor = new Color(80, 80, 80); // Grid lines

    public Map(int size, long seed) {
        this.size = size;
        this.random = new Random(seed);
        
        try {
            windImage = ImageIO.read(new File("Asset/Angin.png"));
            portal1Image = ImageIO.read(new File("Asset/Portal1.png"));
            portal2Image = ImageIO.read(new File("Asset/Portal2.png"));
            jinxBlockImage = ImageIO.read(new File("Asset/JinxBlock.png"));
            starImage = ImageIO.read(new File("Asset/Bintang.png"));
            wallImage = ImageIO.read(new File("Asset/Wall.png"));
        } catch (IOException e) {
            System.out.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Generate map directly instead of loading image
        mapGenerator = mapGenerator();
        randomAssetsGenerator();
        repaint();
    }

    public void randomAssetsGenerator(){
        map = new int[size][size];
        List<Integer> positions = new ArrayList<>();
        
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
        
        if (size == 3) {
            assets = new int[] {1, 4};
        } else {
            assets = new int[] {1, 2, 3, 4};
        }

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

    public int[][] mapGenerator(){
        int[][] data = new int[size + 2][size + 2];

        for (int i = 0; i < size + 2; i++) {
            for (int j = 0; j < size + 2; j++) {
                if (i == 0 || j == 0 || i == size + 1 || j == size + 1) {
                    data[i][j] = WALL;
                } else {
                    data[i][j] = (random.nextDouble() < 0.2) ? WALL : EMPTY;
                }
            }
        }
        data[size][1] = START;
        data[1][size] = END;
        if (size > 1) {
            data[size - 1][1] = EMPTY;
            data[size][2] = EMPTY;
            data[2][size] = EMPTY;
            data[1][size - 1] = EMPTY;
        }
        return data;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int cellSize = Math.min(getWidth(), getHeight()) / (size + 2);
        int x = (getWidth() - cellSize * (size + 2)) / 2;
        int y = (getHeight() - cellSize * (size + 2)) / 2;
        
        // Draw the background
        g2d.setColor(new Color(18, 18, 18));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(new Color(24, 24, 24));
        g2d.fillRect(x, y, cellSize * (size + 2), cellSize * (size + 2));
        
        // Draw cells
        for (int row = 0; row < size + 2; row++) {
            for (int col = 0; col < size + 2; col++) {
                int cellX = x + col * cellSize;
                int cellY = y + row * cellSize;
                
                if (row == 0 || col == 0 || row == size + 1 || col == size + 1) {
                    // Draw border wall
                    g2d.setColor(wallColor);
                    g2d.fillRect(cellX, cellY, cellSize, cellSize);
                    continue;
                }
                
                int mapValue = mapGenerator[row][col];
                
                if (mapValue == WALL) {
                    // Draw wall
                    if (wallImage != null) {
                        g2d.drawImage(wallImage, cellX, cellY, cellSize, cellSize, this);
                    } else {
                        g2d.setColor(wallColor);
                        g2d.fillRect(cellX, cellY, cellSize, cellSize);
                    }
                } else if (mapValue == START) {
                    // Draw start
                    g2d.setColor(startColor);
                    g2d.fillRect(cellX, cellY, cellSize, cellSize);
                    g2d.setColor(Color.WHITE);
                    g2d.drawString("S", cellX + cellSize/2 - 4, cellY + cellSize/2 + 4);
                } else if (mapValue == END) {
                    // Draw end
                    g2d.setColor(endColor);
                    g2d.fillRect(cellX, cellY, cellSize, cellSize);
                    g2d.setColor(Color.WHITE);
                    g2d.drawString("E", cellX + cellSize/2 - 4, cellY + cellSize/2 + 4);
                } else {
                    // Draw empty cell
                    g2d.setColor(emptyColor);
                    g2d.fillRect(cellX, cellY, cellSize, cellSize);
                    
                    // Draw assets if present
                    if (row - 1 >= 0 && col - 1 >= 0 && row - 1 < size && col - 1 < size) {
                        BufferedImage assetImage = null;
                        switch (map[row - 1][col - 1]) {
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
                            g2d.drawImage(assetImage, cellX, cellY, cellSize, cellSize, this);
                        }
                    }
                }
                
                // Draw grid lines for clarity
                g2d.setColor(gridColor);
                g2d.drawRect(cellX, cellY, cellSize, cellSize);
            }
        }
        
        // Draw path stars
        for (int[] pos : starPathPositions) {
            int rowStar = pos[0], colStar = pos[1];
            int starX = x + (colStar + 1) * cellSize;
            int starY = y + (rowStar + 1) * cellSize;
            
            g2d.drawImage(starImage, starX, starY, cellSize, cellSize, this);
        }
    }
    
    public int[][] getMapGenerator() {
        return mapGenerator;
    }

    public BufferedImage getStarImage() {
        return starImage;
    }

    public void addStarPathPosition(int row, int col) {
        for (int[] pos : starPathPositions) {
            if (pos[0] == row && pos[1] == col) {
                return;
            }
        }
        starPathPositions.add(new int[]{row, col});
        repaint();
    }

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

    public int getPathStepCount() {
        return starPathPositions.size();
    }
    
    // Add this method for setting a custom title above the map
    public void setMapTitle(String title) {
        // Implementation could be added if needed
    }
}
