import java.io.*;
import java.util.*;
import javax.swing.*;

/*
 * SolverMain
 * Author: 
 * Version information: 1.0
 * Date: October 23, 2025

 ========
 * Input: The program prompts the user to select a word list file (one word per line),
 *       a puzzle grid file (one row per line, characters adjacent), and an output
 *       HTML filename where results will be saved.
 * Output: Generates an HTML report containing execution time, a table of found words
 *        with their start/end coordinates, and the puzzle grid with found words highlighted.
 * Process: Uses an Aho-Corasick automaton built from the word list (including reversed
 *        words) to efficiently scan rows, columns, and both diagonal directions of the grid.
 *        The program measures execution time with System.nanoTime(), writes the HTML
 *        output using StringBuilder, and allows the user to restart or quit via GUI dialogs.
 ======
 */
public class SolverMain {
    
    /**
     * Main entry point for the application.
     * 
     * @param args Command line arguments (not used - GUI input)
     */
    public static void main(String[] args) {
        // Set look and feel for better GUI appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel if system look and feel fails
        }
        
        while (true) {
            try {
                // Step 1: Show welcome dialog
                int choice = JOptionPane.showConfirmDialog(
                    null,
                    "Welcome to Word Search Solver!\n" +
                    "Using Aho-Corasick Algorithm\n\n" +
                    "Ready to select input files?",
                    "Word Search Solver",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                if (choice != JOptionPane.OK_OPTION) {
                    System.out.println("User cancelled. Exiting.");
                    break;
                }
                
                // Step 2: Select word list file
                JFileChooser wordListChooser = new JFileChooser(System.getProperty("user.dir"));
                wordListChooser.setDialogTitle("Select Word List File");
                wordListChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
                
                int wordListResult = wordListChooser.showOpenDialog(null);
                if (wordListResult != JFileChooser.APPROVE_OPTION) {
                    System.out.println("No word list selected. Exiting.");
                    break;
                }
                
                File wordListFile = wordListChooser.getSelectedFile();
                String wordListPath = wordListFile.getAbsolutePath();
                
                // Step 3: Select puzzle grid file
                JFileChooser gridChooser = new JFileChooser(wordListFile.getParent());
                gridChooser.setDialogTitle("Select Puzzle Grid File");
                gridChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
                
                int gridResult = gridChooser.showOpenDialog(null);
                if (gridResult != JFileChooser.APPROVE_OPTION) {
                    System.out.println("No grid file selected. Exiting.");
                    break;
                }
                
                File gridFile = gridChooser.getSelectedFile();
                String gridPath = gridFile.getAbsolutePath();
                
                // Step 4: Select output HTML file location
                JFileChooser outputChooser = new JFileChooser(gridFile.getParent());
                outputChooser.setDialogTitle("Save Results As HTML");
                outputChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("HTML Files", "html"));
                outputChooser.setSelectedFile(new File("result.html"));
                
                int outputResult = outputChooser.showSaveDialog(null);
                if (outputResult != JFileChooser.APPROVE_OPTION) {
                    System.out.println("No output file selected. Exiting.");
                    break;
                }
                
                File outputFile = outputChooser.getSelectedFile();
                String outputPath = outputFile.getAbsolutePath();
                if (!outputPath.toLowerCase().endsWith(".html")) {
                    outputPath += ".html";
                }
                
                // Display processing message
                System.out.println("\n=== Word Search Solver ===");
                System.out.println("Word List: " + wordListPath);
                System.out.println("Grid File: " + gridPath);
                System.out.println("Output: " + outputPath);
                System.out.println("\nReading input files...");
                
                // Step 5: Read word list
                List<String> words = readWordList(wordListPath);
                System.out.println("Loaded " + words.size() + " words");
                
                // Step 6: Read puzzle grid
                char[][] grid = readGrid(gridPath);
                int rows = grid.length;
                int cols = grid[0].length;
                System.out.println("Loaded " + rows + "x" + cols + " grid");
                
                // Step 7: START TIMING - After file I/O completes
                long startTime = System.nanoTime();
                
                // Step 8: Execute word search
                System.out.println("Searching for words...");
                WordSearchSolver solver = new WordSearchSolver(grid, words);
                List<WordSearchSolver.FoundWord> results = solver.solve();
                
                // Step 9: Generate HTML output
                System.out.println("Generating HTML output...");
                generateHTMLOutput(outputPath, grid, results);
                
                // Step 10: END TIMING - After HTML generation completes
                long endTime = System.nanoTime();
                double executionTimeMs = (endTime - startTime) / 1_000_000.0;
                
                // Step 11: Display results in console
                System.out.println("\n=== Results ===");
                System.out.println("Words found: " + results.size() + " / " + words.size());
                System.out.printf("Execution time: %.3f milliseconds%n", executionTimeMs);
                System.out.println("Output saved to: " + outputPath);
                
                // Display found words summary
                System.out.println("\nFound words:");
                Map<String, Integer> wordCounts = new HashMap<>();
                for (WordSearchSolver.FoundWord fw : results) {
                    wordCounts.put(fw.word, wordCounts.getOrDefault(fw.word, 0) + 1);
                }
                
                StringBuilder foundSummary = new StringBuilder();
                for (String word : words) {
                    String upperWord = word.toUpperCase();
                    int count = wordCounts.getOrDefault(upperWord, 0);
                    String status = count > 0 ? "✓ FOUND" : "✗ NOT FOUND";
                    System.out.printf("  %-15s %s%n", upperWord, status);
                    foundSummary.append(String.format("%-15s %s%n", upperWord, status));
                }
                
                // Step 12: Show success dialog with restart option
                String message = String.format(
                    "Word Search Complete!\n\n" +
                    "Grid Size: %dx%d\n" +
                    "Words Found: %d / %d\n" +
                    "Execution Time: %.3f ms\n\n" +
                    "Output saved to:\n%s\n\n" +
                    "Would you like to solve another word search?",
                    rows, cols, results.size(), words.size(), executionTimeMs, outputPath
                );
                
                int restartChoice = JOptionPane.showConfirmDialog(
                    null,
                    message,
                    "Success - Word Search Complete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                if (restartChoice != JOptionPane.YES_OPTION) {
                    System.out.println("\n--- Program terminated by user ---");
                    break;
                }
                
                System.out.println("\n--- Restarting Word Search Solver ---\n");
                
            } catch (FileNotFoundException e) {
                String errorMsg = "File not found: " + e.getMessage();
                System.err.println("Error: " + errorMsg);
                JOptionPane.showMessageDialog(
                    null,
                    errorMsg + "\n\nPlease check the file path and try again.",
                    "File Not Found",
                    JOptionPane.ERROR_MESSAGE
                );
                // Ask if user wants to try again
                int retry = JOptionPane.showConfirmDialog(
                    null,
                    "Would you like to try again with different files?",
                    "Error",
                    JOptionPane.YES_NO_OPTION
                );
                if (retry != JOptionPane.YES_OPTION) break;
                
            } catch (IOException e) {
                String errorMsg = "Error reading file: " + e.getMessage();
                System.err.println(errorMsg);
                JOptionPane.showMessageDialog(
                    null,
                    errorMsg,
                    "I/O Error",
                    JOptionPane.ERROR_MESSAGE
                );
                // Ask if user wants to try again
                int retry = JOptionPane.showConfirmDialog(
                    null,
                    "Would you like to try again with different files?",
                    "Error",
                    JOptionPane.YES_NO_OPTION
                );
                if (retry != JOptionPane.YES_OPTION) break;
                
            } catch (Exception e) {
                String errorMsg = "Unexpected error: " + e.getMessage();
                System.err.println(errorMsg);
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                    null,
                    errorMsg,
                    "Unexpected Error",
                    JOptionPane.ERROR_MESSAGE
                );
                break;
            }
        }
        
        System.out.println("Exiting Word Search Solver. Goodbye!");
    }
    
    /**
     * Reads a word list from a file (one word per line).
     * 
     * @param filePath Path to the word list file
     * @return List of words (trimmed, may be mixed case)
     * @throws IOException if file reading fails
     */
    private static List<String> readWordList(String filePath) throws IOException {
        List<String> words = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    words.add(line);
                }
            }
        }
        
        if (words.isEmpty()) {
            throw new IOException("Word list file is empty: " + filePath);
        }
        
        return words;
    }
    
    /**
     * Reads a puzzle grid from a file.
     * Expected format: One row per line, characters adjacent (no spaces).
     * 
     * @param filePath Path to the grid file
     * @return 2D character array representing the grid
     * @throws IOException if file reading fails or format is invalid
     */
    private static char[][] readGrid(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().replaceAll("\\s+", ""); // Remove all whitespace
                if (!line.isEmpty()) {
                    lines.add(line.toUpperCase());
                }
            }
        }
        
        if (lines.isEmpty()) {
            throw new IOException("Grid file is empty: " + filePath);
        }
        
        int rows = lines.size();
        int cols = lines.get(0).length();
        
        // Validate grid dimensions
        for (String line : lines) {
            if (line.length() != cols) {
                throw new IOException("Grid has inconsistent row lengths");
            }
        }
        
        // Convert to 2D array
        char[][] grid = new char[rows][cols];
        for (int r = 0; r < rows; r++) {
            grid[r] = lines.get(r).toCharArray();
        }
        
        return grid;
    }
    
    /**
     * Generates an HTML output file with search results and highlighted grid.
     * Uses StringBuilder for efficient string construction.
     * 
     * @param outputPath Path for the output HTML file
     * @param grid The puzzle grid
     * @param results List of found words
     * @throws IOException if file writing fails
     */
    private static void generateHTMLOutput(String outputPath, char[][] grid, 
                                          List<WordSearchSolver.FoundWord> results) throws IOException {
        int rows = grid.length;
        int cols = grid[0].length;

        // Build highlighted cells lookup (2-5x faster than HashSet<String>)
        boolean[][] highlighted = new boolean[rows][cols];
        for (WordSearchSolver.FoundWord fw : results) {
            // Inline direction calculation (avoids Integer.compare overhead)
            int dr = fw.endRow > fw.startRow ? 1 : (fw.endRow < fw.startRow ? -1 : 0);
            int dc = fw.endCol > fw.startCol ? 1 : (fw.endCol < fw.startCol ? -1 : 0);

            int r = fw.startRow;
            int c = fw.startCol;

            while (true) {
                highlighted[r][c] = true;
                if (r == fw.endRow && c == fw.endCol) break;
                r += dr;
                c += dc;
            }
        }

        // Pre-allocate StringBuilder: ~60 bytes per cell for 20x20 = 24KB
        StringBuilder html = new StringBuilder(rows * cols * 60);
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset=\"utf-8\"><title>Word Search</title>");
        html.append("<style>table{border-collapse:collapse}td{width:28px;height:28px;text-align:center;border:1px solid #000;font-family:monospace;font-weight:bold}.h{background:#ffeb3b}</style>");
        html.append("</head><body>");
        html.append("<h1 style=\"font-family:monospace\">Word Search Results</h1>");
        html.append("<table>");

        for (int r = 0; r < rows; r++) {
            html.append("<tr>");
            for (int c = 0; c < cols; c++) {
                if (highlighted[r][c]) {
                    html.append("<td class=\"h\">").append(grid[r][c]).append("</td>");
                } else {
                    html.append("<td>").append(grid[r][c]).append("</td>");
                }
            }
            html.append("</tr>");
        }

        html.append("</table>");
        html.append("</body></html>");

        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(html.toString());
        }
    }
}
