package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all Solitaire board types.
 *
 * Responsibilities:
 *   - Stores the grid of CellStates
 *   - Enforces move validation and application rules common to all boards
 *   - Declares abstract methods that each board type must implement
 *
 * Subclasses must implement:
 *   - initBoard()  : fill the grid for that specific board shape
 *   - getDirections() : return the set of jump directions allowed for this board
 */
public abstract class Board {

    protected CellState[][] grid;
    protected final int size;   // grid dimension (e.g. 7 for a 7x7 English board)
    private int pegCount;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    protected Board(int size) {
        this.size = size;
        this.grid = new CellState[size][size];
        // Fill everything as INVALID first; subclass initBoard() will mark
        // the playable cells as PEG or EMPTY.
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                grid[r][c] = CellState.INVALID;
    }

    /**
     * Set up the initial board state.
     * Must be called by every subclass constructor after super(size).
     */
    protected abstract void initBoard();

    /**
     * Returns the legal jump directions for this board type.
     * Each element is a {deltaRow, deltaCol} pair representing one step
     * in that direction (the jump covers 2 steps).
     *
     * English/Diamond boards use orthogonal directions only.
     * Hexagon boards may add diagonal directions.
     */
    protected abstract int[][] getDirections();

    // -------------------------------------------------------------------------
    // Public board access
    // -------------------------------------------------------------------------

    public int getSize() { return size; }

    public CellState getCell(int row, int col) {
        if (!inBounds(row, col)) return CellState.INVALID;
        return grid[row][col];
    }

    /** Package-private setter used by Board itself and test code. */
    public void setCell(int row, int col, CellState state) {
        if (inBounds(row, col)) grid[row][col] = state;
    }

    public int getPegCount() { return pegCount; }

    // -------------------------------------------------------------------------
    // Move validation
    // -------------------------------------------------------------------------

    /**
     * Returns true if moving the peg at (fromRow, fromCol) to (toRow, toCol)
     * is a legal Solitaire move:
     *   1. Source must be a PEG
     *   2. Destination must be EMPTY (and a valid cell)
     *   3. The jump must be exactly 2 steps in one of the allowed directions
     *   4. The cell in between must be a PEG (the one being jumped over)
     */
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!inBounds(fromRow, fromCol) || !inBounds(toRow, toCol)) return false;
        if (grid[fromRow][fromCol] != CellState.PEG)   return false;
        if (grid[toRow][toCol]     != CellState.EMPTY)  return false;

        int dr = toRow - fromRow;
        int dc = toCol - fromCol;

        // Must be exactly 2 steps in one allowed direction
        for (int[] dir : getDirections()) {
            if (dr == dir[0] * 2 && dc == dir[1] * 2) {
                int midRow = fromRow + dir[0];
                int midCol = fromCol + dir[1];
                return grid[midRow][midCol] == CellState.PEG;
            }
        }
        return false;
    }

    public boolean isValidMove(Move move) {
        return isValidMove(move.getFromRow(), move.getFromCol(),
                           move.getToRow(),  move.getToCol());
    }

    // -------------------------------------------------------------------------
    // Move application
    // -------------------------------------------------------------------------

    /**
     * Applies a move that has already been validated.
     * Moves the source peg to the destination, removes the jumped peg.
     *
     * @throws IllegalArgumentException if the move is not valid.
     */
    public void applyMove(Move move) {
        if (!isValidMove(move))
            throw new IllegalArgumentException("Invalid move: " + move);

        grid[move.getFromRow()][move.getFromCol()] = CellState.EMPTY;
        grid[move.getJumpedRow()][move.getJumpedCol()] = CellState.EMPTY;
        grid[move.getToRow()][move.getToCol()] = CellState.PEG;

        pegCount--;
    }

    // -------------------------------------------------------------------------
    // Game state queries
    // -------------------------------------------------------------------------

    /**
     * Returns all currently valid moves on the board.
     */
    public List<Move> getValidMoves() {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] != CellState.PEG) continue;
                for (int[] dir : getDirections()) {
                    int tr = r + dir[0] * 2;
                    int tc = c + dir[1] * 2;
                    if (isValidMove(r, c, tr, tc))
                        moves.add(new Move(r, c, tr, tc));
                }
            }
        }
        return moves;
    }

    /**
     * Returns true when no valid moves remain.
     */
    public boolean isGameOver() {
        return getValidMoves().isEmpty();
    }

    /**
     * Returns a player rating based on how many pegs remain.
     * Outstanding : 1 peg
     * Very Good   : 2 pegs
     * Good        : 3 pegs
     * Average     : 4+ pegs
     */
    public String getRating() {
        switch (pegCount) {
            case 1:  return "Outstanding";
            case 2:  return "Very Good";
            case 3:  return "Good";
            default: return "Average";
        }
    }

    // -------------------------------------------------------------------------
    // Deep copy (used by randomize / replay features in later sprints)
    // -------------------------------------------------------------------------

    /**
     * Returns a deep copy of the grid so callers cannot mutate internal state.
     */
    public CellState[][] getGridCopy() {
        CellState[][] copy = new CellState[size][size];
        for (int r = 0; r < size; r++)
            copy[r] = grid[r].clone();
        return copy;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    protected boolean inBounds(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    /**
     * Called by subclasses during initBoard() to place a peg and track count.
     */
    protected void placePeg(int row, int col) {
        grid[row][col] = CellState.PEG;
        pegCount++;
    }

    /**
     * Called by subclasses during initBoard() to mark a cell as empty
     * (valid board position, but no peg).
     */
    protected void placeEmpty(int row, int col) {
        grid[row][col] = CellState.EMPTY;
    }

    /**
     * Used internally (and by randomize) to set peg count explicitly
     * after bulk grid modifications.
     */
    public void recountPegs() {
        pegCount = 0;
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (grid[r][c] == CellState.PEG)
                    pegCount++;
    }
}