package com.dawidweiss.dyna.view.swing;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.view.resources.ImageUtilities;
import com.dawidweiss.dyna.view.resources.Images;
import com.dawidweiss.dyna.view.resources.ImagesFactory;

/**
 * Swing board view.
 */
@SuppressWarnings("serial")
public final class BoardFrame extends JFrame implements IGameEventListener
{
    private final GraphicsConfiguration conf;
    private BoardPanel gamePanel;
    private ScorePanel scorePanel;

    private BoardFrame(GraphicsConfiguration conf)
    {
        this.conf = conf;
    }

    public BoardFrame()
    {
        this(ImageUtilities.getGraphicsConfiguration());
        final Images images = ImagesFactory.DYNA_CLASSIC;

        gamePanel = new BoardPanel(images, conf);
        gamePanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e)
            {
                pack();
            }
        });

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        getContentPane().add(panel);
        panel.add(gamePanel, BorderLayout.CENTER);

        scorePanel = new ScorePanel();
        panel.add(scorePanel, BorderLayout.SOUTH);

        setLocationByPlatform(true);
        setIgnoreRepaint(true);
        setFocusTraversalKeysEnabled(false);
        getRootPane().setDoubleBuffered(false);
        setResizable(false);
        pack();
        try
        {
            setIconImage(ImageUtilities.loadResourceImage("icons/window-icon.png"));
        }
        catch (IOException e)
        {
            // Ignore.
        }
        setTitle("Play responsibly.");
    }

    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        gamePanel.onFrame(frame, events);
        scorePanel.onFrame(frame, events);
    }
}
