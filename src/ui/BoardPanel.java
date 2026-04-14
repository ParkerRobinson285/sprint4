package ui;

import game.Game;
import model.Board;
import model.CellState;
import model.Move;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Renders the Solitaire board and handles player click input.
 *
 * Click interaction model:
 *   First click  → selects a peg (highlights it and shows valid destinations)
 *   Second click → if on a valid destination, executes the move
 *                  if on another peg, re-selects that peg instead
 *                  if on an invalid cell, clears the selection
 *
 * The panel is repainted automatically after every interaction.
 * It fires a Runnable callback (onMoveApplied) after each successful
 * move so the parent window can update the status panel.
 */
public class BoardPanel extends JPanel {

    // -------------------------------------------------------------------------
    // Visual constants
    // -------------------------------------------------------------------------

    private static final int CELL_SIZE    = 52;   // pixels per grid cell
    private static final int PEG_RADIUS   = 18;   // peg circle radius
    private static final int HOLE_RADIUS  = 6;    // empty hole circle radius

    private static final Color COLOR_BG          = new Color(245, 222, 179);  // warm wood
    private static final Color COLOR_BOARD_BG    = new Color(139, 90,  43);   // dark wood
    private static final Color COLOR_PEG         = new Color(40,  40,  120);  // dark blue
    private static final Color COLOR_PEG_BORDER  = new Color(20,  20,  80);
    private static final Color COLOR_SELECTED    = new Color(255, 200, 0);    // gold highlight
    private static final Color COLOR_VALID_DEST  = new Color(80,  200, 80);   // green hint
    private static final Color COLOR_EMPTY_HOLE  = new Color(100, 60,  20);   // dark hole
    private static final Color COLOR_INVALID     = COLOR_BOARD_BG;            // blends with bg

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private Game game;

    // Selected source cell (-1 = nothing selected)
    private int selectedRow = -1;
    private int selectedCol = -1;

    // Valid destinations for the currently selected peg
    private List<Move> validMovesFromSelected = new java.util.ArrayList<>();

    // Callback fired after every successful move
    private Runnable onMoveApplied;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public BoardPanel() {
        setBackground(COLOR_BG);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Replace the active game and repaint. Called on New Game / board change. */
    public void setGame(Game game) {
        this.game = game;
        clearSelection();
        recalculatePreferredSize();
        repaint();
    }

    /** Register a callback to be invoked after each successful player move. */
    public void setOnMoveApplied(Runnable callback) {
        this.onMoveApplied = callback;
    }

    /** Repaint without changing selection (used by Autoplay timer). */
    public void refresh() {
        repaint();
    }

    // -------------------------------------------------------------------------
    // Click handling
    // -------------------------------------------------------------------------

    private void handleClick(int px, int py) {
        if (game == null || game.isOver()) return;
        // Manual game only — ignore clicks during autoplay
        if (!(game instanceof game.ManualGame)) return;

        int[] cell = pixelToCell(px, py);
        if (cell == null) return;

        int row = cell[0];
        int col = cell[1];
        Board board = game.getBoard();
        CellState state = board.getCell(row, col);

        if (state == CellState.INVALID) {
            clearSelection();
            repaint();
            return;
        }

        // If nothing selected yet — try to select this cell
        if (selectedRow == -1) {
            if (state == CellState.PEG) {
                select(row, col);
            }
            repaint();
            return;
        }

        // Something already selected — try to move to this cell
        Move attempted = new Move(selectedRow, selectedCol, row, col);
        if (isValidDestination(row, col)) {
            // Execute the move
            ((game.ManualGame) game).makeMove(attempted);
            clearSelection();
            if (onMoveApplied != null) onMoveApplied.run();
        } else if (state == CellState.PEG) {
            // Re-select the newly clicked peg
            select(row, col);
        } else {
            clearSelection();
        }
        repaint();
    }

    private void select(int row, int col) {
        selectedRow = row;
        selectedCol = col;
        // Pre-compute valid destinations for this peg
        validMovesFromSelected = game.getValidMoves().stream()
            .filter(m -> m.getFromRow() == row && m.getFromCol() == col)
            .collect(java.util.stream.Collectors.toList());
    }

    private void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
        validMovesFromSelected = new java.util.ArrayList<>();
    }

    private boolean isValidDestination(int row, int col) {
        return validMovesFromSelected.stream()
            .anyMatch(m -> m.getToRow() == row && m.getToCol() == col);
    }

    // -------------------------------------------------------------------------
    // Coordinate conversion
    // -------------------------------------------------------------------------

