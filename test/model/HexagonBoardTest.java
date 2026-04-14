package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for HexagonBoard — mapped to acceptance criteria.
 *
 * AC 3.1 — board resets to initial state with all holes filled except center
 * AC 4.2 — valid move executes correctly
 * AC 4.3 — invalid move is rejected
 * AC 5.1 — game over detected when no valid moves remain
 */
class HexagonBoardTest {

    private HexagonBoard board;

    @BeforeEach
    void setUp() {
        board = new HexagonBoard();
    }

    // ── AC 3.1 — Initial board state ──────────────────────────────────────────

    @Test
    void initialPegCount_is36() {
        assertEquals(36, board.getPegCount());
    }

    @Test
    void centerCell_isEmptyAtStart() {
        assertEquals(CellState.EMPTY, board.getCell(3, 3));
    }

    @Test
    void playableCells_arePegAtStart() {
        assertEquals(CellState.PEG, board.getCell(0, 2));
        assertEquals(CellState.PEG, board.getCell(3, 0));
        assertEquals(CellState.PEG, board.getCell(6, 4));
    }

    @Test
    void invalidCells_areMarkedCorrectly() {
        assertEquals(CellState.INVALID, board.getCell(0, 0));
        assertEquals(CellState.INVALID, board.getCell(0, 5));
        assertEquals(CellState.INVALID, board.getCell(6, 0));
        assertEquals(CellState.INVALID, board.getCell(6, 6));
    }

    // ── AC 4.2 — Valid move executes correctly ────────────────────────────────

    @Test
    void validMove_isAccepted() {
        assertTrue(board.isValidMove(3, 1, 3, 3));
    }

    @Test
    void applyMove_updatesCellStates() {
        board.applyMove(new Move(3, 1, 3, 3));
        assertEquals(CellState.EMPTY, board.getCell(3, 1));
        assertEquals(CellState.EMPTY, board.getCell(3, 2));
        assertEquals(CellState.PEG,   board.getCell(3, 3));
    }

    @Test
    void applyMove_decrementsPegCount() {
        int before = board.getPegCount();
        board.applyMove(new Move(3, 1, 3, 3));
        assertEquals(before - 1, board.getPegCount());
    }

    // ── AC 4.3 — Invalid move is rejected ────────────────────────────────────

    @Test
    void invalidMove_sourceIsEmpty_isRejected() {
        assertFalse(board.isValidMove(3, 3, 3, 5));
    }

    // ── AC 5.1 — Game over detection ─────────────────────────────────────────

    @Test
    void isGameOver_falseAtStart() {
        assertFalse(board.isGameOver());
    }

    @Test
    void isGameOver_trueWithOnePeg() {
        clearBoard();
        board.setCell(3, 2, CellState.PEG);
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