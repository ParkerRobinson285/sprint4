package game;

import model.EnglishBoard;
import model.CellState;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AutoGame — mapped to acceptance criteria.
 *
 * AC 6.1 — computer selects a random valid move and applies it
 * AC 6.3 — autoplay stops when no valid moves remain
 * AC 7.1 — after game ends, rating is a valid string matching peg count
 */
class AutoGameTest {

    private AutoGame game;

    @BeforeEach
    void setUp() {
        game = new AutoGame(new EnglishBoard());
    }

    // ── AC 6.1 — Computer applies a valid move ────────────────────────────────

    @Test
    void makeMove_returnsTrueWhenMovesAvailable() {
        assertTrue(game.makeMove(null));
    }

    @Test
    void makeMove_decrementsPegCount() {
        int before = game.getPegCount();
        game.makeMove(null);
        assertEquals(before - 1, game.getPegCount());
    }

    @Test
    void makeMove_appliesALegalMove() {
        game.makeMove(null);
        int counted = 0;
        for (int r = 0; r < game.getBoard().getSize(); r++)
            for (int c = 0; c < game.getBoard().getSize(); c++)
                if (game.getBoard().getCell(r, c) == CellState.PEG)
                    counted++;
        assertEquals(counted, game.getPegCount());
    }

    // ── AC 6.3 — Autoplay stops when no moves remain ─────────────────────────

    @Test
    void makeMove_returnsFalseWhenGameOver() {
        clearBoardExcept(3, 2);
        assertFalse(game.makeMove(null));
    }

    @Test
    void runToEnd_terminatesWithGameOver() {
        game.runToEnd();
        assertTrue(game.isOver());
    }

    // ── AC 7.1 — Rating is valid after game ends ──────────────────────────────

    @Test
    void afterRunToEnd_ratingIsValid() {
        game.runToEnd();
        String rating = game.getRating();
        assertTrue(
            rating.equals("Outstanding") ||
            rating.equals("Very Good")   ||
            rating.equals("Good")        ||
            rating.equals("Average"),
            "Unexpected rating: " + rating
        );
    }

    @Test
    void afterRunToEnd_ratingMatchesPegCount() {
        game.runToEnd();
        int pegs = game.getPegCount();
        String rating = game.getRating();
        if      (pegs == 1) assertEquals("Outstanding", rating);
        else if (pegs == 2) assertEquals("Very Good",   rating);
        else if (pegs == 3) assertEquals("Good",        rating);
        else                assertEquals("Average",      rating);
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