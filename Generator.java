/**
 * Class Name: Generator
 * Author: Eric Zhao
 * Version Information: Java 21, VS Code
 * Date: October 24, 2025
 * =========================================
 * Goal: To generate a complete word search puzzle and a 
 * corresponding solution key based on a list of input words 
 * and user-specified grid dimensions.
 * =========================================
 * Input: User supplies via GUI: (1) Input Word File name (words 4–8 chars, max 10, one per line); 
 *  (2) Grid Dimensions (R x C, 10 ≤ R, C ≤ 20, requiring validation/re-prompt);
 *  (3) Output file names (Solution and Puzzle).
 *  Output: Two text files (all letters CAPITALIZED):
 *  (1) Solution File: Shows word placement; empty cells are spaces. 
 * (2) Puzzle File: Shows word placement; empty cells are filled with random capital letters (no spaces).
 * Process:
 * Placement must attempt all 8 directions (Horizontal, Vertical, 2x Diagonal) and both orientations (Forward/Backward).
 *  Grid must be initialized, validated for size, words placed (with intersection allowed), and remaining cells filled with random characters.
 */

import java.awt.*;
import java.io.*;
import javax.swing.*;

public class Generator {

    /** Simple holder for validated user input from the GUI. */
    public static class UserInput {
        public final File inputFile;
        public final int rows;
        public final int cols;
        public final String solutionFileName;
        public final String puzzleFileName;
        public final boolean forceIntersection;

        public UserInput(File inputFile, int rows, int cols, String solutionFileName, String puzzleFileName, boolean forceIntersection) {
            this.inputFile = inputFile;
            this.rows = rows;
            this.cols = cols;
            this.solutionFileName = solutionFileName;
            this.puzzleFileName = puzzleFileName;
            this.forceIntersection = forceIntersection;
        }
    }

