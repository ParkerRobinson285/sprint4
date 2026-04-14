package ui;
 
import game.AutoGame;
import game.Game;
import game.GameFactory;
import game.ManualGame;
import game.ReplayGame;
import model.Board;
import record.GameLoader;
import record.GameLoader.RecordedGame;
import record.GameRecorder;
 
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
 
/**
 * The main application window for Peg Solitaire.
 *
 * Layout:
 *   ┌──────────────────────────────────────────────────┐
 *   │  [Sidebar]              [BoardPanel]              │
 *   │  Board Type                                       │
 *   │   ○ English                                       │
 *   │   ○ Hexagon      (board drawn here)               │
 *   │   ○ Diamond                                       │
 *   │                                                   │
 *   │  Board size: [7] (odd only)                       │
 *   │                                                   │
 *   │  [New Game]   ☑ Record game                       │
 *   │  [Autoplay]                                       │
 *   │  [Randomize]                                      │
 *   │  [Replay]                                         │
 *   ├──────────────────────────────────────────────────┤
 *   │  [GameStatusPanel]                                │
 *   └──────────────────────────────────────────────────┘
 *
 * Autoplay and Replay both use a javax.swing.Timer to step one move per
 * tick (400ms), producing step-by-step animation on the board.
 *
 * Recording: when the "Record game" checkbox is ticked, the game's move
 * history is saved to a .txt file (chosen via file dialog) at game end.
 * Replay: the Replay button opens a saved .txt file and plays it back.
 */
public class MainWindow extends JFrame {
 
    // Milliseconds between each move tick for Autoplay and Replay
    private static final int TIMER_DELAY_MS = 400;
 
    // ── Child components ──────────────────────────────────────────────────────
 
    private final BoardPanel      boardPanel;
    private final GameStatusPanel statusPanel;
 
    // ── Sidebar controls ──────────────────────────────────────────────────────
 
    private final JRadioButton rbEnglish;
    private final JRadioButton rbHexagon;
    private final JRadioButton rbDiamond;
    private final JSpinner     sizeSpinner;
    private final JCheckBox    chkRecord;     // "Record game" toggle
    private final JButton      btnNewGame;
    private final JButton      btnAutoplay;
    private final JButton      btnRandomize;
    private final JButton      btnReplay;     // opens a saved file and replays it
 
    // ── State ─────────────────────────────────────────────────────────────────
 
    private Game  currentGame;
    private Timer gameTimer;        // shared timer for both Autoplay and Replay
 
    // Stored when a game starts so we can pass them to GameRecorder at game end
    private String currentBoardType;
    private int    currentBoardSize;
    private String currentGameMode;
 
    // When true, radio button and spinner changes do not trigger a new game.
    // Set during syncSidebarToLoaded() so programmatic sidebar updates during
    // replay setup don't cause ghost repaints or unwanted new games.
    private boolean suppressBoardTypeChange = false;
 
    // Tracks the board size used on the last pack() call so we only repack
    // when the board size actually changes, avoiding ghost repaint artifacts.
    private int lastPackedSize = -1;
 
    private static final Color SIDEBAR_BG = new Color(235, 210, 160);
 
    // ── Construction ──────────────────────────────────────────────────────────
 
    public MainWindow() {
        super("Peg Solitaire");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
 
        boardPanel  = new BoardPanel();
        statusPanel = new GameStatusPanel();
 
        // When the player successfully makes a move, refresh the status bar.
        // If the game is now over, save the recording (if enabled) and show dialog.
        boardPanel.setOnMoveApplied(() -> {
            statusPanel.refresh(currentGame);
            if (currentGame != null && currentGame.isOver())
                handleGameOver();
        });
 
        // ── Board type radio buttons ──
        rbEnglish = new JRadioButton("English", true);
        rbHexagon = new JRadioButton("Hexagon");
        rbDiamond = new JRadioButton("Diamond");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbEnglish); bg.add(rbHexagon); bg.add(rbDiamond);
 
        // Changing board type resets the spinner to the new type's default and starts a new game
        rbEnglish.addActionListener(e -> onBoardTypeChanged());
        rbHexagon.addActionListener(e -> onBoardTypeChanged());
        rbDiamond.addActionListener(e -> onBoardTypeChanged());
 
        // ── Board size spinner (odd numbers only, step = 2) ──
        // SpinnerNumberModel(initialValue, min, max, stepSize)
        sizeSpinner = new JSpinner(new SpinnerNumberModel(7, 5, 25, 2));
        sizeSpinner.setMaximumSize(new Dimension(60, 28));
        sizeSpinner.setPreferredSize(new Dimension(60, 28));
        // Note: spinner changes do NOT auto-start — player must press New Game
        // to avoid starting a new game mid-edit while typing a custom size
 
