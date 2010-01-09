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
    private final GraphicsConfiguration conf;
    private BoardPanel gamePanel;

    /**
     * Attached score frame, if any.
     */
    private ScoreFrame scoreFrame;
    
    /**
     * List of attached status frames.
     */
    private ArrayList<StatusFrame> statusFrames;

    /*
     * 
     */
    private BoardFrame(GraphicsConfiguration conf)
    {
        this.conf = conf;
    }

    /*
     * 
     */
    public BoardFrame()
    {
        this(ImageUtilities.getGraphicsConfiguration());
        final Images images = ImagesFactory.DYNA_CLASSIC;

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
        
        statusFrames = Lists.newArrayList();

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        getContentPane().add(panel);
        panel.add(gamePanel, BorderLayout.CENTER);

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
         * Add a dependent frame. Whenever this frame is opened, the dependent frame 
         * is also opened.
         * 
         * TODO: [future] Snapping and gluing in Swing is hacky here and it won't work
         * with so many open windows. The status should be integrated with the main frame,
         * perhaps the scoreboard should be as well.
         */
        addWindowListener(new WindowAdapter()
        {
            public void windowOpened(WindowEvent e)
            {
            	for (int statusIndex = 0; statusIndex < statusFrames.size(); statusIndex++)
            	{
            	    statusFrames.get(statusIndex).setVisible(true);
            	    if (statusIndex == 0)
            	    {
            	        SwingUtils.glueTo(BoardFrame.this, statusFrames.get(statusIndex), SwingUtils.SnapSide.RIGHT);
            	        SwingUtils.snapFrame(BoardFrame.this, statusFrames.get(statusIndex));
            	    }
            	    else
            	    {
            	        SwingUtils.glueTo(statusFrames.get(statusIndex - 1), statusFrames.get(statusIndex), SwingUtils.SnapSide.BOTTOM);
                        SwingUtils.snapFrame(statusFrames.get(statusIndex - 1), statusFrames.get(statusIndex));
            	    }
            	}

            	scoreFrame.setVisible(true);
                SwingUtils.glueTo(BoardFrame.this.statusFrames.get(statusFrames.size() - 1), scoreFrame, SwingUtils.SnapSide.BOTTOM);
                SwingUtils.snapFrame(BoardFrame.this.statusFrames.get(statusFrames.size() - 1), scoreFrame);                
            }
            
            public void windowClosed(WindowEvent e)
            {
                scoreFrame.dispose();
                for (int statusIndex = 0; statusIndex < statusFrames.size(); statusIndex++)
                {
                    statusFrames.get(statusIndex).dispose();
                }
            }
        });

        pack();
    }

    /**
     * React to on-frame events propagating to sub-views.
     */
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        this.gamePanel.onFrame(frame, events);
        this.scoreFrame.onFrame(frame, events);
        for (StatusFrame f : statusFrames)
        {
            f.onFrame(frame, events);
        }
    }

    /**
     * Return the game panel associated with this frame.
     */
    public BoardPanel getGamePanel()
    {
        return gamePanel;
    }

    /**
     * Creates frames with statistics about player(s). The main frame must
     * not be visible at the time of calling.
     */
    public void createStatusFrames(String... players)
    {
        assert !isVisible();

        for (String playerName : players)
        {
            statusFrames.add(new StatusFrame(playerName));
        }
    }
}
