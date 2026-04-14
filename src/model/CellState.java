package model;

/**
 * Represents the state of a single cell on the Solitaire board.
 */
public enum CellState {
    PEG,     // Cell contains a peg/marble
    EMPTY,   // Cell is empty (valid position, no peg)
    INVALID  // Cell is not part of the board (outside the playable area)
}