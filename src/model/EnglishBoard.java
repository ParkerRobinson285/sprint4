package model;

/**
 * English Peg Solitaire board — cross/plus shape.
 *
 * Scales to any odd size >= 7. For a grid of size N:
 *   - Center is at (mid, mid) where mid = N/2
 *   - Corner arm width = N/3 (rounded to nearest odd, minimum 3)
 *   - The cross arms span the middle third of the grid
 *
 * Default size 7 layout:
 *   . . X X X . .
 *   . . X X X . .
 *   X X X X X X X
 *   X X X O X X X   <- center hole
 *   X X X X X X X
 *   . . X X X . .
 *   . . X X X . .
 */
public class EnglishBoard extends Board {

    public static final int DEFAULT_SIZE = 7;
    public static final int MIN_SIZE     = 7;

    private static final int[][] DIRECTIONS = {
        {-1,  0}, { 1,  0}, { 0, -1}, { 0,  1}
    };

    // The column range that defines the cross arms (top/bottom rows)
    private final int armStart;
    private final int armEnd;

    public EnglishBoard() {
        this(DEFAULT_SIZE);
    }

    public EnglishBoard(int size) {
        super(size);
        // Arm occupies the middle third of the grid
        // For size 7: armStart=2, armEnd=4 (width 3)
        // For size 9: armStart=3, armEnd=5 (width 3)
        // For size 11: armStart=3, armEnd=7 (width 5) — scales up
        int mid = size / 2;
        int armWidth = Math.max(3, size / 3);
        // Force odd arm width so it's symmetric around center
        if (armWidth % 2 == 0) armWidth--;
        armStart = mid - armWidth / 2;
        armEnd   = mid + armWidth / 2;
        initBoard();
    }

    @Override
    protected void initBoard() {
        int mid = size / 2;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (isPlayableCell(r, c)) {
                    placePeg(r, c);
                }
            }
        }
        setCell(mid, mid, CellState.EMPTY);
        recountPegs();
    }

    @Override
    protected int[][] getDirections() { return DIRECTIONS; }

    private boolean isPlayableCell(int row, int col) {
        // Middle rows — full width
        if (row >= armStart && row <= armEnd) return true;
        // Top/bottom arm rows — only center columns
        return col >= armStart && col <= armEnd;
    }
}