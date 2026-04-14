package record;
 
import model.Move;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
 
/**
 * Reads a saved game file and returns the information needed to replay it.
 *
 * Supports two file formats:
 *
 *   New format (with P/R prefix):
 *     BOARD_TYPE=Hexagon
 *     BOARD_SIZE=7
 *     GAME_MODE=Manual
 *     P 3,1,3,3
 *     R 5,3,3,3
 *     P 0,2,2,2
 *
 *   Legacy format (no prefix — all moves treated as player moves):
 *     BOARD_TYPE=Hexagon
 *     BOARD_SIZE=7
 *     GAME_MODE=Manual
 *     3,2,1,2
 *     5,3,3,3
 *
 * The P/R prefix allows the replayer to correctly reproduce a board state
 * that included randomize calls during a manual game.
 */
public class GameLoader {
 
    private GameLoader() {}
 
    /**
     * Parses the given file and returns a RecordedGame.
     *
     * @param file the file to read
     * @return a RecordedGame with all data needed to replay
     * @throws IOException              if the file cannot be read
     * @throws IllegalArgumentException if the file format is invalid
     */
    public static RecordedGame load(File file) throws IOException {
        String boardType = null;
        int    boardSize = -1;
        String gameMode  = null;
        List<TaggedMove> moves = new ArrayList<>();
 
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
 
                if (line.startsWith("BOARD_TYPE=")) {
                    boardType = line.substring("BOARD_TYPE=".length());
                } else if (line.startsWith("BOARD_SIZE=")) {
                    boardSize = Integer.parseInt(line.substring("BOARD_SIZE=".length()));
                } else if (line.startsWith("GAME_MODE=")) {
                    gameMode = line.substring("GAME_MODE=".length());
                } else {
                    // Move line — detect whether it has a P/R prefix
                    boolean isRandomize = false;
                    String movePart = line;
 
                    if (line.startsWith("P ") || line.startsWith("R ")) {
                        isRandomize = line.startsWith("R ");
                        movePart = line.substring(2); // strip the prefix
                    }
                    // else: legacy format with no prefix — treat as player move
 
                    String[] parts = movePart.split(",");
                    if (parts.length != 4)
                        throw new IllegalArgumentException("Invalid move line: " + line);
 
                    int fromRow = Integer.parseInt(parts[0].trim());
                    int fromCol = Integer.parseInt(parts[1].trim());
                    int toRow   = Integer.parseInt(parts[2].trim());
                    int toCol   = Integer.parseInt(parts[3].trim());
                    moves.add(new TaggedMove(new Move(fromRow, fromCol, toRow, toCol), isRandomize));
                }
            }
        }
 
        if (boardType == null || boardSize == -1 || gameMode == null)
            throw new IllegalArgumentException(
                "File is missing required header fields (BOARD_TYPE, BOARD_SIZE, GAME_MODE).");
 
        return new RecordedGame(boardType, boardSize, gameMode, moves);
    }
 
    // ── Inner types ───────────────────────────────────────────────────────────
 
    /** A move tagged with whether it came from a player click or a randomize call. */
    public static class TaggedMove {
        public final Move move;
        public final boolean isRandomize;
 
        public TaggedMove(Move move, boolean isRandomize) {
            this.move = move;
            this.isRandomize = isRandomize;
        }
    }
 
    /** Everything parsed from a saved game file. */
    public static class RecordedGame {
        public final String boardType;
        public final int    boardSize;
        public final String gameMode;
        public final List<TaggedMove> moves; // full sequence including randomize moves
 
        public RecordedGame(String boardType, int boardSize,
                            String gameMode, List<TaggedMove> moves) {
            this.boardType = boardType;
            this.boardSize = boardSize;
            this.gameMode  = gameMode;
            this.moves     = moves;
        }
    }
}