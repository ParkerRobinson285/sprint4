package model;

/**
 * Represents a single move in Peg Solitaire.
 * A move goes FROM a source peg, jumps OVER an adjacent peg, and lands in an EMPTY hole.
 * The jumped peg is removed from the board.
 */
public class Move {
    private final int fromRow;
    private final int fromCol;
    private final int toRow;
    private final int toCol;

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow   = toRow;
        this.toCol   = toCol;
    }

    public int getFromRow() { return fromRow; }
    public int getFromCol() { return fromCol; }
    public int getToRow()   { return toRow; }
    public int getToCol()   { return toCol; }

    /**
     * Returns the row of the peg that gets jumped over (removed).
     */
    public int getJumpedRow() {
        return (fromRow + toRow) / 2;
    }

    /**
     * Returns the column of the peg that gets jumped over (removed).
     */
    public int getJumpedCol() {
        return (fromCol + toCol) / 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move other = (Move) o;
        return fromRow == other.fromRow
            && fromCol == other.fromCol
            && toRow   == other.toRow
            && toCol   == other.toCol;
    }

    @Override
    public int hashCode() {
        int result = fromRow;
        result = 31 * result + fromCol;
        result = 31 * result + toRow;
        result = 31 * result + toCol;
        return result;
    }

    @Override
    public String toString() {
        return String.format("Move(%d,%d) -> (%d,%d) [jumps (%d,%d)]",
            fromRow, fromCol, toRow, toCol, getJumpedRow(), getJumpedCol());
    }
}