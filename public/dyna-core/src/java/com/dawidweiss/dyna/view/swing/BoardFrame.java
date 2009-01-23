package com.dawidweiss.dyna.view.swing;

import java.awt.GraphicsConfiguration;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JFrame;

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

        getContentPane().add(gamePanel);
        setLocationByPlatform(true);
        setIgnoreRepaint(true);
        setFocusTraversalKeysEnabled(false);
        getRootPane().setDoubleBuffered(false);
        setResizable(false);
        pack();
        setIconImage(new BufferedImage(16, 16, BufferedImage.TRANSLUCENT));
        setTitle("Play responsibly.");
    }

    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        gamePanel.onFrame(frame, events);
    }
}
