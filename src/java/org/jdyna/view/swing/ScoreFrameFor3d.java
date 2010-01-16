package org.jdyna.view.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.jdyna.GameEvent;
import org.jdyna.IGameEventListener;
import org.jdyna.IPlayerSprite;
import org.jdyna.ISprite;
import org.jdyna.view.resources.ImageUtilities;
import org.jdyna.view.resources.Images;
import org.jdyna.view.resources.ImagesFactory;

import com.google.common.collect.Lists;


/**
 * Swing scoreboard view.
 */
@SuppressWarnings("serial")
public final class ScoreFrameFor3d extends JFrame implements IGameEventListener
{
    private PlayersScorePanel playerScorePanel;
    private TeamScorePanel teamScorePanel;
    private JPanel statuses;
    
    /**
     * Sub-listeners.
     */
    private final ArrayList<IGameEventListener> sublisteners = Lists.newArrayList();

    /*
     * 
     */
    public ScoreFrameFor3d()
    {
        playerScorePanel = new PlayersScorePanel();
        teamScorePanel = new TeamScorePanel();

        // Set preferred sizes only to have proportional divider in place.
        playerScorePanel.setPreferredSize(new Dimension(200, 200));
        teamScorePanel.setPreferredSize(new Dimension(200, 50));

        final JSplitPane panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        getContentPane().add(panel, BorderLayout.CENTER);

        panel.setTopComponent(playerScorePanel);
        panel.setBottomComponent(teamScorePanel);
        
        this.statuses = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        getContentPane().add(statuses, BorderLayout.SOUTH);
        
        sublisteners.add(playerScorePanel);
        sublisteners.add(teamScorePanel);

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
        for (IGameEventListener l : sublisteners)
        {
            l.onFrame(frame, events);
        }
    }
    
    /**
     * Add status panels for these players. You should call this method
     * once with all the players that require status information.
     */
    public void showStatusFor(IPlayerSprite... players)
    {
        final Images images = ImagesFactory.DYNA_CLASSIC;
        for (IPlayerSprite player : players)
        {
            final PlayerStatusPanel p = new PlayerStatusPanel(
                images, player.getName(), player.getType());

            sublisteners.add(p);
            statuses.add(p);
        }
    }
    
    public void showStatusFor(String playerName)
    {
        final Images images = ImagesFactory.DYNA_CLASSIC;
        final PlayerStatusPanel p = new PlayerStatusPanel(
                images, playerName, ISprite.Type.PLAYER_1);
        sublisteners.add(p);
        statuses.add(p);
    }
}
