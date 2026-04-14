package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for EnglishBoard — mapped to acceptance criteria.
 *
 * AC 3.1 — board resets to initial state with all holes filled except center
 * AC 4.2 — valid move executes correctly (peg moves, jumped peg removed, count decrements)
 * AC 4.3 — invalid move is rejected and board state unchanged
 * AC 5.1 — game over detected when no valid moves remain
 * AC 5.2 — rating is Outstanding when one peg remains
 */
class EnglishBoardTest {

    private EnglishBoard board;

    @BeforeEach
    void setUp() {
        board = new EnglishBoard();
    }

    // ── AC 3.1 — Initial board state ──────────────────────────────────────────

    @Test
    void initialPegCount_is32() {
        assertEquals(32, board.getPegCount());
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

    // ── AC 4.2 — Valid move executes correctly ────────────────────────────────

    @Test
    void validMove_isAccepted() {
        assertTrue(board.isValidMove(3, 1, 3, 3));
    }

    @Test
    void applyMove_updatesCellStates() {
        board.applyMove(new Move(3, 1, 3, 3));
        assertEquals(CellState.EMPTY, board.getCell(3, 1)); // source emptied
        assertEquals(CellState.EMPTY, board.getCell(3, 2)); // jumped peg removed
        assertEquals(CellState.PEG,   board.getCell(3, 3)); // destination filled
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
        assertFalse(board.isValidMove(3, 3, 3, 5)); // center is empty
    }

    @Test
    void invalidMove_doesNotChangePegCount() {
        int before = board.getPegCount();
        board.isValidMove(3, 3, 3, 5);
        assertEquals(before, board.getPegCount());
    }

    // ── AC 5.1 — Game over detection ─────────────────────────────────────────

    @Test
    void isGameOver_falseAtStart() {
        assertFalse(board.isGameOver());
    }

    @Test
    void isGameOver_trueWhenNoPegsCanMove() {
        clearBoard();
        board.setCell(3, 2, CellState.PEG);
        board.recountPegs();
        assertTrue(board.isGameOver());
    }

    // ── AC 5.2 — Rating Outstanding for one peg ──────────────────────────────

    @Test
    void rating_outstanding_for1Peg() {
        clearBoard();
        board.setCell(3, 2, CellState.PEG);
        board.recountPegs();
        assertEquals("Outstanding", board.getRating());
    }

    @Test
    void rating_average_atStart() {
        assertEquals("Average", board.getRating());
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