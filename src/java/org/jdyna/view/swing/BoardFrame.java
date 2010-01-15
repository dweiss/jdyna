package org.jdyna.view.swing;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.jdyna.*;
import org.jdyna.view.resources.*;

import com.google.common.collect.Lists;

/**
 * Swing board view.
 */
@SuppressWarnings("serial")
public final class BoardFrame extends JFrame implements IGameEventListener
{
    private BoardPanel gamePanel;

    /**
     * Attached score frame, if any.
     */
    private ScoreFrame scoreFrame;

    /**
     * Sub-listeners.
     */
    private final ArrayList<IGameEventListener> sublisteners = Lists.newArrayList();

    /**
     * Images used by this frame.
     */
    private final Images images;

    /**
     * South panel with player statuses.
     */
    private final JPanel statuses;

    /*
     * 
     */
    public BoardFrame()
    {
        this(ImageUtilities.getGraphicsConfiguration());
    }

    /*
     * 
     */
    public BoardFrame(GraphicsConfiguration conf)
    {
        this.images = ImagesFactory.DYNA_CLASSIC;

        gamePanel = new BoardPanel(images, conf);
        gamePanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e)
            {
                /*
                 * Resize the entire frame when the game panel changes size.
                 */
                BoardFrame.this.validate();
                BoardFrame.this.setSize(BoardFrame.this.getPreferredSize());
            }
        });

        /*
         * Create the score frame.
         */
        this.scoreFrame = new ScoreFrame();
        scoreFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        scoreFrame.setSize(new Dimension(300, 500));
        scoreFrame.setFocusable(false);      

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        getContentPane().add(panel);
        panel.add(gamePanel, BorderLayout.CENTER);

        this.statuses = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.add(statuses, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationByPlatform(true);
        setFocusTraversalKeysEnabled(false);
        getRootPane().setDoubleBuffered(false);
        setResizable(false);
        try
        {
            setIconImage(ImageUtilities.loadResourceImage("icons/window-icon.png"));
        }
        catch (IOException e)
        {
            // Ignore.
        }
        setTitle("Play responsibly.");

        /*
         * Add the scoreboard frame.
         */
        addWindowListener(new WindowAdapter()
        {
            public void windowOpened(WindowEvent e)
            {
                scoreFrame.setVisible(true);
                SwingUtils.glueTo(BoardFrame.this, scoreFrame, SwingUtils.SnapSide.RIGHT);
                SwingUtils.snapFrame(BoardFrame.this, scoreFrame);
            }

            public void windowClosed(WindowEvent e)
            {
                scoreFrame.dispose();
            }
        });

        sublisteners.add(gamePanel);
        sublisteners.add(scoreFrame);

        pack();
    }

    /**
     * React to on-frame events propagating to sub-views.
     */
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        for (IGameEventListener l : sublisteners)
        {
            l.onFrame(frame, events);
        }
    }

    /**
     * Track the given player (only one).
     */
    public void trackPlayer(String playerName)
    {
        gamePanel.trackPlayer(playerName);
    }

    /**
     * Add status panels for these players. You should call this method
     * once with all the players that require status information.
     */
    public void showStatusFor(IPlayerSprite... players)
    {
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
        final PlayerStatusPanel p = new PlayerStatusPanel(
                images, playerName, ISprite.Type.PLAYER_1);
        sublisteners.add(p);
        statuses.add(p);
    }
}
