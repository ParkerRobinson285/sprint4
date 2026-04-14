package ui;
 
import game.Game;
 
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
 
/**
 * Displays live game status in a bar below the board.
 *
 * Shows three pieces of information:
 *   - Pegs remaining  — updated after every move
 *   - Current rating  — Outstanding / Very Good / Good / Average
 *   - Game over message, or replay progress when in replay mode
 *
 * Call refresh(game) after every move to keep the display current.
 * Call setReplayMode(true, movesRemaining) during replay to show progress.
 */
public class GameStatusPanel extends JPanel {
 
    private final JLabel pegCountLabel;
    private final JLabel ratingLabel;
    private final JLabel messageLabel;   // game over text OR replay progress
 
    public GameStatusPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 24, 6));
        setBackground(new Color(235, 210, 160));
        setBorder(new EmptyBorder(4, 8, 4, 8));
 
        pegCountLabel = makeLabel("Pegs: --",  new Color(40, 40, 100),  14f);
        ratingLabel   = makeLabel("",           new Color(60, 100, 60),  13f);
        messageLabel  = makeLabel("",           new Color(180, 30, 30),  15f);
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.BOLD, 15f));
 
        add(pegCountLabel);
        add(ratingLabel);
        add(messageLabel);
    }
 
    /**
     * Updates all labels to reflect the current game state.
     * Safe to call from the Swing EDT after every move.
     */
    public void refresh(Game game) {
        if (game == null) {
            pegCountLabel.setText("Pegs: --");
            ratingLabel.setText("");
            messageLabel.setText("");
            return;
        }
 
        pegCountLabel.setText("Pegs remaining: " + game.getPegCount());
        ratingLabel.setText("Rating: " + game.getRating());
 
        if (game.isOver()) {
            // Show final rating prominently; hide the separate rating label to avoid duplication
            messageLabel.setForeground(new Color(180, 30, 30));
            messageLabel.setText("Game Over!  Final rating: " + game.getRating());
            ratingLabel.setText("");
        } else {
            messageLabel.setText("");
        }
    }
 
    /**
     * Switches the message label between normal game-over mode and replay progress mode.
     *
     * In replay mode the label shows how many moves are still to be replayed,
     * giving the player a sense of progress through the recording.
     *
     * @param inReplay       true to show replay progress; false for normal mode
     * @param movesRemaining the number of recorded moves not yet applied
     */
    public void setReplayMode(boolean inReplay, int movesRemaining) {
        if (inReplay) {
            messageLabel.setForeground(new Color(40, 100, 160));
            messageLabel.setText("Replaying — " + movesRemaining + " moves remaining");
        } else {
            messageLabel.setForeground(new Color(180, 30, 30));
            messageLabel.setText("");
        }
    }
 
    // ── Helper ────────────────────────────────────────────────────────────────
 
    private JLabel makeLabel(String text, Color color, float size) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(label.getFont().deriveFont(size));
        return label;
    }
}