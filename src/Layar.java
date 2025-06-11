import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Layar extends JPanel {
    // Komponen UI
    private JTextField sizeField;          // Untuk input ukuran labirin
    private JTextArea output;              // Output teks untuk labirin kecil
    private JScrollPane scrollPane;        // Tampilan dapat digulir untuk output teks
    private JScrollPane mapScrollPane;     // Tampilan dapat digulir untuk visualisasi backtracking
    private JScrollPane solutionMapScrollPane; // Tampilan dapat digulir untuk algoritma solusi
    private Map mapPanel;                  // Panel visual untuk algoritma backtracking
    private Map solutionMapPanel;          // Panel visual untuk algoritma solusi
    private JLabel backtrackingTimerLabel; // Menampilkan timer untuk backtracking
    private JLabel solutionTimerLabel;     // Menampilkan timer untuk algoritma solusi
    
    // Variabel timer
    private long startTime;                // Waktu mulai untuk pengukuran kinerja
    private boolean timerRunning;          // Flag untuk mengontrol thread timer
    private double jinxBlock = 0;          // Akumulator penalti waktu untuk JinxBlock

    // Konstruktor menyiapkan tata letak UI dan komponen
    public Layar() {
        // Menyiapkan panel dengan layout null
        setLayout(null);
        setBackground(new Color(173, 216, 230)); // Latar belakang biru muda
        
        // Menginisialisasi scroll pane sebagai null
        mapScrollPane = null;
        solutionMapScrollPane = null;
        
        // Membuat dan memposisikan komponen UI
        // 1. Label dan field teks untuk input ukuran labirin
        JLabel label = new JLabel("Map size (1-15): ");
        label.setBounds(10, 0, 100, 40);
        label.setBackground(new Color(173, 216, 230));
        add(label);
        
        sizeField = new JTextField(2);
        sizeField.setBounds(105, 10, 30, 20);
        add(sizeField);
        
        // 2. Tombol untuk memicu pembuatan labirin dan pemecahan
        JButton button = new JButton("Generate and Solve");
        button.setBounds(10, 35, 150, 30);
        add(button);
        
        // 3. Label timer untuk perbandingan algoritma
        backtrackingTimerLabel = new JLabel("");
        backtrackingTimerLabel.setBounds(535, 70, 100, 40);
        add(backtrackingTimerLabel);
        backtrackingTimerLabel.setVisible(false);
        
        solutionTimerLabel = new JLabel("");
        solutionTimerLabel.setBounds(1145, 70, 100, 40);
        solutionTimerLabel.setVisible(false);
        add(solutionTimerLabel);
        
        // 4. Area teks untuk tampilan output
        output = new JTextArea();
        output.setEditable(false);
        output.setMargin(new Insets(0, 10, 0, 0));
        output.setBackground(new Color(173, 216, 230));
        
        // 5. Scroll pane untuk output teks
        scrollPane = new JScrollPane(output);
        scrollPane.setBounds(0, 70, 1285, 1000);
        scrollPane.getViewport().setBackground(new Color(173, 216, 230));
        add(scrollPane);
        
        // Menambahkan action listener ke tombol
        button.addActionListener(e -> onGenerate());
    }

    // Handler untuk pembuatan labirin dan pemecahan
    private void onGenerate() {
        // Memvalidasi input pengguna untuk ukuran labirin
        int size;
        output.setText("");
        try {
            size = Integer.parseInt(sizeField.getText());
            if (size < 1 || size > 15) {
                output.setText("Ukuran tidak valid. Masukkan angka antara 1 dan 15.\n");
                return;
            }
        } catch (NumberFormatException ex) {
            output.setText("Silakan masukkan bilangan bulat yang valid.\n");
            return;
        }
        
        // Mulai pembuatan labirin
        output.append("Membuat peta...\n");
        output.append("Peta dibuat dengan batas:\n");
        
        // Menangani pembuatan labirin berdasarkan ukuran
        switch (size) {
            // Untuk ukuran 3-15, gunakan perbandingan visual
            case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11: case 12: case 13: case 14: case 15:
                // Membersihkan visualisasi sebelumnya jika ada
                if (mapPanel != null) {
                    mapPanel.clearPath();
                    mapPanel.repaint();
                }
                if (solutionMapPanel != null) {
                    solutionMapPanel.clearPath();
                    solutionMapPanel.repaint();
                }
                if (mapScrollPane != null) {
                    remove(mapScrollPane);
                    mapScrollPane = null;
                }
                if (solutionMapScrollPane != null) {
                    remove(solutionMapScrollPane);
                    solutionMapScrollPane = null;
                }
                
                // Membuat dua labirin identik dengan seed yang sama
                long seed = System.currentTimeMillis();
                
                // Menyiapkan panel kiri untuk visualisasi backtracking
                mapPanel = new Map(size, seed);
                int mapSize = size * 40;
                mapPanel.setPreferredSize(new Dimension(mapSize, mapSize));
                mapPanel.setBackground(new Color(173, 216, 230));
                mapScrollPane = new JScrollPane(mapPanel);
                mapScrollPane.setBounds(6, 105, 610, 610);
                add(mapScrollPane);
                
                // Menyiapkan panel kanan untuk visualisasi algoritma solusi
                solutionMapPanel = new Map(size, seed);
                solutionMapPanel.setPreferredSize(new Dimension(mapSize, mapSize));
                solutionMapPanel.setBackground(new Color(173, 216, 230));
                solutionMapScrollPane = new JScrollPane(solutionMapPanel);
                solutionMapScrollPane.setBounds(615, 105, 610, 610);
                add(solutionMapScrollPane);
                
                // Menginisialisasi variabel timer
                startTime = System.currentTimeMillis();
                timerRunning = true;
                jinxBlock = 0;
                backtrackingTimerLabel.setText("");
                backtrackingTimerLabel.setVisible(true);
                solutionTimerLabel.setText("");
                solutionTimerLabel.setVisible(false);
                
                // Memulai timer visualisasi backtracking
                startTimer();
                
                // Membuat thread baru untuk algoritma agar UI tetap responsif
                new Thread(() -> {
                    try {
                        // Penundaan singkat sebelum memulai
                        Thread.sleep(500);
                        
                        // Mulai detektor JinxBlock untuk memantau penalti waktu
                        startJinxBlockDetector();
                        
                        // Menjalankan algoritma backtracking di panel kiri
                        Backtracking backtracking = new Backtracking(mapPanel, mapPanel.getStarImage());
                        boolean backtrackingFound = backtracking.solveWithAnimation();
                        
                        // Menghentikan timer dan menghitung total waktu
                        stopTimer();
                        double backtrackingTime = (System.currentTimeMillis() - startTime) / 1000.0 + jinxBlock;
                        
                        // Memperbarui UI dengan hasil backtracking
                        SwingUtilities.invokeLater(() -> {
                            backtrackingTimerLabel.setText("Timer: " + String.format("%.2f", backtrackingTime) + "s");
                            solutionTimerLabel.setVisible(true);
                        });
                        
                        // Penundaan singkat sebelum memulai algoritma solusi
                        Thread.sleep(500);
                        
                        // Mengatur ulang timer untuk algoritma solusi
                        startTime = System.currentTimeMillis();
                        jinxBlock = 0;
                        timerRunning = true;
                        
                        // Membuat thread timer untuk algoritma solusi
                        Thread timerThread = new Thread(() -> {
                            try {
                                while (timerRunning) {
                                    Thread.sleep(10);
                                    final double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0 + jinxBlock;
                                    
                                    SwingUtilities.invokeLater(() -> {
                                        solutionTimerLabel.setText("Timer: " + String.format("%.2f", elapsedTime) + "s");
                                    });
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                        timerThread.start();
                        
                        // Menjalankan algoritma solusi di panel kanan
                        Solution solution = new Solution(solutionMapPanel, solutionMapPanel.getStarImage());
                        boolean solutionFound = solution.solveWithAnimation();
                        
                        // Menghentikan timer dan menampilkan waktu akhir
                        timerRunning = false;
                        double solutionTime = solution.getTime();
                        
                        SwingUtilities.invokeLater(() -> {
                            solutionTimerLabel.setText("Timer: " + String.format("%.2f", solutionTime) + "s");
                        });
                        
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                break;
                
            // Untuk ukuran yang lebih kecil, gunakan tampilan teks
            default:
                backtrackingTimerLabel.setVisible(false);
                if (scrollPane.getParent() == null) {
                    add(scrollPane);
                }
                
                // Membuat labirin berbasis teks sederhana
                MapGenerator mapGenerator = new MapGenerator(size);
                int[][] map = mapGenerator.generateMap();

                // Menampilkan labirin awal
                output.append("□ = Kosong, ■ = Dinding, S = Awal, E = Akhir\n");
                output.append(printMap(map));
                
                // Menjalankan algoritma backtracking
                output.append("Menerapkan backtracking...\n");
                startTime = System.currentTimeMillis();
                Backtracker backtracker = new Backtracker(map);
                boolean solutionFound = backtracker.solve();
                double solveTime = (System.currentTimeMillis() - startTime) / 1000.0;

                // Menampilkan solusi atau pesan kegagalan
                if (solutionFound) {
                    output.append("□ = Kosong, ■ = Dinding, S = Awal, E = Akhir, × = Jalur\n");
                    output.append(printMap(backtracker.getSolutionPath()));
                } else {
                    output.append("Tidak ada solusi yang ada.\n");
                    output.append(printMap(map));
                }
                break;
        }
    }
    
    // Memantau untuk pertemuan JinxBlock dan menambahkan penalti waktu
    private void startJinxBlockDetector() {
        new Thread(() -> {
            try {
                int lastPathSize = 0;
                
                // Saat timer berjalan, periksa pertemuan JinxBlock
                while (timerRunning) {
                    Thread.sleep(100);
                    if (mapPanel != null) {
                        List<int[]> path = mapPanel.getStarPathPositions();
                        
                        // Periksa apakah posisi baru telah ditambahkan ke jalur
                        if (path.size() > lastPathSize && path.size() > 0) {
                            int[] pos = path.get(path.size() - 1);
                            int[][] internalMap = mapPanel.getInternalMap();
                            
                            // Periksa apakah posisi baru adalah JinxBlock
                            if (pos[0] >= 0 && pos[0] < internalMap.length && 
                                pos[1] >= 0 && pos[1] < internalMap[0].length &&
                                internalMap[pos[0]][pos[1]] == 4) {
                                // Tambahkan penalti 4 detik dan jeda selama 2 detik
                                jinxBlock += 4.0;
                                Thread.sleep(2000);
                            }
                            lastPathSize = path.size();
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    // Menghentikan timer
    private void stopTimer() {
        timerRunning = false;
    }

    // Memulai thread timer untuk memperbarui label timer
    private void startTimer() {
        new Thread(() -> {
            while (timerRunning) {
                try {
                    Thread.sleep(10);
                    final double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0 + jinxBlock;
                    
                    SwingUtilities.invokeLater(() -> {
                        backtrackingTimerLabel.setText("Timer: " + String.format("%.2f", elapsedTime) + "s");
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Mengkonversi array labirin ke representasi teks yang rapi
    private String printMap(int[][] map) {
        String[] symbols = {"□ ", "■ ", "S ", "E ", "× "};
        StringBuilder stringBuilder = new StringBuilder();

        // Untuk setiap sel, tambahkan simbol yang sesuai
        for (int[] row : map) {
            for (int value : row) {
                if (value >= 0 && value < symbols.length) {
                    stringBuilder.append(symbols[value]);
                } else {
                    stringBuilder.append(value).append(" ");
                }
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}

// 1. Membuat komponen UI (field teks, tombol, panel, label)
// 2. Menangani input pengguna untuk ukuran labirin (1-15)
// 3. Menghasilkan labirin berdasarkan input pengguna
// 4. Menampilkan dua panel labirin berdampingan:
//    - Panel kiri: Visualisasi algoritma backtracking
//    - Panel kanan: Visualisasi algoritma solusi
// 5. Melacak dan menampilkan waktu penyelesaian untuk kedua algoritma
// 6. Mengelola elemen game khusus seperti JinxBlock yang menambahkan penalti waktu
// 7. Mengkoordinasikan proses pemecahan labirin di thread terpisah
