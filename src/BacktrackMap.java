
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
import javax.swing.Timer;

public class BacktrackMap extends JPanel{
    private int[][] map;
    private BufferedImage mapImage, windImage, portal1Image, portal2Image, jinxBlockImage, treeImage, starImage;
    private int size = 3;
    private TileType[][] tileTypes;

    public BacktrackMap(){
        try{
            windImage = ImageIO.read(new File("Aplro/Asset/Angin.png"));
            portal1Image = ImageIO.read(new File("Aplro/Asset/Portal1.png"));
            portal2Image = ImageIO.read(new File("Aplro/Asset/Portal2.png"));
            jinxBlockImage = ImageIO.read(new File("Aplro/Asset/JinxBlock.png"));
            treeImage = ImageIO.read(new File("Aplro/Asset/Pohon.png"));
            starImage = ImageIO.read(new File("Aplro/Asset/Bintang.png"));
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
            map[size - 1][1] = 0;
            tileTypes[size - 1][1] = TileType.EMPTY;
            map[size][2] = 0;
            tileTypes[size][2] = TileType.EMPTY;
            map[2][size] = 0;
            tileTypes[2][size] = TileType.EMPTY;
            map[1][size - 1] = 0;
            tileTypes[1][size - 1] = TileType.EMPTY;
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
            tileTypes[pos[0]][pos[1]] = TileType.PORTAL1;
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
            tileTypes[pos[0]][pos[1]] = TileType.PORTAL2;
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
        EMPTY, WALL, START, END, PATH, WIND, JINX, TREE, PORTAL1, PORTAL2, STAR
    }

    public List<int[]> findPathBacktrack() {
        boolean[][] visited = new boolean[size + 2][size + 2];
        List<int[]> path = new ArrayList<>();

        int sx = size, sy = 1, ex = 1, ey = size;
        if (backtrack(sx, sy, ex, ey, visited, path)) {
            return path;
        }
        return null;
    }

    private boolean backtrack(int x, int y, int ex, int ey, boolean[][] visited, List<int[]> path) {
        if (x == ex && y == ey) {
            path.add(0, new int[]{x, y});
            return true;
        }
        visited[x][y] = true;
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        if (tileTypes[x][y] == TileType.PORTAL1 || tileTypes[x][y] == TileType.PORTAL2) {
            int px = -1, py = -1;

            for (int i = 1; i <= size; i++) {
                for (int j = 1; j <= size; j++) {
                    if (tileTypes[x][y] == TileType.PORTAL1 && tileTypes[i][j] == TileType.PORTAL2) {
                        px = i; py = j;
                    }
                    if (tileTypes[x][y] == TileType.PORTAL2 && tileTypes[i][j] == TileType.PORTAL1) {
                        px = i; py = j;
                    }
                }
            }
            if (px != -1 && py != -1 && !visited[px][py]) {
                if (backtrack(px, py, ex, ey, visited, path)) {
                    path.add(new int[]{x, y});
                    return true;
                }
            }
        }
        for (int d = 0; d < 4; d++) {
            int nx = x + dx[d], ny = y + dy[d];

            if (nx < 1 || ny < 1 || nx > size || ny > size){
                continue;
            }
            if (visited[nx][ny]){
                continue;
            }
            if (tileTypes[nx][ny] == TileType.WALL || tileTypes[nx][ny] == TileType.TREE){
                continue;
            }
            if (tileTypes[nx][ny] == TileType.WIND) {
                int bx = x - dx[d]*2, by = y - dy[d]*2;

                if (bx >= 1 && by >= 1 && bx <= size && by <= size &&
                    !visited[bx][by] &&
                    tileTypes[bx][by] != TileType.WALL && tileTypes[bx][by] != TileType.TREE) {
                    if (backtrack(bx, by, ex, ey, visited, path)) {
                        path.add(0, new int[]{x, y});
                        return true;
                    }
                }
            } else {
                if (backtrack(nx, ny, ex, ey, visited, path)) {
                    path.add(0, new int[]{x, y});
                    return true;
                }
            }
        }
        visited[x][y] = false;
        return false;
    }

    public void animatePath(List<int[]> path, int waktu, Runnable onFinish) {
        if (path == null || path.isEmpty()) {
            if (onFinish != null) onFinish.run();
            return;
        }
        final int[] step = {0};
        Timer timer = new Timer(300, null);

        timer.addActionListener(e -> {
            if (step[0] < path.size()) {
                int[] pos = path.get(step[0]);
                
                if (tileTypes[pos[0]][pos[1]] != TileType.START && tileTypes[pos[0]][pos[1]] != TileType.END) {
                    tileTypes[pos[0]][pos[1]] = TileType.STAR;
                }
                repaint();
                step[0]++;
            } else {
                timer.stop();
                if (onFinish != null){
                    onFinish.run();
                }
            }
        });
        timer.start();
    }

    public int getPathTime(List<int[]> path) {
        if (path == null){
            return -1;
        }
        int time = 0;

        for (int[] pos : path) {
            TileType t = tileTypes[pos[0]][pos[1]];
            if (t == TileType.JINX){
                time += 4;
            }
            else{
                time += 1;
            }
        }
        return time;
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
                    case PORTAL1:
                        if (portal1Image != null){
                            g.drawImage(portal1Image, x, y, cellSize, cellSize, this);
                        }
                        break;
                    case PORTAL2:
                        if (portal2Image != null){
                            g.drawImage(portal2Image, x, y, cellSize, cellSize, this);
                        }
                        break;
                    case STAR:
                        if (starImage != null){
                            g.drawImage(starImage, x, y, cellSize, cellSize, this);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
