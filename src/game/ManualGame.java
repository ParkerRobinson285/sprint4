package game;
 
import model.Board;
import model.Move;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
 
/**
 * Controls a human-driven Peg Solitaire game.
 *
 * Tracks two separate move lists:
 *   moveHistory       — player moves only (satisfies AC 8.3)
 *   randomizeHistory  — moves applied during randomize() calls
 *
 * Both lists are needed together to produce a complete replay. The file format
 * uses P/R prefixes to distinguish them, so the replayer can reproduce the
 * exact board state without conflating player moves with randomize moves.
 */
public class ManualGame extends Game {
 
    private static final Random RNG = new Random();
 
    /**
     * Moves applied by randomize() — kept separate from moveHistory so that
     * AC 8.3 is satisfied (randomize moves are not counted as player moves),
     * while still being available to GameRecorder for complete replay.
     *
     * Each entry is a pair: {moveIndex, Move} where moveIndex is the position
     * in the overall move sequence at which this randomize block was applied.
     * GameRecorder uses getFullMoveSequence() instead, which interleaves both lists.
     */
    private final List<TaggedMove> fullSequence = new ArrayList<>();
 
    public ManualGame(Board board) {
        super(board);
    }
 
    // ── TaggedMove inner class ────────────────────────────────────────────────
 
    /**
     * A move tagged with whether it came from a player click or a randomize call.
     * Used to build the complete replay sequence while keeping the two histories separate.
     */
    public static class TaggedMove {
        public final Move move;
        public final boolean isRandomize;
 
        public TaggedMove(Move move, boolean isRandomize) {
            this.move = move;
            this.isRandomize = isRandomize;
        }
    }
 
    // ── Game.makeMove implementation ──────────────────────────────────────────
 
    /**
     * Attempts to apply the player's chosen move.
     * Records it in both moveHistory (player moves only) and fullSequence (all moves).
     *
     * @param move the move selected by the player
     * @return true if applied; false if invalid
     */
    @Override
    public boolean makeMove(Move move) {
        if (move == null || !board.isValidMove(move)) return false;
        board.applyMove(move);
        moveHistory.add(move);                          // player moves only (AC 8.3)
        fullSequence.add(new TaggedMove(move, false));  // full sequence for replay
        return true;
    }
 
    // ── Randomize ─────────────────────────────────────────────────────────────
 
    /**
     * Randomizes the board by applying a series of random valid moves.
     *
     * Randomize moves are NOT added to moveHistory (AC 8.3), but ARE added to
     * fullSequence tagged as randomize moves so that GameRecorder can write a
     * complete file that reproduces the exact board state during replay.
     *
     * @param minMoves minimum number of random moves to apply
     * @param maxMoves maximum number of random moves to apply
     */
    public void randomize(int minMoves, int maxMoves) {
        if (minMoves < 0) minMoves = 0;
        if (maxMoves < minMoves) maxMoves = minMoves;
 
        int steps = minMoves + RNG.nextInt(maxMoves - minMoves + 1);
 
        for (int i = 0; i < steps; i++) {
            List<Move> valid = board.getValidMoves();
            if (valid.isEmpty()) break;
            Move chosen = valid.get(RNG.nextInt(valid.size()));
            board.applyMove(chosen);
            // Not added to moveHistory — satisfies AC 8.3
            fullSequence.add(new TaggedMove(chosen, true)); // but included for replay
        }
    }
 
    /** Convenience overload: applies 5 to 15 random moves. */
    public void randomize() {
        randomize(5, 15);
    }
 
    /**
     * Returns the complete interleaved sequence of player and randomize moves
     * in the order they were applied. Used by GameRecorder to write a file
     * that can be fully replayed.
     */
    public List<TaggedMove> getFullSequence() {
        return Collections.unmodifiableList(fullSequence);
    }
 
    @Override
    public String getModeName() { return "Manual"; }
}