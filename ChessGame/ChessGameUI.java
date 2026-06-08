import java.awt.*;
import javax.swing.*;

public class ChessGameUI extends JFrame {
    private static final int BOARD_SIZE = 8;

    // Your UI controls
    private JPanel boardPanel;
    private JButton[][] squares;
    private ChessGameEngine gameEngine;

    // Selection state (kept as in your code)
    private int selectedRow = -1;
    private int selectedCol = -1;

    // Image helper
    private final PieceImageLoader pieceLoader = new PieceImageLoader();

    public ChessGameUI() {
        gameEngine = new ChessGameEngine();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the board panel (square, grid-like)
        boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        squares = new JButton[BOARD_SIZE][BOARD_SIZE];

        // Initialize the squares
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                JButton btn = new JButton();
                squares[row][col] = btn;

                btn.setFocusPainted(false);
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setText(""); // we will use icons instead of text
                btn.setHorizontalAlignment(SwingConstants.CENTER);
                btn.setVerticalAlignment(SwingConstants.CENTER);
                btn.setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);

                final int finalRow = row;
                final int finalCol = col;
                btn.addActionListener(e -> handleSquareClick(finalRow, finalCol));

                boardPanel.add(btn);
            }
        }

        // Resize-safe icon scaling: refresh icons when the board panel changes size.
        boardPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int squareSize = Math.max(1, Math.min(boardPanel.getWidth(), boardPanel.getHeight()) / BOARD_SIZE);
                updateBoardDisplay(squareSize);
            }
        });

        add(boardPanel, BorderLayout.CENTER);

        // Add status panel
        JPanel statusPanel = new JPanel();
        JLabel statusLabel = new JLabel("White's turn");
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);

        // Update the board display (initial sizing)
        // If pack() hasn't established sizes yet, use a sensible fallback.
        // Initial paint using a fallback square size; will be corrected on resize
        updateBoardDisplay(80);


        pack();
        setLocationRelativeTo(null);
    }

    
    private void handleSquareClick(int row, int col) {
        if (selectedRow == -1) {
            // First click - select piece
            char piece = gameEngine.getBoard()[row][col];
            if (piece != ' ' && 
                (Character.isUpperCase(piece) == gameEngine.isWhiteTurn())) {
                selectedRow = row;
                selectedCol = col;
                squares[row][col].setBackground(Color.YELLOW);
            }
        } else {
            // Second click - attempt to move
            boolean moveSuccess = gameEngine.makeMove(selectedRow, selectedCol, row, col);
            
            // Reset selection
            squares[selectedRow][selectedCol].setBackground(
                (selectedRow + selectedCol) % 2 == 0 ? Color.WHITE : Color.GRAY
            );
            selectedRow = -1;
            selectedCol = -1;
            
            if (moveSuccess) {
                // Repaint pieces using the current square size
                int squareSize = Math.max(1,
                        Math.min(boardPanel.getWidth(), boardPanel.getHeight()) / BOARD_SIZE);
                updateBoardDisplay(squareSize);
            }
        }
    }

    /**
     * Updates each JButton square with the correct icon for the piece notation.
     *
     * Inserted replacement for the old text-based updateBoardDisplay().
     */
    private void updateBoardDisplay(int squareSize) {
        char[][] board = gameEngine.getBoard();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                char piece = board[row][col];
                JButton btn = squares[row][col];

                btn.setText(""); // ensure no text remains

                Icon icon = pieceLoader.getPieceIcon(piece, squareSize);
                btn.setIcon(icon);

                // Keep the icon centered regardless of layout/resize
                btn.setHorizontalAlignment(SwingConstants.CENTER);
                btn.setVerticalAlignment(SwingConstants.CENTER);
            }
        }
        revalidate();
        repaint();
    }

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChessGameUI ui = new ChessGameUI();
            ui.setVisible(true);
        });
    }
}