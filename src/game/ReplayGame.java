package game;
 
import model.Board;
import model.Move;
import record.GameLoader.TaggedMove;
 
import java.util.List;
 
/**
 * A Game subclass that replays a previously recorded game move by move.
 *
 * Works through a fixed list of TaggedMoves (player + randomize) in order.
 * The UI drives replay using the same Timer mechanism as Autoplay — it calls
 * makeMove(null) once per tick and repaints the board.
 *
 * All moves in the sequence are applied regardless of their tag — the P/R
 * distinction is preserved in the file but both types are needed to correctly
 * reconstruct the board state during replay.
 *
 * Class hierarchy:
 *   Game  (abstract)
 *   ├── ManualGame
 *   ├── AutoGame
 *   └── ReplayGame   ← playback-driven from a saved file
 */
public class ReplayGame extends Game {
 
    // The complete ordered move sequence from the saved file (player + randomize)
    private final List<TaggedMove> recordedMoves;
 
    // Index of the next move to apply
    private int nextMoveIndex;
 
    /**
     * Creates a ReplayGame on a fresh board with the recorded move sequence.
     *
     * @param board         freshly initialised board (same type/size as original)
     * @param recordedMoves the complete tagged move sequence from GameLoader
     */
    public ReplayGame(Board board, List<TaggedMove> recordedMoves) {
        super(board);
        this.recordedMoves = recordedMoves;
        this.nextMoveIndex = 0;
    }
 
    // ── Game.makeMove implementation ──────────────────────────────────────────
 
    /**
     * Applies the next move in the recording to the board.
     *
     * Both player moves (P) and randomize moves (R) are applied — all are needed
     * to reproduce the exact board state from the original game. The move parameter
     * is ignored; pass null from the UI timer.
     *
     * @param move ignored — pass null
     * @return true if a move was applied; false if replay is complete
     */
    @Override
    public boolean makeMove(Move move) {
        if (nextMoveIndex >= recordedMoves.size()) return false;
 
        Move next = recordedMoves.get(nextMoveIndex).move;
 
        if (!board.isValidMove(next)) return false; // guard against corrupted file
 
        board.applyMove(next);
        moveHistory.add(next);
        nextMoveIndex++;
        return true;
    }
 
    /**
     * Returns true when all recorded moves have been replayed or the board
     * has no valid moves remaining.
     */
    @Override
    public boolean isOver() {
        return nextMoveIndex >= recordedMoves.size() || board.isGameOver();
    }
 
    /** Returns how many moves remain to be replayed. */
    public int getMovesRemaining() {
        return recordedMoves.size() - nextMoveIndex;
    }
 
    /** Returns the total number of moves in the recording. */
    public int getTotalMoves() {
        return recordedMoves.size();
    }
 
    @Override
    public String getModeName() { return "Replay"; }
}