import javax.swing.JFrame;

public class App {
    public static void main(String[] args) throws Exception {
        // Membuat jendela aplikasi utama (JFrame)
        // Mengatur judul jendela menjadi "Backtracking Maze Solver"
        // Mengatur ukuran jendela menjadi 800x600 piksel
        // Menambahkan instance Layar (panel utama) ke jendela
        // Membuat jendela terlihat bagi pengguna
        JFrame frame = new JFrame("Backtracking Maze Solver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(new Layar());
        frame.setVisible(true);
    }
}
