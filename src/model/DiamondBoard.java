package model;

/**
 * Diamond Peg Solitaire board — rotated square shape.
 *
 * Scales to any odd size >= 5.
 * For a grid of size N, center is at (mid, mid) where mid = N/2.
 * Row widths: 1, 3, 5, ... N, ... 5, 3, 1 (diamond/rhombus).
 *
 * Default size 9:
 *   . . . . O . . . .
 *   . . . O O O . . .
 *   . . O O O O O . .
 *   . O O O O O O O .
 *   O O O O O O O O O   <- center
 *   . O O O O O O O .
 *   . . O O O O O . .
 *   . . . O O O . . .
 *   . . . . O . . . .
 */
public class DiamondBoard extends Board {

    public static final int DEFAULT_SIZE = 9;
    public static final int MIN_SIZE     = 5;

    private static final int[][] DIRECTIONS = {
        {-1,  0}, { 1,  0}, { 0, -1}, { 0,  1}
    };

    public DiamondBoard() {
        this(DEFAULT_SIZE);
    }

    public DiamondBoard(int size) {
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

    private int getColStart(int row) {
        return Math.abs(row - size / 2);
    }

    private int getColEnd(int row) {
        return size - 1 - Math.abs(row - size / 2);
    }

    public int getRowColStart(int row) { return getColStart(row); }
    public int getRowColEnd(int row)   { return getColEnd(row); }
}