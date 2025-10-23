import java.util.*;

/*
 * WordSearchSolver
 * Author:
 * Version information: 1.0
 * Date: October 23, 2025
 
 ========
 * Input: The solver accepts a 2D character array (grid) representing the puzzle
 *        and a List<String> of words to search for (one word per entry). The grid
 *        may be non-square (R x C) where 10 <= R,C <= 20.
 * Output: Produces a List of FoundWord records containing the original word (uppercase),
 *         start and end coordinates (row, column), and a direction label for each occurrence.
 * Process: Builds an Aho-Corasick automaton containing both forward and reversed patterns,
 *         then performs single-pass scans over rows, columns, and all diagonals. 1D match
 *         indices are mapped back to 2D coordinates using precomputed coordinate lists for diagonals.
 ======
 */
public class WordSearchSolver {
    
    private final char[][] grid;
    private final int rows;
    private final int cols;
    private final AhoCorasickAutomaton automaton;
    private final List<FoundWord> results;
    private final Map<String, FoundWord> uniqueWords; // Prevent duplicates
    
    /**
     * FoundWord represents a single word found in the grid.
     */
    public static class FoundWord {
        public final String word;
        public final int startRow;
        public final int startCol;
        public final int endRow;
        public final int endCol;
        public final String direction;
        
        public FoundWord(String word, int startRow, int startCol, int endRow, int endCol, String direction) {
            this.word = word;
            this.startRow = startRow;
            this.startCol = startCol;
            this.endRow = endRow;
            this.endCol = endCol;
            this.direction = direction;
        }
        
        /**
         * Creates a unique key for this word placement to avoid duplicates.
         */
        public String getKey() {
            return word + "|" + startRow + "," + startCol + "|" + endRow + "," + endCol;
        }
    }
    
    /**
     * Constructs a WordSearchSolver for the given grid and word list.
     * 
     * @param grid 2D character array representing the word search grid
     * @param words List of words to search for
     */
    public WordSearchSolver(char[][] grid, List<String> words) {
        this.grid = grid;
        this.rows = grid.length;
        this.cols = grid[0].length;
        this.automaton = new AhoCorasickAutomaton(words);
        this.results = new ArrayList<>();
        this.uniqueWords = new HashMap<>();
    }
    
    /**
     * Executes the complete word search across all directions.
     * 
     * @return List of all found words with their positions and directions
     */
    public List<FoundWord> solve() {
        scanHorizontal();
        scanVertical();
        scanDiagonalTLBR(); // Top-Left to Bottom-Right
        scanDiagonalTRBL(); // Top-Right to Bottom-Left
        
        return new ArrayList<>(results);
    }
    
    /**
     * Scans all horizontal rows (left-to-right).
     * Since the automaton includes reversed words, a single forward scan
     * detects both directions.
     */
    private void scanHorizontal() {
        for (int r = 0; r < rows; r++) {
            StringBuilder row = new StringBuilder(cols);
            for (int c = 0; c < cols; c++) {
                row.append(grid[r][c]);
            }
            
            List<AhoCorasickAutomaton.Match> matches = automaton.search(row.toString());
            for (AhoCorasickAutomaton.Match match : matches) {
                String pattern = automaton.getPattern(match.patternId);
                int endCol = match.endPos;
                int startCol = endCol - pattern.length() + 1;
                
                boolean isReversed = automaton.isReversedPattern(match.patternId);
                String direction = isReversed ? "HORIZONTAL_REVERSE" : "HORIZONTAL";
                
                // For reversed patterns, swap start/end for intuitive display
                if (isReversed) {
                    addFoundWord(new StringBuilder(pattern).reverse().toString(), 
                                r, endCol, r, startCol, direction);
                } else {
                    addFoundWord(pattern, r, startCol, r, endCol, direction);
                }
            }
        }
    }
    
    /**
     * Scans all vertical columns (top-to-bottom).
     * Since the automaton includes reversed words, a single forward scan
     * detects both directions.
     */
    private void scanVertical() {
        for (int c = 0; c < cols; c++) {
            StringBuilder col = new StringBuilder(rows);
            for (int r = 0; r < rows; r++) {
                col.append(grid[r][c]);
            }
            
            List<AhoCorasickAutomaton.Match> matches = automaton.search(col.toString());
            for (AhoCorasickAutomaton.Match match : matches) {
                String pattern = automaton.getPattern(match.patternId);
                int endRow = match.endPos;
                int startRow = endRow - pattern.length() + 1;
                
                boolean isReversed = automaton.isReversedPattern(match.patternId);
                String direction = isReversed ? "VERTICAL_REVERSE" : "VERTICAL";
                
                // For reversed patterns, swap start/end for intuitive display
                if (isReversed) {
                    addFoundWord(new StringBuilder(pattern).reverse().toString(), 
                                endRow, c, startRow, c, direction);
                } else {
                    addFoundWord(pattern, startRow, c, endRow, c, direction);
                }
            }
        }
    }
    
