package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DiamondBoard — mapped to acceptance criteria.
 *
 * AC 3.1 — board resets to initial state with all holes filled except center
 * AC 4.2 — valid move executes correctly
 * AC 4.3 — invalid move is rejected
 * AC 5.1 — game over detected when no valid moves remain
 */
class DiamondBoardTest {

    private DiamondBoard board;

    @BeforeEach
    void setUp() {
        board = new DiamondBoard();
    }

    // ── AC 3.1 — Initial board state ──────────────────────────────────────────

    @Test
    void initialPegCount_is40() {
        assertEquals(40, board.getPegCount());
    }

    @Test
    void centerCell_isEmptyAtStart() {
        assertEquals(CellState.EMPTY, board.getCell(4, 4));
    }

    @Test
    void tipCells_arePegAtStart() {
        assertEquals(CellState.PEG, board.getCell(0, 4)); // top tip
        assertEquals(CellState.PEG, board.getCell(8, 4)); // bottom tip
        assertEquals(CellState.PEG, board.getCell(4, 0)); // left tip
        assertEquals(CellState.PEG, board.getCell(4, 8)); // right tip
    }

    @Test
    void cornerCells_areInvalid() {
        assertEquals(CellState.INVALID, board.getCell(0, 0));
        assertEquals(CellState.INVALID, board.getCell(0, 8));
        assertEquals(CellState.INVALID, board.getCell(8, 0));
        assertEquals(CellState.INVALID, board.getCell(8, 8));
    }

    // ── AC 4.2 — Valid move executes correctly ────────────────────────────────

    @Test
    void validMove_isAccepted() {
        assertTrue(board.isValidMove(4, 2, 4, 4));
    }

    @Test
    void applyMove_updatesCellStates() {
        board.applyMove(new Move(4, 2, 4, 4));
        assertEquals(CellState.EMPTY, board.getCell(4, 2));
        assertEquals(CellState.EMPTY, board.getCell(4, 3));
        assertEquals(CellState.PEG,   board.getCell(4, 4));
    }

    @Test
    void applyMove_decrementsPegCount() {
        int before = board.getPegCount();
        board.applyMove(new Move(4, 2, 4, 4));
        assertEquals(before - 1, board.getPegCount());
    }

    // ── AC 4.3 — Invalid move is rejected ────────────────────────────────────

    @Test
    void invalidMove_sourceIsEmpty_isRejected() {
        assertFalse(board.isValidMove(4, 4, 4, 6));
    }

    // ── AC 5.1 — Game over detection ─────────────────────────────────────────

    @Test
    void isGameOver_falseAtStart() {
        assertFalse(board.isGameOver());
    }

    @Test
    void isGameOver_trueWithOnePeg() {
        clearBoard();
        board.setCell(4, 3, CellState.PEG);
        board.recountPegs();
        assertTrue(board.isGameOver());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void clearBoard() {
        for (int r = 0; r < board.getSize(); r++)
            for (int c = 0; c < board.getSize(); c++)
                if (board.getCell(r, c) == CellState.PEG)
                    board.setCell(r, c, CellState.EMPTY);
        board.recountPegs();
    }
}