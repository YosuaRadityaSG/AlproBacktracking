import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        int size;
        
        System.out.println("=== Backtracking Maze Solver ===");
        
        // Step 1: Get map size
        do {
            System.out.print("Enter map size (1-15): ");
            size = scanner.nextInt();
            
            if (size < 1 || size > 15) {
                System.out.println("Invalid size. Please enter a number between 1 and 15.");
            }
        } while (size < 1 || size > 15);
        
        // Step 2: Generate the map
        System.out.println("\nStep 1: Generating map...");
        MapGenerator mapGenerator = new MapGenerator(size);
        int[][] map = mapGenerator.generateMap();
        
        // Step 3: Display the generated map
        System.out.println("\nStep 2: Map generated with borders:");
        System.out.println("□ = Empty, ■ = Wall, S = Start, E = End");
        printMap(map);
        
        // Step 4: Apply backtracking to find solution
        System.out.println("\nStep 3: Applying backtracking algorithm...");
        Backtracker backtracker = new Backtracker(map);
        boolean solutionFound = backtracker.solve();
        
        // Step 5: Display results
        if (solutionFound) {
            System.out.println("\nStep 4: Solution found!");
            System.out.println("□ = Empty, ■ = Wall, S = Start, E = End, × = Path");
            printMap(backtracker.getSolutionPath());
        } else {
            System.out.println("\nStep 4: No solution exists for this map.");
            printMap(map);
        }
        
        scanner.close();
    }
    
    private static void printMap(int[][] map) {
        String[] symbols = {"□ ", "■ ", "S ", "E ", "× "};
        
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                int value = map[i][j];
                if (value >= 0 && value < symbols.length) {
                    System.out.print(symbols[value]);
                } else {
                    System.out.print(value + " ");
                }
            }
            System.out.println();
        }
    }
}
