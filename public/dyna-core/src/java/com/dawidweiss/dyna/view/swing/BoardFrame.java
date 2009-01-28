package com.dawidweiss.dyna.view.swing;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.dawidweiss.dyna.*;
import com.dawidweiss.dyna.view.resources.*;

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
                pack();
            }
        });

        /*
         * Create the score frame.
         */
        this.scoreFrame = new ScoreFrame();
        scoreFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        scoreFrame.setSize(new Dimension(300, 500));
        scoreFrame.setFocusable(false);
        scoreFrame.setFocusableWindowState(false);

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        getContentPane().add(panel);
        panel.add(gamePanel, BorderLayout.CENTER);

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
         * Add a dependent frame. Whenever this frame is opened, the dependent frame is also opened.  
         */
        addWindowListener(new WindowAdapter()
        {
            public void windowOpened(WindowEvent e)
            {
                scoreFrame.setVisible(true);
                SwingUtils.glueTo(BoardFrame.this, scoreFrame, SwingUtils.SnapSide.RIGHT);
                SwingUtils.snapFrame(BoardFrame.this, scoreFrame);
            }

            public void windowClosing(WindowEvent e)
            {
                scoreFrame.dispose();
            }
        });

        pack();
    }

    /**
     * React to on-frame events.
     */
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        this.gamePanel.onFrame(frame, events);
        this.scoreFrame.onFrame(frame, events);
    }

    /**
     * Return the game panel associated with this frame.
     */
    public BoardPanel getGamePanel()
    {
        return gamePanel;
    }
}
