import java.io.*;
import java.util.*;

/*
 * Class Name: Builder
 * Author: Eric Zhao
 * Version information: Java 21, VS Code
 * Date: October 23, 2025

 ========
 * Input: A word list file (one word per line), desired grid dimensions (rows x cols),
 *        output file names for solution and puzzle, and a boolean flag 'forceIntersection'
 *        which requests that the generator attempt to create at least one shared-letter
 *        intersection between placed words when possible.
 * Output: Two text files: a solution file showing placed words with empty cells as spaces,
 *         and a puzzle file where all empty cells are filled with random capital letters.
 * Process: Reads the input words, initializes an empty grid, places words using an
 *         intersection-first attempt (if requested) followed by randomized placement,
 *         fills remaining cells with random letters, and writes both output files.
 ======
 */
public class Builder {
	// Constants
	private static final int[][] DIRECTIONS = {
		{1, 0}, {-1, 0}, {0, 1}, {0, -1},   // Horizontal and Vertical
		{1, 1}, {-1, -1}, {1, -1}, {-1, 1}  // Diagonals
	};

	// Instance fields
	private final File inputFile;
	private final int rows;
	private final int cols;
	private final String solutionFileName;
	private final String puzzleFileName;
	private final boolean forceIntersection;
	private char[][] grid;
	private List<WordVector> successfullyPlacedWords;
	private Random random;

	public Builder(File inputFile, int rows, int cols, String solutionFileName, String puzzleFileName, boolean forceIntersection) {
		this.inputFile = inputFile;
		this.rows = rows;
		this.cols = cols;
		this.solutionFileName = solutionFileName;
		this.puzzleFileName = puzzleFileName;
		this.forceIntersection = forceIntersection;
	}

	public void begin() {
		try {
			generate();
		} catch (IOException e) {
			System.err.println("Error during word search generation: " + e.getMessage());
		}
	}

	public void generate() throws IOException {
		// Read words from file and convert to uppercase
		List<String> wordsToPlace = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				wordsToPlace.add(line.trim().toUpperCase());
			}
		}

		// Initialize grid and placed words list
		grid = new char[rows][cols];
		successfullyPlacedWords = new ArrayList<>();
		random = new Random();

		// === New: Recursive Backtracking Placement ===
		boolean allPlaced = placeWordsRecursively(wordsToPlace, 0);
		if (!allPlaced) {
			System.out.println("Warning: Not all words could be placed!");
		}

		// Write outputs
		writeSolutionFile();
		fillEmptyCells();
		writePuzzleFile();
	}

	// === Recursive backtracking main method ===
	private boolean placeWordsRecursively(List<String> words, int index) {
		if (index == words.size()) return true; // All words placed!

		String word = words.get(index);

		// Shuffle choices for non-determinism
		List<int[]> directionsShuffled = new ArrayList<>(Arrays.asList(DIRECTIONS));
	 Collections.shuffle(directionsShuffled, random);

		for (int[] dir : directionsShuffled) {
			for (boolean forward : new boolean[]{true, false}) {
				for (int row = 0; row < rows; row++) {
					for (int col = 0; col < cols; col++) {
						if (canPlaceWord(word, row, col, dir[0], dir[1], forward)) {
							placeWord(word, row, col, dir[0], dir[1], forward);
							if (placeWordsRecursively(words, index + 1)) {
								return true;
						 }
							// Backtrack!
							unplaceWord(word, row, col, dir[0], dir[1], forward);
						}
					}
				}
			}
		}
		return false; // Couldn't place this word here
	}

	// Remove word from grid and placed list
	private void unplaceWord(String word, int startRow, int startCol, int dx, int dy, boolean forward) {
		for (int i = 0; i < word.length(); i++) {
			int step = forward ? i : word.length() - 1 - i;
			int row = startRow + dy * step;
			int col = startCol + dx * step;

			// Only remove letter if it was placed by this word (check if it is not used by another word!)
			boolean usedElsewhere = false;
			for (WordVector wv : successfullyPlacedWords) {
				if (wv != null && wv != successfullyPlacedWords.get(successfullyPlacedWords.size() - 1)) {
					int[] cell = wv.getCellForLetter(grid[row][col], row, col);
					if (cell != null) {
						usedElsewhere = true;
						break;
					}
				}
			}
			if (!usedElsewhere) {
				grid[row][col] = '\0';
			}
		}
		// Remove from placed words list
		if (!successfullyPlacedWords.isEmpty()) {
			successfullyPlacedWords.remove(successfullyPlacedWords.size() - 1);
		}
	}

	// Checks if a word can be placed at the given position and direction.
	private boolean canPlaceWord(String word, int startRow, int startCol, int dx, int dy, boolean forward) {
		int endRow = startRow + dy * (word.length() - 1);
		int endCol = startCol + dx * (word.length() - 1);
		if (endRow < 0 || endRow >= rows || endCol < 0 || endCol >= cols) return false;

		for (int i = 0; i < word.length(); i++) {
			int step = forward ? i : word.length() - 1 - i;
			char currentChar = grid[startRow + dy * step][startCol + dx * step];
			if (currentChar != '\0' && currentChar != word.charAt(i)) return false;
		}
		return true;
	}

	private void placeWord(String word, int startRow, int startCol, int dx, int dy, boolean forward) {
		WordVector wordVector = new WordVector(word, startRow, startCol, dx, dy, forward);
		successfullyPlacedWords.add(wordVector);

		for (int i = 0; i < word.length(); i++) {
			int step = forward ? i : word.length() - 1 - i;
			int row = startRow + dy * step;
			int col = startCol + dx * step;
			grid[row][col] = word.charAt(i);
		}
	}

	// ==== The following are unchanged ====

	private void writeSolutionFile() throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(solutionFileName))) {
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					writer.write(grid[r][c] == '\0' ? ' ' : grid[r][c]);
				}
				writer.newLine();
			}
		}
	}

	private void fillEmptyCells() {
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (grid[r][c] == '\0') grid[r][c] = (char) ('A' + random.nextInt(26));
			}
		}
	}

	private void writePuzzleFile() throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(puzzleFileName))) {
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					writer.write(grid[r][c]);
				}
				writer.newLine();
			}
		}
	}

	// WordVector should have this method to help unplacing
	private static class WordVector {
		private final String word;
		private final int startRow, startCol, dx, dy;
		private final boolean forward;

		public WordVector(String word, int startRow, int startCol, int dx, int dy, boolean forward) {
			this.word = word;
			this.startRow = startRow;
			this.startCol = startCol;
			this.dx = dx;
			this.dy = dy;
			this.forward = forward;
		}

		public String getWord() { return word; }
		// Get the position of the j-th letter in this word
		public int[] getCell(int j) {
			int step = forward ? j : word.length() - 1 - j;
			int row = startRow + dy * step;
			int col = startCol + dx * step;
			return new int[]{row, col};
		}
		// For unplacing: See if this word uses a given letter in position (row,col)
		public int[] getCellForLetter(char ch, int row, int col) {
			for (int i = 0; i < word.length(); i++) {
				int[] cell = getCell(i);
				if (cell[0] == row && cell[1] == col && word.charAt(i) == ch) {
					return cell;
				}
			}
			return null;
		}
	}
}
