import java.io.*;
import java.util.*;

/**
 * Builder is responsible for constructing a word-search puzzle and its
 * corresponding solution output files from an input word list. Usage:
 *
 * <pre>
 * Builder b = new Builder(inputFile, rows, cols, "solution.txt", "puzzle.txt", true);
 * b.begin(); // runs generation and writes output files
 * </pre>
 *
 * Design notes:
 * - The generator attempts to place words randomly but includes an optional
 *   intersection-first step to guarantee at least one shared-letter intersection
 *   when possible (this satisfies the assignment requirement to allow words
 *   to intersect). This behavior can be controlled via the forceIntersection
 *   constructor parameter.
 */
public class Builder {
	// Constants
	private static final int MAX_PLACEMENT_ATTEMPTS = 1000;
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

	/**
	 * Constructs a Builder with all required data for word search generation.
	 * @param inputFile the input word file
	 * @param rows number of rows in the grid
	 * @param cols number of columns in the grid
	 * @param solutionFileName output file name for the solution
	 * @param puzzleFileName output file name for the puzzle
	 * @param forceIntersection if true, attempts to guarantee at least one word intersection
	 */
	public Builder(File inputFile, int rows, int cols, String solutionFileName, String puzzleFileName, boolean forceIntersection) {
		this.inputFile = inputFile;
		this.rows = rows;
		this.cols = cols;
		this.solutionFileName = solutionFileName;
		this.puzzleFileName = puzzleFileName;
		this.forceIntersection = forceIntersection;
	}

	/**
	 * Main entry point for generation workflow. Call after constructing Builder.
	 */
	public void begin() {
		try {
			generate();
		} catch (IOException e) {
			System.err.println("Error during word search generation: " + e.getMessage());
		}
	}

