/**
 * Represents a word's placement and direction in the word search grid.
 * Makes word placement and searching easier.
 */
public class WordVector {
    final private String word;
    final private int startRow;
    final private int startCol;
    final private int dx; // direction x (column step)
    final private int dy; // direction y (row step)
    final private boolean forward; // true: forward, false: backward

    /**
     * Constructs a WordVector for a word at a given start position and direction.
     * @param word the word to place
     * @param startRow starting row (0-based)
     * @param startCol starting column (0-based)
     * @param dx direction x (column step: -1, 0, 1)
     * @param dy direction y (row step: -1, 0, 1)
     * @param forward true for forward, false for backward
     */
    public WordVector(String word, int startRow, int startCol, int dx, int dy, boolean forward) {
        this.word = word;
        this.startRow = startRow;
        this.startCol = startCol;
        this.dx = dx;
        this.dy = dy;
        this.forward = forward;
    }

    /** Returns the word. */
    public String getWord() { return word; }
    /** Returns the starting row. */
    public int getStartRow() { return startRow; }
    /** Returns the starting column. */
    public int getStartCol() { return startCol; }
    /** Returns the direction x. */
    public int getDx() { return dx; }
    /** Returns the direction y. */
    public int getDy() { return dy; }
    /** Returns true if forward, false if backward. */
    public boolean isForward() { return forward; }

    /**
     * Gets the row and column for the nth letter in the word.
     * @param n index in word (0-based)
     * @return int[]{row, col}
     */
    public int[] getCell(int n) {
        int step = forward ? n : word.length() - 1 - n;
        int row = startRow + dy * step;
        int col = startCol + dx * step;
        return new int[]{row, col};
    }

    /**
     * Returns the path of all cells for this word in the grid.
     * @return array of int[]{row, col}
     */
    public int[][] getPath() {
        int[][] path = new int[word.length()][2];
        for (int i = 0; i < word.length(); i++) {
            path[i] = getCell(i);
        }
        return path;
    }
}
