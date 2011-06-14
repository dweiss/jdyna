package org.jdyna.view.swing;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;

import javax.swing.*;

import org.jdyna.*;
import org.jdyna.view.resources.*;
import org.jdyna.view.status.StatusFrame;


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
     * Attached status frame.
     */
    private StatusFrame statusFrame;

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
        
        this.statusFrame = new StatusFrame();
        statusFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        statusFrame.setSize(new Dimension(300, 80));
        statusFrame.setFocusable(false);        

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
         * Add a dependent frame. Whenever this frame is opened, the dependent frame is also opened.  
         */
        addWindowListener(new WindowAdapter()
        {
            public void windowOpened(WindowEvent e)
            {                
            	statusFrame.setVisible(true);
                SwingUtils.glueTo(BoardFrame.this, statusFrame, SwingUtils.SnapSide.RIGHT);
                SwingUtils.snapFrame(BoardFrame.this, statusFrame);
            	scoreFrame.setVisible(true);
                SwingUtils.glueTo(BoardFrame.this.statusFrame, scoreFrame, SwingUtils.SnapSide.BOTTOM);
                SwingUtils.snapFrame(BoardFrame.this.statusFrame, scoreFrame);                
            }
            
            public void windowClosed(WindowEvent e)
            {
                scoreFrame.dispose();
                statusFrame.dispose();
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
        this.statusFrame.onFrame(frame, events);
    }

    /**
     * Return the game panel associated with this frame.
     */
    public BoardPanel getGamePanel()
    {
        return gamePanel;
    }
}