    // Helper: check if filename has an extension
    private static boolean hasExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 && dot < filename.length() - 1;
    }

    /**
     * Helper: check for valid Windows filename (no \/:*?"<>|)
     * @param filename the filename to check
     * @return true if valid, false otherwise
     */
    private static boolean isValidWindowsFilename(String filename) {
        return !filename.matches("[\\\\/:*?\"<>|]+") && !filename.contains("\0");
    }

    /**
     * Main method to launch the GUI
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Set system look and feel for better GUI appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel if system look and feel fails
        }
        
        while (true) { // Loop to allow restarting
            final UserInput[] holder = new UserInput[1];
            try {
                javax.swing.SwingUtilities.invokeAndWait(() -> holder[0] = createAndShowGUI());
            } catch (InterruptedException | java.lang.reflect.InvocationTargetException ex) {
                System.err.println("Failed to open GUI: " + ex.getMessage());
                break;
            }

            if (holder[0] != null) {
                UserInput input = holder[0];
                // Instantiate Builder with validated input and generate word search
                Builder builder = new Builder(
                    input.inputFile,
                    input.rows,
                    input.cols,
                    input.solutionFileName,
                    input.puzzleFileName,
                    input.forceIntersection
                );
                
                // Generate the word search puzzle and solution files
                System.out.println("Generating word search puzzle...");
                builder.begin();
                System.out.println("Word search generation complete!");
                System.out.println("Solution file: " + input.solutionFileName);
                System.out.println("Puzzle file: " + input.puzzleFileName);

                // Prompt user to restart or quit
                int choice = JOptionPane.showConfirmDialog(
                    null,
                    "Word search generation complete!\n" +
                    "Solution file: " + input.solutionFileName + "\n" +
                    "Puzzle file: " + input.puzzleFileName + "\n\n" +
                    "Would you like to generate another word search?",
                    "Generation Complete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
                );

                if (choice != JOptionPane.YES_OPTION) {
                    System.out.println("Exiting program. Goodbye!");
                    break; // Exit the loop and end the program
                }
                System.out.println("\n--- Restarting Word Search Generator ---\n");
            } else {
                // User closed the dialog without submitting
                System.out.println("No input provided. Exiting program.");
                break;
            }
        }
    }


    /**     
     * Creates and displays the GUI for the Word Search Generator.
     * Includes fields for input file selection, grid dimensions, and output file names.
     * @return UserInput object containing validated user inputs.
     */

    public static UserInput createAndShowGUI() {
        // Use a modal dialog so the method blocks until user submits
        JDialog dialog = new JDialog((Frame) null, "Word Search Generator", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 350);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

    JLabel fileLabel = new JLabel("Input Word File (.txt):");
    JTextField fileField = new JTextField(24); // give it a sensible width
        fileField.setEditable(false);
        JButton browseButton = new JButton("Browse");

        JLabel rowsLabel = new JLabel("Rows (10-20):");
    JTextField rowsField = new JTextField(6);

        JLabel colsLabel = new JLabel("Columns (10-20):");
    JTextField colsField = new JTextField(6);

        JLabel solLabel = new JLabel("Solution Output File:");
    JTextField solField = new JTextField(24);

        JLabel puzzleLabel = new JLabel("Puzzle Output File:");
    JTextField puzzleField = new JTextField(24);

        JCheckBox forceIntersectionCheckBox = new JCheckBox("Force at least one word intersection", true);
        forceIntersectionCheckBox.setToolTipText("When enabled, the generator will attempt to ensure at least one word shares a letter with another word");

        JButton submitButton = new JButton("Submit");

        // File chooser
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));//assume the word list is in the same directory because assignment said so!
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
        browseButton.addActionListener(e -> {
            // touch event param to avoid unused warning
            e.getSource();
            int result = fileChooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selected = fileChooser.getSelectedFile();
                if (selected.getName().toLowerCase().endsWith(".txt")) {
                    fileField.setText(selected.getName()); // Show only file name
                    fileField.setToolTipText(selected.getAbsolutePath()); // Show full path as tooltip
                } else {
                    JOptionPane.showMessageDialog(dialog, "Please select a .txt file.", "Invalid File", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

    // Layout
    gbc.weightx = 0; addComponent(dialog, fileLabel, gbc, 0, 0, 1);
    gbc.weightx = 1.0; addComponent(dialog, fileField, gbc, 1, 0, 1);
    gbc.weightx = 0; addComponent(dialog, browseButton, gbc, 2, 0, 1);
        
    gbc.weightx = 0; addComponent(dialog, rowsLabel, gbc, 0, 1, 1);
    gbc.weightx = 1.0; addComponent(dialog, rowsField, gbc, 1, 1, 2);
        
    gbc.weightx = 0; addComponent(dialog, colsLabel, gbc, 0, 2, 1);
    gbc.weightx = 1.0; addComponent(dialog, colsField, gbc, 1, 2, 2);
        
    gbc.weightx = 0; addComponent(dialog, solLabel, gbc, 0, 3, 1);
    gbc.weightx = 1.0; addComponent(dialog, solField, gbc, 1, 3, 2);
        
    gbc.weightx = 0; addComponent(dialog, puzzleLabel, gbc, 0, 4, 1);
    gbc.weightx = 1.0; addComponent(dialog, puzzleField, gbc, 1, 4, 2);
        
    gbc.weightx = 1.0; addComponent(dialog, forceIntersectionCheckBox, gbc, 0, 5, 3);
        
    gbc.weightx = 0; addComponent(dialog, submitButton, gbc, 1, 6, 2);

        // Submit button logic
        final UserInput[] resultHolder = new UserInput[1];
        submitButton.addActionListener(e -> {
            // touch event param to avoid unused warning
            e.getSource();
            while (true) {
                String displayName = fileField.getText().trim();
                String filePath = fileField.getToolTipText(); // full path stored as tooltip
                String rowsText = rowsField.getText().trim();
                String colsText = colsField.getText().trim();
                String solName = solField.getText().trim();
                String puzzleName = puzzleField.getText().trim();

                // Validate input file
                if (displayName.isEmpty() || filePath == null || !filePath.toLowerCase().endsWith(".txt")) {
                    JOptionPane.showMessageDialog(dialog, "Please select a valid .txt input file.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    break;
                }
                File inputFile = new File(filePath);
                if (!inputFile.exists() || !inputFile.isFile()) {
                    JOptionPane.showMessageDialog(dialog, "Input file does not exist.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    break;
                }

                // Validate grid dimensions
                int rows, cols;
                try {
                    rows = Integer.parseInt(rowsText);
                    cols = Integer.parseInt(colsText);
                    if (rows < 10 || rows > 20 || cols < 10 || cols > 20) {
                        JOptionPane.showMessageDialog(dialog, "Rows and columns must be between 10 and 20.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Rows and columns must be integers.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    break;
                }

                // Validate output file names (must have extension and be valid Windows file names since schools computers use windows)
                if (solName.isEmpty() || puzzleName.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter output file names for both solution and puzzle.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (!hasExtension(solName) || !hasExtension(puzzleName)) {
                    JOptionPane.showMessageDialog(dialog, "Output file names must have an extension (e.g., .txt, .dat, .out)", "Input Error", JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (!isValidWindowsFilename(solName) || !isValidWindowsFilename(puzzleName)) {
                    JOptionPane.showMessageDialog(dialog, "Output file names contain invalid characters.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    break;
                }

                // All inputs valid
                JOptionPane.showMessageDialog(dialog, "Inputs accepted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                resultHolder[0] = new UserInput(inputFile, rows, cols, solName, puzzleName, forceIntersectionCheckBox.isSelected());
                dialog.dispose();
                break;
            }
        });

        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true); // modal: blocks until disposed
        return resultHolder[0];
    }

    /**
     * Helper method to add a component to a container with GridBagConstraints
     * @param container The container to add the component to
     * @param component The component to add
     * @param gbc The GridBagConstraints object
     * @param x The gridx position
     * @param y The gridy position
     * @param width The gridwidth (number of columns to span)
     */
    private static void addComponent(Container container, Component component, 
                                     GridBagConstraints gbc, int x, int y, int width) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        container.add(component, gbc);
        gbc.gridwidth = 1; // Reset to default
    }

}