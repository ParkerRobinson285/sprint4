package game;

import model.Board;
import model.DiamondBoard;
import model.EnglishBoard;
import model.HexagonBoard;

/**
 * Creates a Game instance from the player's UI selections.
 */
public class GameFactory {

    public static final String ENGLISH  = "English";
    public static final String HEXAGON  = "Hexagon";
    public static final String DIAMOND  = "Diamond";
    public static final String MANUAL   = "Manual";
    public static final String AUTOPLAY = "Autoplay";

    // Default and min sizes per board type
    public static final int DEFAULT_SIZE_ENGLISH = 7;
    public static final int DEFAULT_SIZE_HEXAGON = 7;
    public static final int DEFAULT_SIZE_DIAMOND = 9;
    public static final int MIN_SIZE_ENGLISH     = EnglishBoard.MIN_SIZE;
    public static final int MIN_SIZE_HEXAGON     = HexagonBoard.MIN_SIZE;
    public static final int MIN_SIZE_DIAMOND     = DiamondBoard.MIN_SIZE;

    private GameFactory() {}

    public static Game create(String boardType, String gameMode, int size) {
        Board board = createBoard(boardType, size);
        return createGame(board, gameMode);
    }

    /** Convenience overload using default size. */
    public static Game create(String boardType, String gameMode) {
        return create(boardType, gameMode, getDefaultSize(boardType));
    }

    public static Board createBoard(String boardType, int size) {
        switch (boardType) {
            case ENGLISH: return new EnglishBoard(size);
            case HEXAGON: return new HexagonBoard(size);
            case DIAMOND: return new DiamondBoard(size);
            default: throw new IllegalArgumentException("Unknown board type: " + boardType);
        }
    }

    public static Game createGame(Board board, String gameMode) {
        switch (gameMode) {
            case MANUAL:   return new ManualGame(board);
            case AUTOPLAY: return new AutoGame(board);
            default: throw new IllegalArgumentException("Unknown game mode: " + gameMode);
        }
    }

    public static int getDefaultSize(String boardType) {
        switch (boardType) {
            case HEXAGON: return DEFAULT_SIZE_HEXAGON;
            case DIAMOND: return DEFAULT_SIZE_DIAMOND;
            default:      return DEFAULT_SIZE_ENGLISH;
        }
    }

    public static int getMinSize(String boardType) {
        switch (boardType) {
            case HEXAGON: return MIN_SIZE_HEXAGON;
            case DIAMOND: return MIN_SIZE_DIAMOND;
            default:      return MIN_SIZE_ENGLISH;
        }
    }
}