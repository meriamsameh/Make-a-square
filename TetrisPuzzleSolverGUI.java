import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TetrisPuzzleSolverGUI extends JFrame {
    private JTextField numPiecesField;
    private JPanel puzzleBoardPanel;
    private JButton submitButton;
    private JButton solveButton;

    private int numPieces;
    private Color[] pieceColors;

    public TetrisPuzzleSolverGUI() {
        super("Tetris Puzzle Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        mainPanel.add(createInputPanel(), BorderLayout.WEST);

        puzzleBoardPanel = new JPanel();
        puzzleBoardPanel.setLayout(new GridLayout(0, 1));
        puzzleBoardPanel.setBorder(BorderFactory.createTitledBorder("Puzzle Board"));
        mainPanel.add(puzzleBoardPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Initialize the pieceColors array
        pieceColors = new Color[7];
        pieceColors[0] = Color.RED;
        pieceColors[1] = Color.GREEN;
        pieceColors[2] = Color.BLUE;
        pieceColors[3] = Color.YELLOW;
        pieceColors[5] = Color.ORANGE;
        pieceColors[4] = Color.CYAN;
        pieceColors[6] = Color.MAGENTA;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 1, 10, 10));

        inputPanel.add(new JLabel("Enter the number of pieces:"));
        numPiecesField = new JTextField();
        inputPanel.add(numPiecesField);

        submitButton = new JButton("Submit");
        inputPanel.add(submitButton);

        solveButton = new JButton("Solve Puzzle");
        inputPanel.add(solveButton);

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitPieces();
            }
        });

        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solvePuzzle();
            }
        });

        return inputPanel;
    }

    private void submitPieces() {
        String numPiecesText = numPiecesField.getText();

        if (numPiecesText != null && !numPiecesText.isEmpty()) {
            try {
                numPieces = Integer.parseInt(numPiecesText);
                JOptionPane.showMessageDialog(this, "Number of pieces submitted successfully!");
                submitButton.setEnabled(false);
                solveButton.setEnabled(true);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input for the number of pieces. Please enter a valid integer.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please enter the number of pieces.");
        }
    }

    private void solvePuzzle() {
        char[][][] pieces = new char[numPieces][][];
        for (int i = 0; i < numPieces; i++) {
            try {
                int rows = Integer.parseInt(JOptionPane.showInputDialog("Enter the number of rows for piece " + (i + 1) + ":"));
                int cols = Integer.parseInt(JOptionPane.showInputDialog("Enter the number of columns for piece " + (i + 1) + ":"));

                char[][] piece = new char[rows][cols];

                for (int j = 0; j < rows; j++) {
                    String line = JOptionPane.showInputDialog("Enter the shape of the piece (0 or 1) for row " + (j + 1) + ":");
                    if (line == null) {
                        return;
                    }
                    piece[j] = line.toCharArray();
                }

                pieces[i] = piece;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter valid integers for rows and columns.");
                return;
            }
        }

        JOptionPane.showMessageDialog(this, "Puzzle solved! Output printed to the console.");
        int[][] solvedBoard = TetrisPuzzleSolver.solvePuzzle(pieces);
        printOutputToConsole(solvedBoard);
    }

    private void printOutputToConsole(int[][] solvedBoard) {
        if (solvedBoard != null) {
            puzzleBoardPanel.removeAll();

            for (int i = 0; i < solvedBoard.length; i++) {
                JPanel rowPanel = new JPanel(new GridLayout(0, solvedBoard[i].length));
                for (int j = 0; j < solvedBoard[i].length; j++) {
                    JPanel cellPanel = new JPanel();
                    cellPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    cellPanel.setPreferredSize(new Dimension(40, 40)); // Adjust the size as needed

                    int pieceNumber = solvedBoard[i][j] - 1;
                    if (pieceNumber >= 0 && pieceNumber < pieceColors.length) {
                        cellPanel.setBackground(pieceColors[pieceNumber]);
                    }

                    rowPanel.add(cellPanel);
                }
                puzzleBoardPanel.add(rowPanel);
            }

            puzzleBoardPanel.revalidate();
            puzzleBoardPanel.repaint();
        } else {
            JOptionPane.showMessageDialog(this, "No solution possible");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TetrisPuzzleSolverGUI().setVisible(true);
            }
        });
    }
}