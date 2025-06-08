
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Map extends JPanel{
    private int[][] map;
    private BufferedImage mapImage, windImage, portal1Image, portal2Image, jinxBlockImage, treeImage;
    private int size = 3;
    private TileType[][] tileTypes;

    public Map(){
        try{
            windImage = ImageIO.read(new File("Aplro/Asset/Angin.png"));
            portal1Image = ImageIO.read(new File("Aplro/Asset/Portal1.png"));
            portal2Image = ImageIO.read(new File("Aplro/Asset/Portal2.png"));
            jinxBlockImage = ImageIO.read(new File("Aplro/Asset/JinxBlock.png"));
            treeImage = ImageIO.read(new File("Aplro/Asset/Pohon.png"));
        }catch(IOException e){
            System.out.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setMap(int[][] map, int size) {
        this.map = map;
        this.size = size;
        this.tileTypes = new TileType[size + 2][size + 2];
        Random random = new Random();

        for (int i = 0; i < size + 2; i++) {
            for (int j = 0; j < size + 2; j++) {
                if (i == 0 || j == 0 || i == size + 1 || j == size + 1) {
                    map[i][j] = 1;
                    tileTypes[i][j] = TileType.WALL;
                } else {
                    if (random.nextDouble() < 0.3) {
                        map[i][j] = 1;
                        tileTypes[i][j] = TileType.WALL;
                    } else {
                        map[i][j] = 0;
                        tileTypes[i][j] = TileType.EMPTY;
                    }
                }
            }
        }
        map[size][1] = 2;
        tileTypes[size][1] = TileType.START;
        map[1][size] = 3;
        tileTypes[1][size] = TileType.END;
        if (size > 1) {
            map[size - 1][1] = 0; tileTypes[size - 1][1] = TileType.EMPTY;
            map[size][2] = 0;   tileTypes[size][2] = TileType.EMPTY;
            map[2][size] = 0;   tileTypes[2][size] = TileType.EMPTY;
            map[1][size - 1] = 0; tileTypes[1][size - 1] = TileType.EMPTY;
        }
        List<int[]> emptyCells = new ArrayList<>();

        for (int i = 1; i <= size; i++) {
            for (int j = 1; j <= size; j++) {
                if (tileTypes[i][j] == TileType.EMPTY) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }
        Function<List<int[]>, int[]> pickRandomCell = (cells) -> {
            if (cells.isEmpty()){
                return null;   
            }
            int idx = random.nextInt(cells.size());

            return cells.remove(idx);
        };
        int[] pos;

        pos = pickRandomCell.apply(emptyCells);
        if (pos != null){
            tileTypes[pos[0]][pos[1]] = TileType.WIND;   
        }
        pos = pickRandomCell.apply(emptyCells);
        if (pos != null){
            tileTypes[pos[0]][pos[1]] = TileType.PORTAL;
        }
        pos = pickRandomCell.apply(emptyCells);
        if (pos != null){
            tileTypes[pos[0]][pos[1]] = TileType.JINX;
        }
        pos = pickRandomCell.apply(emptyCells);
        if (pos != null){
            tileTypes[pos[0]][pos[1]] = TileType.TREE;
        }
        pos = pickRandomCell.apply(emptyCells);
        if (pos != null){
            tileTypes[pos[0]][pos[1]] = TileType.PORTAL;
        }
        loadImage(size);
        repaint();
    }

    private void loadImage(int size) {
        try {
            mapImage = ImageIO.read(new File("Aplro/Asset/map_" + size + "x" + size + "_gray.png"));
        } catch (IOException e) {
            System.out.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public enum TileType {
        EMPTY, WALL, START, END, PATH, WIND, JINX, TREE, PORTAL
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (mapImage == null) {
            return;   
        }
        int x0 = (getWidth() - mapImage.getWidth(this)) / 2, y0 = (getHeight() - mapImage.getHeight(this)) / 2, cellSize = mapImage.getWidth(this) / size;
        
        g.drawImage(mapImage, x0, y0, this);
        for (int i = 1; i <= size; i++) {
            for (int j = 1; j <= size; j++) {
                int x = x0 + (j - 1) * cellSize, y = y0 + (i - 1) * cellSize;
                
                switch (tileTypes[i][j]) {
                    case WIND:
                        if (windImage != null){
                            g.drawImage(windImage, x, y, cellSize, cellSize, this);
                        }
                        break;
                    case JINX:
                        if (jinxBlockImage != null){
                            g.drawImage(jinxBlockImage, x, y, cellSize, cellSize, this);
                        }
                        break;
                    case TREE:
                        if (treeImage != null){
                            g.drawImage(treeImage, x, y, cellSize, cellSize, this);
                        }
                        break;
                    case PORTAL:
                        if (portal1Image != null){
                            g.drawImage(portal1Image, x, y, cellSize, cellSize, this);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
