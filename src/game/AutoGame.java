package game;

import model.Board;
import model.Move;

import java.util.List;
import java.util.Random;

/**
 * Controls a computer-driven Solitaire game (Autoplay mode).
 *
 * The computer picks a random valid move each turn.
 * The game is never hardcoded — every run plays out differently.
 *
 * Two usage patterns are supported:
 *
 *   1. Step-by-step (for animated UI):
 *      Call makeMove(null) once per frame/tick.
 *      The UI calls this in a timer loop and repaints after each call.
 *
 *   2. Run-to-completion (for instant result):
 *      Call runToEnd(), which applies all moves immediately.
 */
public class AutoGame extends Game {

    private static final Random RNG = new Random();

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public AutoGame(Board board) {
        super(board);
    }

    // -------------------------------------------------------------------------
    // Game.makeMove implementation
    // -------------------------------------------------------------------------

    /**
     * Picks a random valid move and applies it.
     *
     * The move parameter is ignored — AutoGame always chooses its own move.
     * Pass null when calling from the UI.
     *
     * @return true if a move was made; false if the game is already over.
     */
    @Override
    public boolean makeMove(Move move) {
        if (board.isGameOver()) return false;

        List<Move> valid = board.getValidMoves();
        if (valid.isEmpty()) return false;

        Move chosen = valid.get(RNG.nextInt(valid.size()));
        board.applyMove(chosen);
        moveHistory.add(chosen);
        return true;
    }

    /**
     * Runs the game to completion by applying random valid moves until
     * no moves remain. Useful for testing or instant-result mode.
     */
    public void runToEnd() {
        while (!board.isGameOver()) {
            makeMove(null);
        }
    }

    // -------------------------------------------------------------------------
    // Game identity
    // -------------------------------------------------------------------------

    @Override
    public String getModeName() { return "Autoplay"; }
}