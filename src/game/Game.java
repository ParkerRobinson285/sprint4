package game;

import model.Board;
import model.Move;

import java.util.List;

/**
 * Abstract base class for all Solitaire game modes.
 *
 * Responsibilities:
 *   - Owns a Board instance for the current game
 *   - Tracks move history (for Sprint 5 replay)
 *   - Provides shared state queries (isOver, pegCount, rating)
 *   - Declares makeMove() as abstract so Manual and Auto behave differently
 *
 * The class hierarchy required by Sprint 3:
 *
 *   Game  (abstract)
 *   ├── ManualGame   — player chooses every move
 *   └── AutoGame     — computer picks random valid moves
 */
public abstract class Game {

    protected Board board;
    protected final List<Move> moveHistory;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    protected Game(Board board) {
        this.board = board;
        this.moveHistory = new java.util.ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Abstract interface — subclasses define how a move is made
    // -------------------------------------------------------------------------

    /**
     * Execute one move in this game mode.
     * ManualGame validates and applies a player-supplied Move.
     * AutoGame picks a random valid Move and applies it.
     *
     * @param move The move to attempt (ignored by AutoGame).
     * @return true if the move was successfully applied, false otherwise.
     */
    public abstract boolean makeMove(Move move);

    // -------------------------------------------------------------------------
    // Shared state queries
    // -------------------------------------------------------------------------

    public Board getBoard()           { return board; }
    public boolean isOver()           { return board.isGameOver(); }
    public int getPegCount()          { return board.getPegCount(); }
    public String getRating()         { return board.getRating(); }
    public List<Move> getMoveHistory(){ return java.util.Collections.unmodifiableList(moveHistory); }
    public List<Move> getValidMoves() { return board.getValidMoves(); }

    /**
     * Returns the game mode name for display in the UI.
     */
    public abstract String getModeName();
}