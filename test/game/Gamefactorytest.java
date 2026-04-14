package game;

import model.DiamondBoard;
import model.EnglishBoard;
import model.HexagonBoard;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GameFactory — mapped to acceptance criteria.
 *
 * AC 1.2 — selecting a board type creates a game with that board type
 * AC 1.4 — custom size board has correct dimensions and center hole empty
 * AC 2.1 — selecting Autoplay creates an AutoGame
 * AC 3.2 — new game has the correct initial peg count
 * AC 5.3 — after game over, a new game via factory produces a fully reset board
 * AC 7.1 — after auto game ends, rating is a valid string matching peg count
 */
class GameFactoryTest {

    // ── AC 1.2 — Board type selection creates the correct board ───────────────

    @Test
    void create_english_returnsEnglishBoard() {
        Game g = GameFactory.create(GameFactory.ENGLISH, GameFactory.MANUAL);
        assertInstanceOf(ManualGame.class, g);
        assertInstanceOf(EnglishBoard.class, g.getBoard());
    }

    @Test
    void create_hexagon_returnsHexagonBoard() {
        Game g = GameFactory.create(GameFactory.HEXAGON, GameFactory.MANUAL);
        assertInstanceOf(ManualGame.class, g);
        assertInstanceOf(HexagonBoard.class, g.getBoard());
    }

    @Test
    void create_diamond_returnsDiamondBoard() {
        Game g = GameFactory.create(GameFactory.DIAMOND, GameFactory.MANUAL);
        assertInstanceOf(ManualGame.class, g);
        assertInstanceOf(DiamondBoard.class, g.getBoard());
    }

    // ── AC 1.4 — Custom size board has correct dimensions and center empty ────

    @Test
    void customSize_englishBoard_hasCorrectSizeAndCenterEmpty() {
        Game g = GameFactory.create(GameFactory.ENGLISH, GameFactory.MANUAL, 9);
        assertEquals(9, g.getBoard().getSize());
        assertEquals(model.CellState.EMPTY, g.getBoard().getCell(4, 4));
    }

    @Test
    void customSize_hexagonBoard_hasCorrectSizeAndCenterEmpty() {
        Game g = GameFactory.create(GameFactory.HEXAGON, GameFactory.MANUAL, 9);
        assertEquals(9, g.getBoard().getSize());
        assertEquals(model.CellState.EMPTY, g.getBoard().getCell(4, 4));
    }

    @Test
    void customSize_diamondBoard_hasCorrectSizeAndCenterEmpty() {
        Game g = GameFactory.create(GameFactory.DIAMOND, GameFactory.MANUAL, 7);
        assertEquals(7, g.getBoard().getSize());
        assertEquals(model.CellState.EMPTY, g.getBoard().getCell(3, 3));
    }

    // ── AC 2.1 — Autoplay mode creates an AutoGame ───────────────────────────

    @Test
    void create_autoplay_returnsAutoGame() {
        Game g = GameFactory.create(GameFactory.ENGLISH, GameFactory.AUTOPLAY);
        assertInstanceOf(AutoGame.class, g);
    }

    // ── AC 3.2 — New game has correct initial peg count ───────────────────────

    @Test
    void newGame_english_hasCorrectPegCount() {
        Game g = GameFactory.create(GameFactory.ENGLISH, GameFactory.MANUAL);
        assertEquals(32, g.getPegCount());
        assertFalse(g.isOver());
    }

    @Test
    void newGame_hexagon_hasCorrectPegCount() {
        Game g = GameFactory.create(GameFactory.HEXAGON, GameFactory.MANUAL);
        assertEquals(36, g.getPegCount());
    }

    @Test
    void newGame_diamond_hasCorrectPegCount() {
        Game g = GameFactory.create(GameFactory.DIAMOND, GameFactory.MANUAL);
        assertEquals(40, g.getPegCount());
    }

    // ── AC 5.3 — New game after game over is fully reset ─────────────────────

    @Test
    void newGame_afterGameOver_boardIsFullyReset() {
        Game first = GameFactory.create(GameFactory.ENGLISH, GameFactory.MANUAL);
        for (int r = 0; r < first.getBoard().getSize(); r++)
            for (int c = 0; c < first.getBoard().getSize(); c++)
                if (first.getBoard().getCell(r, c) == model.CellState.PEG
                        && !(r == 3 && c == 2))
                    first.getBoard().setCell(r, c, model.CellState.EMPTY);
        first.getBoard().recountPegs();
        assertTrue(first.isOver());

        Game fresh = GameFactory.create(GameFactory.ENGLISH, GameFactory.MANUAL);
        assertFalse(fresh.isOver());
        assertEquals(32, fresh.getPegCount());
        assertEquals(model.CellState.EMPTY, fresh.getBoard().getCell(3, 3));
    }

    // ── AC 7.1 — Rating after auto game ends is valid ────────────────────────

    @Test
    void autoGame_afterRunToEnd_ratingIsValid() {
        AutoGame g = (AutoGame) GameFactory.create(GameFactory.ENGLISH, GameFactory.AUTOPLAY);
        g.runToEnd();
        String rating = g.getRating();
        assertTrue(
            rating.equals("Outstanding") ||
            rating.equals("Very Good")   ||
            rating.equals("Good")        ||
            rating.equals("Average"),
            "Unexpected rating: " + rating
        );
    }

    @Test
    void autoGame_afterRunToEnd_pegCountMatchesRating() {
        AutoGame g = (AutoGame) GameFactory.create(GameFactory.ENGLISH, GameFactory.AUTOPLAY);
        g.runToEnd();
        int pegs = g.getPegCount();
        String rating = g.getRating();
        if      (pegs == 1) assertEquals("Outstanding", rating);
        else if (pegs == 2) assertEquals("Very Good",   rating);
        else if (pegs == 3) assertEquals("Good",        rating);
        else                assertEquals("Average",      rating);
    }
}