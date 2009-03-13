package org.jdyna.view.swing;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import org.jdyna.GameEvent;
import org.jdyna.IGameEventListener;
import org.jdyna.view.resources.ImageUtilities;


/**
 * Swing scoreboard view.
 */
@SuppressWarnings("serial")
public final class ScoreFrame extends JFrame implements IGameEventListener
{
    private PlayerScorePanel playerScorePanel;
    private TeamScorePanel teamScorePanel;

    /*
     * 
     */
    public ScoreFrame()
    {
        playerScorePanel = new PlayerScorePanel();
        teamScorePanel = new TeamScorePanel();

        // Set preferred sizes only to have proportional divider in place.
        playerScorePanel.setPreferredSize(new Dimension(200, 200));
        teamScorePanel.setPreferredSize(new Dimension(200, 50));

        final JSplitPane panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        getContentPane().add(panel);

        panel.setTopComponent(playerScorePanel);
        panel.setBottomComponent(teamScorePanel);

        setLocationByPlatform(false);
        setFocusTraversalKeysEnabled(false);
        setResizable(true);

        try
        {
            setIconImage(ImageUtilities.loadResourceImage("icons/window-icon.png"));
        }
        catch (IOException e)
        {
            // Ignore.
        }
        setTitle("Scoreboard.");
    }

    /**
     * Update the scoreboard.
     */
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        playerScorePanel.onFrame(frame, events);
        teamScorePanel.onFrame(frame, events);
    }
}