    /**
     * Scans all Top-Left to Bottom-Right diagonals.
     * 
     * DIAGONAL ITERATION LOGIC (TL-BR):
     * ==================================
     * For a non-square R x C grid, there are (R + C - 1) diagonals going from
     * top-left to bottom-right.
     * 
     * Strategy:
     * 1. Diagonals starting from the first column (col = 0):
     *    - Start points: (0,0), (1,0), (2,0), ..., (R-1, 0)
     *    - For each, move diagonally: (r+i, c+i) while in bounds
     * 
     * 2. Diagonals starting from the first row (row = 0), excluding (0,0):
     *    - Start points: (0,1), (0,2), ..., (0, C-1)
     *    - For each, move diagonally: (r+i, c+i) while in bounds
     * 
     * This ensures complete coverage without duplication.
     */
    private void scanDiagonalTLBR() {
        // Diagonals starting from first column
        for (int startRow = 0; startRow < rows; startRow++) {
            StringBuilder diag = new StringBuilder();
            List<int[]> coords = new ArrayList<>();
            
            int r = startRow, c = 0;
            while (r < rows && c < cols) {
                diag.append(grid[r][c]);
                coords.add(new int[]{r, c});
                r++;
                c++;
            }
            
            processDiagonalMatches(diag.toString(), coords, "DIAGONAL_TL_BR", "DIAGONAL_BR_TL");
        }
        
        // Diagonals starting from first row (excluding 0,0)
        for (int startCol = 1; startCol < cols; startCol++) {
            StringBuilder diag = new StringBuilder();
            List<int[]> coords = new ArrayList<>();
            
            int r = 0, c = startCol;
            while (r < rows && c < cols) {
                diag.append(grid[r][c]);
                coords.add(new int[]{r, c});
                r++;
                c++;
            }
            
            processDiagonalMatches(diag.toString(), coords, "DIAGONAL_TL_BR", "DIAGONAL_BR_TL");
        }
    }
    
    /**
     * Scans all Top-Right to Bottom-Left diagonals.
     * 
     * DIAGONAL ITERATION LOGIC (TR-BL):
     * ==================================
     * For a non-square R x C grid, there are (R + C - 1) diagonals going from
     * top-right to bottom-left.
     * 
     * Strategy:
     * 1. Diagonals starting from the last column (col = C-1):
     *    - Start points: (0, C-1), (1, C-1), (2, C-1), ..., (R-1, C-1)
     *    - For each, move diagonally: (r+i, c-i) while in bounds
     * 
     * 2. Diagonals starting from the first row (row = 0), excluding (0, C-1):
     *    - Start points: (0, C-2), (0, C-3), ..., (0, 0)
     *    - For each, move diagonally: (r+i, c-i) while in bounds
     * 
     * This ensures complete coverage without duplication.
     */
    private void scanDiagonalTRBL() {
        // Diagonals starting from last column
        for (int startRow = 0; startRow < rows; startRow++) {
            StringBuilder diag = new StringBuilder();
            List<int[]> coords = new ArrayList<>();
            
            int r = startRow, c = cols - 1;
            while (r < rows && c >= 0) {
                diag.append(grid[r][c]);
                coords.add(new int[]{r, c});
                r++;
                c--;
            }
            
            processDiagonalMatches(diag.toString(), coords, "DIAGONAL_TR_BL", "DIAGONAL_BL_TR");
        }
        
        // Diagonals starting from first row (excluding 0, C-1)
        for (int startCol = cols - 2; startCol >= 0; startCol--) {
            StringBuilder diag = new StringBuilder();
            List<int[]> coords = new ArrayList<>();
            
            int r = 0, c = startCol;
            while (r < rows && c >= 0) {
                diag.append(grid[r][c]);
                coords.add(new int[]{r, c});
                r++;
                c--;
            }
            
            processDiagonalMatches(diag.toString(), coords, "DIAGONAL_TR_BL", "DIAGONAL_BL_TR");
        }
    }
    
    /**
     * Processes matches found in a diagonal string and maps them back to 2D coordinates.
     * 
     * @param diag The 1D diagonal string
     * @param coords List of [row, col] pairs corresponding to each character in diag
     * @param forwardDir Direction label for forward matches
     * @param reverseDir Direction label for reverse matches
     */
    private void processDiagonalMatches(String diag, List<int[]> coords, String forwardDir, String reverseDir) {
        List<AhoCorasickAutomaton.Match> matches = automaton.search(diag);
        
        for (AhoCorasickAutomaton.Match match : matches) {
            String pattern = automaton.getPattern(match.patternId);
            int endIdx = match.endPos;
            int startIdx = endIdx - pattern.length() + 1;
            
            if (startIdx < 0 || endIdx >= coords.size()) continue;
            
            int[] startCoord = coords.get(startIdx);
            int[] endCoord = coords.get(endIdx);
            
            boolean isReversed = automaton.isReversedPattern(match.patternId);
            String direction = isReversed ? reverseDir : forwardDir;
            
            // For reversed patterns, swap start/end for intuitive display
            if (isReversed) {
                addFoundWord(new StringBuilder(pattern).reverse().toString(), 
                            endCoord[0], endCoord[1], startCoord[0], startCoord[1], direction);
            } else {
                addFoundWord(pattern, startCoord[0], startCoord[1], endCoord[0], endCoord[1], direction);
            }
        }
    }
    
    /**
     * Adds a found word to the results, avoiding duplicates.
     * 
     * @param word The word found
     * @param startRow Starting row coordinate
     * @param startCol Starting column coordinate
     * @param endRow Ending row coordinate
     * @param endCol Ending column coordinate
     * @param direction Direction label
     */
    private void addFoundWord(String word, int startRow, int startCol, int endRow, int endCol, String direction) {
        FoundWord foundWord = new FoundWord(word, startRow, startCol, endRow, endCol, direction);
        String key = foundWord.getKey();
        
        if (!uniqueWords.containsKey(key)) {
            uniqueWords.put(key, foundWord);
            results.add(foundWord);
        }
    }
    
}