    /**
     * Converts a pixel coordinate to a (row, col) board cell.
     * Returns null if the click is outside the board area.
     */
    private int[] pixelToCell(int px, int py) {
        if (game == null) return null;
        int boardSize = game.getBoard().getSize();
        int offsetX   = getBoardOffsetX();
        int offsetY   = getBoardOffsetY();

        int row = (py - offsetY) / CELL_SIZE;
        int col = (px - offsetX) / CELL_SIZE;

        if (row < 0 || row >= boardSize || col < 0 || col >= boardSize) return null;
        if (game.getBoard().getCell(row, col) == CellState.INVALID) return null;
        return new int[]{row, col};
    }

    private int getBoardOffsetX() {
        int boardPixels = game.getBoard().getSize() * CELL_SIZE;
        return Math.max(10, (getWidth() - boardPixels) / 2);
    }

    private int getBoardOffsetY() {
        int boardPixels = game.getBoard().getSize() * CELL_SIZE;
        return Math.max(10, (getHeight() - boardPixels) / 2);
    }

    /**
     * Returns the pixel x position (relative to offsetX) of the left edge
     * of cell (row, col).
     *
     * For English and Diamond boards this is simply c * CELL_SIZE.
     *
     * For the Hexagon board each row is centered over the full grid width.
     * The data model stores row r's cells starting at colStart(r), but
     * visually they should start at (maxCols - rowWidth) / 2 cells from the left.
     * So the visual x of column c in row r is:
     *   visualStart(r) * CELL_SIZE + (c - colStart(r)) * CELL_SIZE
     */
    private int getCellPixelX(int row, int col) {
        return col * CELL_SIZE;
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (game == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Board board = game.getBoard();
        int boardSize = board.getSize();
        int offsetX = getBoardOffsetX();
        int offsetY = getBoardOffsetY();

        // Draw board background rectangle
        g2.setColor(COLOR_BOARD_BG);
        g2.fillRoundRect(offsetX - 8, offsetY - 8,
            boardSize * CELL_SIZE + 16, boardSize * CELL_SIZE + 16, 12, 12);

        // Draw cells
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                CellState state = board.getCell(r, c);
                if (state == CellState.INVALID) continue;

                int cx = offsetX + getCellPixelX(r, c) + CELL_SIZE / 2;
                int cy = offsetY + r * CELL_SIZE + CELL_SIZE / 2;

                boolean isSelected  = (r == selectedRow && c == selectedCol);
                boolean isValidDest = isValidDestination(r, c);

                if (state == CellState.PEG) {
                    drawPeg(g2, cx, cy, isSelected);
                } else {
                    drawHole(g2, cx, cy, isValidDest);
                }
            }
        }
    }

    private void drawPeg(Graphics2D g2, int cx, int cy, boolean selected) {
        if (selected) {
            // Gold glow ring behind the peg
            g2.setColor(COLOR_SELECTED);
            g2.fillOval(cx - PEG_RADIUS - 5, cy - PEG_RADIUS - 5,
                (PEG_RADIUS + 5) * 2, (PEG_RADIUS + 5) * 2);
        }
        // Peg body
        g2.setColor(COLOR_PEG);
        g2.fillOval(cx - PEG_RADIUS, cy - PEG_RADIUS, PEG_RADIUS * 2, PEG_RADIUS * 2);
        // Border
        g2.setColor(COLOR_PEG_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(cx - PEG_RADIUS, cy - PEG_RADIUS, PEG_RADIUS * 2, PEG_RADIUS * 2);
        // Shine dot
        g2.setColor(new Color(120, 120, 200, 180));
        g2.fillOval(cx - PEG_RADIUS / 2, cy - PEG_RADIUS / 2, PEG_RADIUS / 2, PEG_RADIUS / 2);
    }

    private void drawHole(Graphics2D g2, int cx, int cy, boolean validDest) {
        if (validDest) {
            // Green ring to hint at valid landing spot
            g2.setColor(COLOR_VALID_DEST);
            g2.fillOval(cx - HOLE_RADIUS - 6, cy - HOLE_RADIUS - 6,
                (HOLE_RADIUS + 6) * 2, (HOLE_RADIUS + 6) * 2);
        }
        g2.setColor(COLOR_EMPTY_HOLE);
        g2.fillOval(cx - HOLE_RADIUS, cy - HOLE_RADIUS, HOLE_RADIUS * 2, HOLE_RADIUS * 2);
    }

    // -------------------------------------------------------------------------
    // Layout
    // -------------------------------------------------------------------------

    private void recalculatePreferredSize() {
        if (game == null) return;
        int boardPixels = game.getBoard().getSize() * CELL_SIZE + 36;
        setPreferredSize(new Dimension(boardPixels, boardPixels));
    }
}