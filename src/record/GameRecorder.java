package record;
 
import game.AutoGame;
import game.Game;
import game.ManualGame;
import game.ManualGame.TaggedMove;
import model.Move;
 
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
 
/**
 * Writes a completed game to a plain-text file so it can be replayed later.
 *
 * File format:
 *
 *   BOARD_TYPE=English
 *   BOARD_SIZE=7
 *   GAME_MODE=Manual
 *   P 3,1,3,3        <- P = player move
 *   R 1,3,3,3        <- R = randomize move (only in Manual games)
 *   P 5,3,3,3
 *   ...
 *
 * For AutoGame, every line is prefixed with P (all moves are computer moves,
 * no distinction needed — the R prefix is only meaningful for ManualGame).
 *
 * The P/R prefix lets the replayer distinguish player moves from randomize moves
 * so it can show them differently in the UI and satisfy AC 8.3 (randomize moves
 * are not counted as player moves in the history).
 */
public class GameRecorder {
 
    private GameRecorder() {}
 
    /**
     * Saves the game's complete move sequence to the specified file.
     *
     * For ManualGame, writes the full interleaved sequence of player (P) and
     * randomize (R) moves from getFullSequence().
     * For AutoGame, writes all moves from getMoveHistory() prefixed with P.
     *
     * @param game      the game to save
     * @param boardType e.g. "English", "Hexagon", "Diamond"
     * @param boardSize the board size used
     * @param gameMode  e.g. "Manual", "Autoplay"
     * @param file      destination file
     * @throws IOException if the file cannot be written
     */
    public static void save(Game game, String boardType, int boardSize,
                            String gameMode, File file) throws IOException {
 
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Header
            writer.write("BOARD_TYPE=" + boardType); writer.newLine();
            writer.write("BOARD_SIZE=" + boardSize); writer.newLine();
            writer.write("GAME_MODE="  + gameMode);  writer.newLine();
 
            if (game instanceof ManualGame) {
                // Write the full sequence including randomize moves
                for (TaggedMove tm : ((ManualGame) game).getFullSequence()) {
                    String prefix = tm.isRandomize ? "R" : "P";
                    writer.write(prefix + " " + encodedMove(tm.move));
                    writer.newLine();
                }
            } else {
                // AutoGame — all moves are computer moves, prefix all with P
                for (Move move : game.getMoveHistory()) {
                    writer.write("P " + encodedMove(move));
                    writer.newLine();
                }
            }
        }
    }
 
    /** Encodes a move as "fromRow,fromCol,toRow,toCol". */
    private static String encodedMove(Move move) {
        return move.getFromRow() + "," + move.getFromCol() + ","
             + move.getToRow()   + "," + move.getToCol();
    }
}