        // ── Record checkbox ──
        chkRecord = new JCheckBox("Record game");
        chkRecord.setBackground(SIDEBAR_BG);
        chkRecord.setToolTipText("Save this game to a file when it ends");
 
        // ── Buttons ──
        btnNewGame   = new JButton("New Game");
        btnAutoplay  = new JButton("Autoplay");
        btnRandomize = new JButton("Randomize");
        btnReplay    = new JButton("Replay");
 
        styleButton(btnAutoplay,  new Color(100, 180, 80),  new Color(20, 60, 20));
        styleButton(btnRandomize, new Color(80,  140, 200), new Color(10, 30, 80));
        styleButton(btnReplay,    new Color(180, 120, 40),  new Color(60, 30, 0));
 
        btnNewGame.addActionListener(e   -> startNewManualGame());
        btnAutoplay.addActionListener(e  -> startAutoplay());
        btnRandomize.addActionListener(e -> randomizeBoard());
        btnReplay.addActionListener(e    -> startReplay());
 
        // ── Window layout ──
        setLayout(new BorderLayout(8, 8));
        add(buildSidebar(), BorderLayout.WEST);
        add(boardPanel,     BorderLayout.CENTER);
        add(statusPanel,    BorderLayout.SOUTH);
 
        startNewManualGame();
 
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(640, 480));
    }
 
    // ── Game control ──────────────────────────────────────────────────────────
 
    /**
     * Stops any running timer and starts a fresh manual game with the currently
     * selected board type and size. Enables the Randomize and Autoplay buttons.
     */
    private void startNewManualGame() {
        stopTimer();
        currentBoardType = getSelectedBoardType();
        currentBoardSize = getSelectedSize();
        currentGameMode  = GameFactory.MANUAL;
 
        currentGame = GameFactory.create(currentBoardType, GameFactory.MANUAL, currentBoardSize);
        boardPanel.setGame(currentGame);
        statusPanel.refresh(currentGame);
 
        btnRandomize.setEnabled(true);
        btnAutoplay.setEnabled(true);
        btnReplay.setEnabled(true);
        chkRecord.setEnabled(true);
        packIfSizeChanged();
    }
 
    /**
     * Starts an Autoplay game on the currently selected board type and size.
     *
     * A javax.swing.Timer fires every TIMER_DELAY_MS milliseconds. On each tick
     * it calls AutoGame.makeMove(null) and repaints the board, producing the
     * step-by-step animation required by AC 6.2. When the game ends the timer
     * stops automatically.
     */
    private void startAutoplay() {
        stopTimer();
        currentBoardType = getSelectedBoardType();
        currentBoardSize = getSelectedSize();
        currentGameMode  = GameFactory.AUTOPLAY;
 
        currentGame = GameFactory.create(currentBoardType, GameFactory.AUTOPLAY, currentBoardSize);
        boardPanel.setGame(currentGame);
        statusPanel.refresh(currentGame);
 
        // Disable controls that should not be active during autoplay
        btnRandomize.setEnabled(false);
        btnAutoplay.setEnabled(false);
        btnReplay.setEnabled(false);
        chkRecord.setEnabled(false);
 
        gameTimer = new Timer(TIMER_DELAY_MS, e -> {
            if (currentGame.isOver()) {
                stopTimer();
                statusPanel.refresh(currentGame);
                handleGameOver();
                return;
            }
            ((AutoGame) currentGame).makeMove(null);
            boardPanel.refresh();
            statusPanel.refresh(currentGame);
        });
        gameTimer.start();
        packIfSizeChanged();
    }
 
    /**
     * Randomizes the board state during a manual game by applying a series of
     * random valid moves. Has no effect during Autoplay or Replay.
     * Randomization moves are not recorded (AC 8.3).
     */
    private void randomizeBoard() {
        if (currentGame instanceof ManualGame) {
            ((ManualGame) currentGame).randomize();
            boardPanel.refresh();
            statusPanel.refresh(currentGame);
        }
    }
 
    /**
     * Opens a file chooser for the player to select a saved game file,
     * then replays it step-by-step using the same timer mechanism as Autoplay.
     *
     * The board is reconstructed from the file header (board type, size, mode)
     * so the replay starts from the exact same initial state as the original game.
     * Moves are applied one per timer tick so the player can watch the replay animate.
     */
    private void startReplay() {
        // Ask the player to choose a saved game file
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a saved game file");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Peg Solitaire saves (*.txt)", "txt"));
 
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
 
        File file = chooser.getSelectedFile();
 
        RecordedGame rec;
        try {
            rec = GameLoader.load(file);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Could not load game file:\n" + ex.getMessage(),
                "Load Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
 
        stopTimer();
 
        // Reconstruct the original board in its fresh starting state
        currentBoardType = rec.boardType;
        currentBoardSize = rec.boardSize;
        currentGameMode  = "Replay";
 
        // Sync the sidebar controls to match the loaded game's settings
        syncSidebarToLoaded(rec);
 
        // Create the board fresh and wrap it in a ReplayGame with the recorded moves
        Board replayBoard = GameFactory.createBoard(rec.boardType, rec.boardSize);
        currentGame = new ReplayGame(replayBoard, rec.moves);
 
        boardPanel.setGame(currentGame);
        statusPanel.refresh(currentGame);
        statusPanel.setReplayMode(true, rec.moves.size());
 
        // Disable all controls that should not be active during replay
        btnRandomize.setEnabled(false);
        btnAutoplay.setEnabled(false);
        btnReplay.setEnabled(false);
        btnNewGame.setEnabled(false);
        chkRecord.setEnabled(false);
 
        gameTimer = new Timer(TIMER_DELAY_MS, e -> {
            if (currentGame.isOver()) {
                stopTimer();
                btnNewGame.setEnabled(true);
                btnReplay.setEnabled(true);
                statusPanel.refresh(currentGame);
                statusPanel.setReplayMode(false, 0);
                JOptionPane.showMessageDialog(this,
                    "Replay complete!\n\nMoves replayed: " + rec.moves.size()
                    + "\nFinal pegs: "  + currentGame.getPegCount()
                    + "\nRating: "      + currentGame.getRating(),
                    "Replay Complete", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            ((ReplayGame) currentGame).makeMove(null);
            boardPanel.refresh();
            statusPanel.refresh(currentGame);
            // Update the move counter in the status bar
            int remaining = ((ReplayGame) currentGame).getMovesRemaining();
            statusPanel.setReplayMode(true, remaining);
        });
        gameTimer.start();
        packIfSizeChanged();
    }
 
    /**
     * Stops the autoplay or replay timer if one is running.
     * Re-enables the Autoplay and Randomize buttons.
     */
    private void stopTimer() {
        if (gameTimer != null && gameTimer.isRunning())
            gameTimer.stop();
        btnAutoplay.setEnabled(true);
        btnRandomize.setEnabled(true);
        btnNewGame.setEnabled(true);
        btnReplay.setEnabled(true);
        chkRecord.setEnabled(true);
    }
 
    /**
     * Called when a game ends (manual or autoplay).
     * Shows the game-over dialog, then saves the recording if the checkbox is ticked.
     * The save dialog is deferred via invokeLater so it opens cleanly after the
     * game-over dialog has fully closed and the window has regained focus.
     */
    private void handleGameOver() {
        showGameOverDialog();
 
        // Re-enable controls now that the game has ended
        btnAutoplay.setEnabled(true);
        btnRandomize.setEnabled(true);
        btnReplay.setEnabled(true);
        chkRecord.setEnabled(true);
 
        // Defer the save dialog so it appears after the game-over dialog is gone
        if (chkRecord.isSelected() && !(currentGame instanceof ReplayGame)) {
            SwingUtilities.invokeLater(this::saveRecording);
        }
    }
 
    /**
     * Prompts the player for a save location and writes the game's move history
     * to a plain-text file using GameRecorder.
     */
    private void saveRecording() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save game recording");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Peg Solitaire saves (*.txt)", "txt"));
        // Default filename based on board type and mode
        chooser.setSelectedFile(new File(
            currentBoardType + "_" + currentGameMode + "_" + currentBoardSize + ".txt"));
 
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
 
        File file = chooser.getSelectedFile();
        // Ensure the file has a .txt extension
        if (!file.getName().endsWith(".txt"))
            file = new File(file.getAbsolutePath() + ".txt");
 
        try {
            GameRecorder.save(currentGame, currentBoardType, currentBoardSize,
                              currentGameMode, file);
            JOptionPane.showMessageDialog(this,
                "Game saved to:\n" + file.getAbsolutePath(),
                "Game Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Could not save game:\n" + ex.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    // ── Event handlers ────────────────────────────────────────────────────────
 
    /**
     * Called when the player selects a different board type radio button.
     * Updates the spinner's minimum and default values for the new type,
     * then starts a fresh manual game automatically.
     */
    private void onBoardTypeChanged() {
        if (suppressBoardTypeChange) return; // programmatic update — skip new game
        String boardType = getSelectedBoardType();
        int minSize      = GameFactory.getMinSize(boardType);
        int defaultSize  = GameFactory.getDefaultSize(boardType);
 
        SpinnerNumberModel model = (SpinnerNumberModel) sizeSpinner.getModel();
        int current = (int) sizeSpinner.getValue();
        int newVal  = Math.max(current, minSize);
        if (newVal % 2 == 0) newVal++; // keep odd
 
        model.setMinimum(minSize);
        model.setValue(defaultSize);
 
        startNewManualGame();
    }
 
    /**
     * Syncs the sidebar radio buttons and spinner to match a loaded game's settings.
     * Called before starting a replay so the controls reflect what is being replayed.
     */
    private void syncSidebarToLoaded(RecordedGame rec) {
        suppressBoardTypeChange = true; // prevent radio/spinner changes from starting a new game
        try {
            switch (rec.boardType) {
                case GameFactory.HEXAGON: rbHexagon.setSelected(true); break;
                case GameFactory.DIAMOND: rbDiamond.setSelected(true); break;
                default:                  rbEnglish.setSelected(true); break;
            }
            SpinnerNumberModel model = (SpinnerNumberModel) sizeSpinner.getModel();
            model.setMinimum(GameFactory.getMinSize(rec.boardType));
            model.setValue(rec.boardSize);
        } finally {
            suppressBoardTypeChange = false; // always restore, even if an exception occurs
        }
    }
 
    // ── Helpers ───────────────────────────────────────────────────────────────
 
    /**
     * Calls pack() only when the current board size differs from the last time
     * pack() was called. This resizes the window to fit larger or smaller boards
     * without triggering the ghost repaint artifacts that occur when pack() is
     * called repeatedly on an already-visible window.
     */
    private void packIfSizeChanged() {
        int currentSize = (currentGame != null) ? currentGame.getBoard().getSize() : -1;
        if (currentSize != lastPackedSize) {
            pack();
            lastPackedSize = currentSize;
        }
    }
 
    /** Returns the board type string matching the selected radio button. */
    private String getSelectedBoardType() {
        if (rbHexagon.isSelected()) return GameFactory.HEXAGON;
        if (rbDiamond.isSelected()) return GameFactory.DIAMOND;
        return GameFactory.ENGLISH;
    }
 
    /**
     * Returns the spinner's current value, guaranteed to be odd.
     * If the user somehow enters an even number, rounds up to the next odd.
     */
    private int getSelectedSize() {
        int val = (int) sizeSpinner.getValue();
        if (val % 2 == 0) val++;
        return val;
    }
 
    /** Displays the game-over dialog showing peg count and rating. */
    private void showGameOverDialog() {
        int pegs = currentGame.getPegCount();
        String msg = String.format("Game Over!\n\nPegs remaining: %d\nRating: %s",
            pegs, currentGame.getRating());
        JOptionPane.showMessageDialog(this, msg, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }
 
    /** Applies consistent styling to the coloured action buttons. */
    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD));
    }
 
    // ── Sidebar layout ────────────────────────────────────────────────────────
 
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(12, 12, 12, 12));
        sidebar.setBackground(SIDEBAR_BG);
 
        // Board Type radio group
        JPanel typePanel = new JPanel();
        typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.Y_AXIS));
        typePanel.setBorder(new TitledBorder("Board Type"));
        typePanel.setBackground(SIDEBAR_BG);
        for (JRadioButton rb : new JRadioButton[]{rbEnglish, rbHexagon, rbDiamond}) {
            rb.setBackground(SIDEBAR_BG);
            typePanel.add(rb);
        }
 
        // Board size row
        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        sizePanel.setBackground(SIDEBAR_BG);
        sizePanel.add(new JLabel("Board size:"));
        sizePanel.add(sizeSpinner);
        JLabel oddNote = new JLabel("(odd only)");
        oddNote.setFont(oddNote.getFont().deriveFont(10f));
        oddNote.setForeground(Color.DARK_GRAY);
        sizePanel.add(oddNote);
 
        Dimension spacer = new Dimension(0, 10);
 
        sidebar.add(typePanel);
        sidebar.add(Box.createRigidArea(spacer));
        sidebar.add(sizePanel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 16)));
        sidebar.add(makeFullWidthButton(btnNewGame));
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(chkRecord);               // Record game checkbox sits below New Game
        sidebar.add(Box.createRigidArea(spacer));
        sidebar.add(makeFullWidthButton(btnAutoplay));
        sidebar.add(Box.createRigidArea(spacer));
        sidebar.add(makeFullWidthButton(btnRandomize));
        sidebar.add(Box.createRigidArea(spacer));
        sidebar.add(makeFullWidthButton(btnReplay));
        sidebar.add(Box.createVerticalGlue());
 
        return sidebar;
    }
 
    private JPanel makeFullWidthButton(JButton btn) {
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btn.getPreferredSize().height));
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(SIDEBAR_BG);
        wrapper.add(btn, BorderLayout.CENTER);
        return wrapper;
    }
 
    // ── Entry point ───────────────────────────────────────────────────────────
 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}