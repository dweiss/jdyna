package com.dawidweiss.dyna.view.swing;

import java.awt.GraphicsConfiguration;

import javax.swing.JFrame;

import com.dawidweiss.dyna.IGameListener;
import com.dawidweiss.dyna.view.BoardInfo;
import com.dawidweiss.dyna.view.IBoardSnapshot;
import com.dawidweiss.dyna.view.resources.ImageUtilities;
import com.dawidweiss.dyna.view.resources.Images;
import com.dawidweiss.dyna.view.resources.ImagesFactory;

/**
 * Swing board view.
 */
@SuppressWarnings("serial")
public final class BoardFrame extends JFrame implements IGameListener
{
    private final GraphicsConfiguration conf;
    private BoardPanel gamePanel;

    private BoardFrame(GraphicsConfiguration conf)
    {
        this.conf = conf;
    }

    public BoardFrame(BoardInfo boardInfo)
    {
        this(ImageUtilities.getGraphicsConfiguration());
        final Images images = ImagesFactory.DYNA_CLASSIC;

        gamePanel = new BoardPanel(boardInfo, images, conf);
        getContentPane().add(gamePanel);
        setLocationByPlatform(true);
        setIgnoreRepaint(true);
        setFocusTraversalKeysEnabled(false);
        getRootPane().setDoubleBuffered(false);
        pack();
    }

    public void onNextFrame(int frame, IBoardSnapshot snapshot)
    {
        gamePanel.onNextFrame(frame, snapshot);
    }
}
