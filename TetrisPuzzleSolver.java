import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class TetrisPuzzleSolver {
    private static final int THREAD_POOL_SIZE = 4;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Read the number of pieces
        System.out.print("Enter the number of pieces: ");
        int numPieces = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        // Initialize the pieces array
        char[][][] pieces = new char[numPieces][][];

        // Read each piece from the user
        for (int i = 0; i < numPieces; i++) {
            System.out.println("Enter the details for piece " + (i + 1) + ":");

            // Read the number of rows and columns
            System.out.print("Number of rows: ");
            int rows = scanner.nextInt();
            System.out.print("Number of columns: ");
            int cols = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            // Initialize the piece array
            char[][] piece = new char[rows][cols];

            // Read the shape of the piece
            System.out.println("Enter the shape of the piece (0 or 1):");
            for (int j = 0; j < rows; j++) {
                String line = scanner.nextLine();
                piece[j] = line.toCharArray();
            }

            // Store the piece in the pieces array
            pieces[i] = piece;
        }

        // Close the scanner
        scanner.close();

        // Solve the puzzle
        int[][] solvedBoard = solvePuzzle(pieces);

        // Output the result
        if (solvedBoard != null) {
            for (int i = 0; i < solvedBoard.length; i++) {
                for (int j = 0; j < solvedBoard[i].length; j++) {
                    System.out.print(solvedBoard[i][j] + " ");
                }
                System.out.println();
            }
        } else {
            System.out.println("No solution possible");
        }

        // Shutdown the executor service
        executorService.shutdown();
    }

    static int[][] solvePuzzle(char[][][] pieces) {
        List<Integer> availablePieceNumbers = new ArrayList<>();
        for (int i = 1; i <= pieces.length; i++) {
            availablePieceNumbers.add(i);
        }

        // Create a 4x4 board
        int[][] board = new int[4][4];

        // Create a list to hold the Callable tasks
        List<Callable<int[][]>> tasks = new ArrayList<>();

        // Start the recursive backtracking using multiple threads
        for (int i = 0; i < pieces.length; i++) {
            int finalI = i;
            tasks.add(() -> solveHelper(pieces, new ArrayList<>(availablePieceNumbers), finalI, copyBoard(board)));
        }

        try {
            // Invoke all tasks and get the results
            List<Future<int[][]>> results = executorService.invokeAll(tasks);

            // Check if any thread found a solution
            for (Future<int[][]> result : results) {
                int[][] resultBoard = result.get();
                if (resultBoard != null) {
                    return resultBoard;  // Return the solved board
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;  // No solution found
    }

    private static int[][] solveHelper(char[][][] pieces, List<Integer> availablePieceNumbers, int pieceIndex, int[][] board) {
        // If all pieces are placed, check if the solution is valid
        if (pieceIndex == pieces.length) {
            return isValidSolution(board) ? board : null;
        }

        int rows = board.length;
        int cols = board[0].length;

        // Try placing the current piece in different orientations and positions
        for (int i = 0; i < 4; i++) {  // 0 to 3: rotations
            for (int j = 0; j < 2; j++) {  // 0 to 1: flips
                for (int row = 0; row < rows; row++) {
                    for (int col = 0; col < cols; col++) {
                        // Rotate and flip the piece
                        char[][] rotatedPiece = rotatePiece(pieces[pieceIndex], i);
                        if (j == 1) {
                            rotatedPiece = flipPiece(rotatedPiece);
                        }

                        // Try each available piece number and position
                        for (int pieceNumber : availablePieceNumbers) {
                            // Check if the piece can be placed at the current position
                            if (canPlacePiece(rotatedPiece, row, col, board)) {
                                // Place the piece on a new board
                                int[][] newBoard = copyBoard(board);
                                placePiece(rotatedPiece, row, col, pieceNumber, newBoard);

                                // Recursively try the next piece
                                int[][] resultBoard = solveHelper(pieces, removePieceNumber(availablePieceNumbers, pieceNumber), pieceIndex + 1, newBoard);
                                if (resultBoard != null) {
                                    return resultBoard;  // Found a solution
                                }

                                // Backtrack by removing the piece from the board
                                // (no need to modify the original board)
                            }
                        }
                    }
                }
            }
        }

        // If no solution is found for the current piece, backtrack
        return null;
    }

    private static boolean isValidSolution(int[][] board) {
        // Check if the board is completely filled
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == 0) {
                    return false;  // Empty cell found, not a valid solution
                }
            }
        }
        return true;  // All cells are covered, valid solution
    }

    private static boolean canPlacePiece(char[][] piece, int row, int col, int[][] board) {
        int pieceRows = piece.length;
        int pieceCols = piece[0].length;

        // Check if the piece fits within the board bounds
        if (row < 0 || col < 0 || row + pieceRows > board.length || col + pieceCols > board[0].length) {
            return false;
        }

        // Check if the cells are empty in the board
        for (int i = 0; i < pieceRows; i++) {
            for (int j = 0; j < pieceCols; j++) {
                if (piece[i][j] == '1') {
                    int newRow = row + i;
                    int newCol = col + j;

                    if (newRow < 0 || newRow >= board.length || newCol < 0 || newCol >= board[0].length || board[newRow][newCol] != 0) {
                        return false;  // Cell is already occupied or out of bounds
                    }
                }
            }
        }

        return true;  // All cells are empty, piece can be placed
    }

    private static void placePiece(char[][] piece, int row, int col, int pieceNumber, int[][] board) {
        int pieceRows = piece.length;
        int pieceCols = piece[0].length;

        for (int i = 0; i < pieceRows; i++) {
            for (int j = 0; j < pieceCols; j++) {
                if (piece[i][j] == '1') {
                    board[row + i][col + j] = pieceNumber;
                }
            }
        }
    }

    private static char[][] rotatePiece(char[][] piece, int rotations) {
        int rows = piece.length;
        int cols = piece[0].length;

        char[][] rotatedPiece = new char[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                rotatedPiece[j][rows - 1 - i] = piece[i][j];
            }
        }

        return rotatedPiece;
    }

    private static char[][] flipPiece(char[][] piece) {
        int rows = piece.length;
        int cols = piece[0].length;

        char[][] flippedPiece = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                flippedPiece[i][j] = piece[i][cols - 1 - j];
            }
        }

        return flippedPiece;
    }

    private static List<Integer> removePieceNumber(List<Integer> availablePieceNumbers, int pieceNumber) {
        List<Integer> updatedList = new ArrayList<>(availablePieceNumbers);
        updatedList.remove(Integer.valueOf(pieceNumber));
        return updatedList;
    }

    private static int[][] copyBoard(int[][] original) {
        int rows = original.length;
        int cols = original[0].length;
        int[][] copy = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, cols);
        }
        return copy;
    }
}
   

    