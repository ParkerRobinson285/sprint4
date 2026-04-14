package model;

/**
 * Hexagon Peg Solitaire board.
 *
 * Scales to any odd size >= 7.
 * Shape: a hexagon with a wide band of full-width rows in the center,
 * tapering by 2 cells per row at top and bottom.
 *
 * For size N, center = mid = N/2:
 *   - The middle 3 rows are full width (cols 0 to N-1)
 *   - Each row above/below the middle band loses 1 col on each side
 *
 * Default size 7:
 *   Row 0: cols 2-4  (3 cells)
 *   Row 1: cols 1-5  (5 cells)
 *   Row 2: cols 0-6  (7 cells)
 *   Row 3: cols 0-6  (7 cells) <- center
 *   Row 4: cols 0-6  (7 cells)
 *   Row 5: cols 1-5  (5 cells)
 *   Row 6: cols 2-4  (3 cells)
 */
public class HexagonBoard extends Board {

    public static final int DEFAULT_SIZE = 7;
    public static final int MIN_SIZE     = 7;

    private static final int[][] DIRECTIONS = {
        {-1,  0}, { 1,  0}, { 0, -1}, { 0,  1}
    };

    public HexagonBoard() {
        this(DEFAULT_SIZE);
    }

    public HexagonBoard(int size) {
        super(size);
        initBoard();
    }

    @Override
    protected void initBoard() {
        int mid = size / 2;
        for (int r = 0; r < size; r++) {
            int colStart = getColStart(r);
            int colEnd   = getColEnd(r);
            for (int c = colStart; c <= colEnd; c++) {
                placePeg(r, c);
            }
        }
        setCell(mid, mid, CellState.EMPTY);
        recountPegs();
    }

    @Override
    protected int[][] getDirections() { return DIRECTIONS; }

    /**
     * Distance from the center row determines the indent.
     * The middle third of rows are full width.
     * Beyond that, each row loses 1 col per step on each side.
     */
    private int getColStart(int row) {
        return Math.max(0, distanceFromMiddleBand(row));
    }

    private int getColEnd(int row) {
        return Math.min(size - 1, size - 1 - distanceFromMiddleBand(row));
    }

    /**
     * Returns how many rows outside the middle full-width band this row is.
     * Returns 0 or negative for rows inside the band (no indent needed).
     * The middle band is the center row ± 1 (3 full-width rows).
     */
    private int distanceFromMiddleBand(int row) {
        int mid = size / 2;
        int distFromCenter = Math.abs(row - mid);
        // Middle band = center row ± 1, so band radius = 1
        return distFromCenter - 1;
    }

    public int getRowColStart(int row) { return getColStart(row); }
    public int getRowColEnd(int row)   { return getColEnd(row); }
}