package game;

import model.CellState;
import model.EnglishBoard;
import model.Move;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ManualGame — mapped to acceptance criteria.
 *
 * AC 4.2 — valid move executes (peg moves, jumped peg removed, count decrements)
 * AC 4.3 — invalid move is rejected, board state unchanged
 * AC 5.1 — game over detected when no valid moves remain
 * AC 5.2 — rating is Outstanding when one peg remains
 * AC 8.1 — randomize changes board to new valid mid-game position
 * AC 8.2 — board is consistent after randomize
 * AC 8.3 — randomize does not affect move history
 */
class ManualGameTest {

    private ManualGame game;

    @BeforeEach
    void setUp() {
        game = new ManualGame(new EnglishBoard());
    }

    // ── AC 4.2 — Valid move executes correctly ────────────────────────────────

    @Test
    void makeMove_validMove_returnsTrue() {
        assertTrue(game.makeMove(new Move(3, 1, 3, 3)));
    }

    @Test
    void makeMove_validMove_updatesBoardState() {
        game.makeMove(new Move(3, 1, 3, 3));
        assertEquals(CellState.EMPTY, game.getBoard().getCell(3, 1));
        assertEquals(CellState.EMPTY, game.getBoard().getCell(3, 2));
        assertEquals(CellState.PEG,   game.getBoard().getCell(3, 3));
    }

    @Test
    void makeMove_validMove_decrementsPegCount() {
        int before = game.getPegCount();
        game.makeMove(new Move(3, 1, 3, 3));
        assertEquals(before - 1, game.getPegCount());
    }

    // ── AC 4.3 — Invalid move is rejected ────────────────────────────────────

    @Test
    void makeMove_invalidMove_returnsFalse() {
        assertFalse(game.makeMove(new Move(3, 3, 3, 5))); // source is empty
    }

    @Test
    void makeMove_invalidMove_doesNotChangePegCount() {
        int before = game.getPegCount();
        game.makeMove(new Move(3, 3, 3, 5));
        assertEquals(before, game.getPegCount());
    }

    @Test
    void makeMove_nullMove_returnsFalse() {
        assertFalse(game.makeMove(null));
    }

    // ── AC 5.1 — Game over detection ─────────────────────────────────────────

    @Test
    void isOver_falseAtStart() {
        assertFalse(game.isOver());
    }

    @Test
    void isOver_trueWhenNoMovesRemain() {
        clearBoardExcept(3, 2);
        assertTrue(game.isOver());
    }

    // ── AC 5.2 — Outstanding rating for one peg ──────────────────────────────

    @Test
    void getRating_outstanding_whenOnePegLeft() {
        clearBoardExcept(3, 2);
        assertEquals("Outstanding", game.getRating());
    }

    @Test
    void getRating_average_atStart() {
        assertEquals("Average", game.getRating());
    }

    // ── AC 8.1 — Randomize changes board to new valid mid-game position ───────

    @Test
    void randomize_reducesPegCount() {
        int before = game.getPegCount();
        game.randomize(3, 3);
        assertEquals(before - 3, game.getPegCount());
    }

    // ── AC 8.2 — Board is consistent after randomize ──────────────────────────

    @Test
    void randomize_producesValidBoardState() {
        game.randomize();
        int counted = 0;
        for (int r = 0; r < game.getBoard().getSize(); r++)
            for (int c = 0; c < game.getBoard().getSize(); c++)
                if (game.getBoard().getCell(r, c) == CellState.PEG)
                    counted++;
        assertEquals(counted, game.getPegCount());
    }

    // ── AC 8.3 — Randomize does not affect move history ───────────────────────

    @Test
    void randomize_doesNotAddToMoveHistory() {
        game.randomize(5, 5);
        assertTrue(game.getMoveHistory().isEmpty());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void clearBoardExcept(int keepRow, int keepCol) {
        for (int r = 0; r < game.getBoard().getSize(); r++)
            for (int c = 0; c < game.getBoard().getSize(); c++)
                if (game.getBoard().getCell(r, c) == CellState.PEG
                        && !(r == keepRow && c == keepCol))
                    game.getBoard().setCell(r, c, CellState.EMPTY);
        game.getBoard().recountPegs();
    }
}