	/**
	 * Main generation method implementing the complete word search algorithm.
	 * Responsibilities:
	 *   - Read words from the provided input file
	 *   - Initialize the grid
	 *   - Place words using an intersection-first strategy (if available) then
	 *       randomized placement
	 *   - Write the solution file, fill empty cells, and write the puzzle file
	 *   @void no return value
	 * @throws IOException if there is a problem reading the input or writing
	 *                     the output files
	 */
	public void generate() throws IOException {
		// Step 1: Initialization and Setup
		List<String> wordsToPlace = new ArrayList<>();
		
		// Read words from file and convert to uppercase
		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				wordsToPlace.add(line.trim().toUpperCase());
			}
		}

		// Create and initialize the grid with null characters
		grid = new char[rows][cols];
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				grid[r][c] = '\0';
			}
		}

		// Prepare helper data
		successfullyPlacedWords = new ArrayList<>();
		random = new Random();

		// Step 2: Core Word Placement Algorithm
		// NOTE: The generator can optionally force at least one intersection
		// between words (shared letters) to demonstrate the assignment requirement
		// "Can the words in the puzzle intersect & have letters on the border? (ie. Share same letters)".
		//
		// This behavior is controlled by the forceIntersection field set via the constructor.
		// When enabled, the code tries an intersection-first placement for the first word
		// that can intersect with previously placed words. Once an intersection is achieved,
		// further words are placed using random placement.
		boolean hasIntersection = false; // Track if we've achieved at least one intersection
		
		for (String word : wordsToPlace) {
			boolean placed = false;
			
			// If forceIntersection is enabled and we haven't achieved an intersection yet
			if (forceIntersection && !hasIntersection && !successfullyPlacedWords.isEmpty()) {
				placed = tryPlaceWordWithIntersection(word);
				if (placed) {
					hasIntersection = true; // Mark that we've achieved our required intersection
				}
			}
			
			// If intersection placement failed or not attempted, use random placement
			if (!placed) {
				int attempts = 0;
				while (!placed && attempts < MAX_PLACEMENT_ATTEMPTS) {
					// Randomly select placement parameters
					boolean forward = random.nextBoolean();
					
					// Select one of the 8 directions
					int[] randomDirection = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
					int dx = randomDirection[0];
					int dy = randomDirection[1];
					
					int startRow = random.nextInt(rows);
					int startCol = random.nextInt(cols);

					// Check if placement is valid
					if (canPlaceWord(word, startRow, startCol, dx, dy, forward)) {
						// Place the word
						placeWord(word, startRow, startCol, dx, dy, forward);
						placed = true;
					}
					
					attempts++;
				}
			}
			
			if (!placed) {
				System.out.println("Warning: Could not place word: " + word);
			}
		}

		// Step 3: Finalize the Grid and Create Output Files
		writeSolutionFile();
		fillEmptyCells();
		writePuzzleFile();
	}

	/**
	 * Checks if a word can be placed at the given position and direction.
	 * @param word the word to place
	 * @param startRow starting row
	 * @param startCol starting column
	 * @param dx direction x
	 * @param dy direction y
	 * @param forward true for forward, false for backward
	 * @return true if the word can be placed, false otherwise
	 */
	private boolean canPlaceWord(String word, int startRow, int startCol, int dx, int dy, boolean forward) {
		// Calculate end position
		int endRow = startRow + dy * (word.length() - 1);
		int endCol = startCol + dx * (word.length() - 1);

		// Boundary check
		if (endRow < 0 || endRow >= rows || endCol < 0 || endCol >= cols) {
			return false;
		}

		// Intersection/Conflict check
		for (int i = 0; i < word.length(); i++) {
			int step = forward ? i : word.length() - 1 - i;
			int row = startRow + dy * step;
			int col = startCol + dx * step;

			char currentChar = grid[row][col];
			char wordChar = word.charAt(i);

			// If cell is empty, it's available
			if (currentChar == '\0') {
				continue;
			}

			// If cell has a character, it must match for valid intersection
			if (currentChar != wordChar) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Tries to place a word with intersection to an already placed word.
	 * Finds common letters between the new word and placed words, and attempts placement.
	 * @param word the word to place
	 * @return true if word was placed with intersection, false otherwise
	 */
	private boolean tryPlaceWordWithIntersection(String word) {
		// Collect all potential intersection points
		List<IntersectionPoint> potentialStarts = new ArrayList<>();
		
		for (WordVector placed : successfullyPlacedWords) {
			String placedWord = placed.getWord();
			
			// Find common letters between words
			for (int i = 0; i < word.length(); i++) {
				for (int j = 0; j < placedWord.length(); j++) {
					if (word.charAt(i) == placedWord.charAt(j)) {
						// Get the cell position of the placed word's character
						int[] cell = placed.getCell(j);
						// Store this as a potential intersection point
						potentialStarts.add(new IntersectionPoint(
							cell[0], cell[1], i
						));
					}
				}
			}
		}
		
		// Shuffle to try intersections in random order
		Collections.shuffle(potentialStarts, random);
		
		// Try each potential intersection point
		for (IntersectionPoint point : potentialStarts) {
			int intersectRow = point.row;
			int intersectCol = point.col;
			int wordIndex = point.wordIndex;
			
			// Try all 8 directions from this intersection point
			for (int[] dir : DIRECTIONS) {
				for (boolean forward : new boolean[]{true, false}) {
					// Calculate start position so that wordIndex aligns with intersection
					int step = forward ? wordIndex : word.length() - 1 - wordIndex;
					int startRow = intersectRow - dir[1] * step;
					int startCol = intersectCol - dir[0] * step;
					
					if (canPlaceWord(word, startRow, startCol, dir[0], dir[1], forward)) {
						placeWord(word, startRow, startCol, dir[0], dir[1], forward);
						
						// NOTE: Intersection was placed here. The placement above was chosen so
						// that the new word shares the character at (intersectRow, intersectCol)
						// with an existing placed word. This behavior is intentional to meet
						// the assignment requirement that words may intersect (share letters).
						return true;
					}
				}
			}
		}
		
		return false;
	}

	/**
	 * Helper class to store intersection point information.
	 * Represents a potential location where a new word can intersect with an existing word.
	 */
	private static class IntersectionPoint {
		int row, col, wordIndex;
		
		IntersectionPoint(int row, int col, int wordIndex) {
			this.row = row;
			this.col = col;
			this.wordIndex = wordIndex;
		}
	}

	/**
	 * Places a word on the grid at the specified position and direction.
	 * @param word the word to place
	 * @param startRow starting row
	 * @param startCol starting column
	 * @param dx direction x
	 * @param dy direction y
	 * @param forward true for forward, false for backward
	 */
	private void placeWord(String word, int startRow, int startCol, int dx, int dy, boolean forward) {
		// Create WordVector for tracking
		WordVector wordVector = new WordVector(word, startRow, startCol, dx, dy, forward);
		successfullyPlacedWords.add(wordVector);

		// Place each letter on the grid
		for (int i = 0; i < word.length(); i++) {
			int step = forward ? i : word.length() - 1 - i;
			int row = startRow + dy * step;
			int col = startCol + dx * step;
			grid[row][col] = word.charAt(i);
		}
	}

	/**
	 * Writes the solution file showing only placed words (empty cells as spaces).
	 * The solution file contains the grid with empty cells written as spaces
	 * and letters where words were placed.
	 *
	 * @throws IOException if an I/O error occurs while writing to
	 *                     {@link #solutionFileName}
	 */
	private void writeSolutionFile() throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(solutionFileName))) {
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					if (grid[r][c] == '\0') {
						writer.write(' ');
					} else {
						writer.write(grid[r][c]);
					}
				}
				writer.newLine();
			}
		}
	}

	/**
	 * Fills all empty cells with random capital letters.
	 */
	private void fillEmptyCells() {
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (grid[r][c] == '\0') {
					grid[r][c] = (char) ('A' + random.nextInt(26));
				}
			}
		}
	}

	/**
	 * Writes the puzzle file with all cells filled (no spaces). This should be
	 * called after {@link #fillEmptyCells()} so that every cell contains a
	 * capital letter.
	 *
	 * @throws IOException if an I/O error occurs while writing to
	 *                     {@link #puzzleFileName}
	 */
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



	/**
	 * Reads all words from the specified text file and returns them as a String array.
	 * Each line in the file is treated as a separate word. Assumes no blank lines.
	 *
	 * @param file the input text file containing words (one per line)
	 * @return a String array of words read from the file
	 * @throws IOException if an I/O error occurs while reading the file
	 */
	public static String[] readWordsFromFile(File file) throws IOException {
		// First, count the number of lines (words)
		int count = 0;
		BufferedReader counter = new BufferedReader(new FileReader(file));
		while (counter.readLine() != null) {
			count++;
		}
		counter.close();

		//read the words into an array of the correct size
		String[] words = new String[count];
		BufferedReader reader = new BufferedReader(new FileReader(file));
		int i = 0;
		String line;
		while ((line = reader.readLine()) != null) {
			words[i] = line.trim(); // Remove leading/trailing whitespace
			i++;
		}
		reader.close();
		return words;
	}
}
