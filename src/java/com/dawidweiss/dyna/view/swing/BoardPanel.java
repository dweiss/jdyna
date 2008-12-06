package com.dawidweiss.dyna.view.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.IGameListener;
import com.dawidweiss.dyna.view.BoardInfo;
import com.dawidweiss.dyna.view.IBoardSnapshot;
import com.dawidweiss.dyna.view.ISprite;
import com.dawidweiss.dyna.view.resources.Images;

/**
 * Swing's {@link JPanel} that displays the state and changes on the playfield during the
 * game.
 */
@SuppressWarnings("serial")
public final class BoardPanel extends JPanel implements IGameListener
{
    /**
     * Exclusive lock so that drawing and updating does not take place at the same time.
     */
    private final Object exclusiveLock = new Object();

    /**
     * The latest board's background state.
     */
    private final BufferedImage background;

    /*
     * Board information.
     */
    private BoardInfo boardInfo;

    /**
     * A set of required images.
     */
    private Images images;

    /**
     * 
     */
    public BoardPanel(BoardInfo boardInfo, Images images, GraphicsConfiguration conf)
    {
        this.boardInfo = boardInfo;
        this.images = images.createCompatible(conf);

        /*
         * TODO: correct buffering strategy.
         */
        this.setDoubleBuffered(false);

        /*
         * Initial board update.
         */
        final Dimension size = getPreferredSize();
        background = conf.createCompatibleImage(size.width, size.height);
    }

    /**
     * Update {@link #background} because the board changed.
     */
    public void updateBoard(IBoardSnapshot snapshot)
    {
        final Graphics2D g = background.createGraphics();

        final BufferedImage backgroundImage = getCellImage(CellType.CELL_EMPTY, 0);
        final Color backgroundColor = new Color(backgroundImage.getRGB(0, 0));

        synchronized (exclusiveLock)
        {
            /*
             * Erase the background with approximated background color.
             */
            g.setColor(backgroundColor);
            g.fillRect(0, 0, boardInfo.pixelSize.width, boardInfo.pixelSize.height);

            /*
             * Paint grid cells.
             */
            final Cell [][] cells = snapshot.getCells();
            final int cellSize = boardInfo.cellSize;
            for (int y = boardInfo.gridSize.height - 1; y >= 0; y--)
            {
                for (int x = boardInfo.gridSize.width - 1; x >= 0; x--)
                {
                    final Cell cell = cells[x][y];
                    final CellType type = cell.type;

                    final BufferedImage image = getCellImage(type, cell.counter);
                    if (image != null)
                    {
                        g.drawImage(image, null, x * cellSize, y * cellSize);
                    }
                }
            }

            /*
             * Paint sprites.
             */
            for (ISprite sprite : snapshot.getPlayers())
            {
                final int state = sprite.getAnimationState();
                final int frame = sprite.getAnimationFrame();

                final BufferedImage image = 
                    images.getSpriteImage(sprite.getType(), state, frame);

                if (image != null)
                {
                    Point p = new Point(sprite.getPosition());
                    Point offset = images.getSpriteOffset(sprite.getType(), state, frame);
                    p.translate(offset.x, offset.y);
                    g.drawImage(image, null, p.x, p.y);
                }
            }
        }
        g.dispose();
    }

    /**
     * @see IGameListener
     */
    public void onNextFrame(int frame, IBoardSnapshot snapshot)
    {
        updateBoard(snapshot);
        BoardPanel.this.repaint();
    }

    /**
     * Return an image for a given cell at the given counter.
     */
    private BufferedImage getCellImage(CellType cell, int cellCounter)
    {
        BufferedImage [] cellImages = images.getCellImage(cell);
        final int advanceRate = images.getCellAdvanceCounter(cell);

        if (cellImages == null || cellImages.length == 0)
        {
            return null;
        }

        final int frame = cellCounter / advanceRate;
        return cellImages[frame % cellImages.length];
    }

    /*
     * 
     */
    @Override
    public void paint(Graphics g)
    {
        synchronized (exclusiveLock)
        {
            final Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(background, null, 0, 0);
        }
    }

    /*
     * 
     */
    @Override
    public void update(Graphics g)
    {
        // Ignore updates, we redraw everything.
    }

    /*
     * 
     */
    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(boardInfo.pixelSize);
    }
}
