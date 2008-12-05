package com.dawidweiss.dyna.view.swing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import javax.swing.JPanel;

import com.dawidweiss.dyna.*;

/**
 * Swing's {@link JPanel} that displays the state and changes on the playfield during the
 * game.
 */
@SuppressWarnings("serial")
public final class BoardPanel extends JPanel
{
    /* */
    private final Logger logger = Logger.getAnonymousLogger();

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
     * Update board state.
     */
    private IGameListener gameListener = new IGameListener()
    {
        public void onNextFrame(int frame, IBoardSnapshot snapshot)
        {
            updateBoard(snapshot);
            BoardPanel.this.repaint();
        }
    };

    /**
     * 
     */
    public BoardPanel(BoardInfo boardInfo, GraphicsConfiguration conf)
    {
        this.boardInfo = boardInfo;

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

        final BufferedImage backgroundImage = resources.cell_images
            .get(CellType.CELL_EMPTY)[0];
        final Color backgroundColor = new Color(backgroundImage.getRGB(0, 0));

        synchronized (exclusiveLock)
        {
            final Cell [][] cells = board.cells;
            for (int y = board.height - 1; y >= 0; y--)
            {
                for (int x = board.width - 1; x >= 0; x--)
                {
                    final Cell cell = cells[x][y];
                    final CellType type = cell.type;

                    final BufferedImage [] frames = resources.cell_images.get(type);
                    if (frames == null)
                    {
                        logger.warning("There is no image for this cell: " + cell.type);
                        continue;
                    }

                    final int advanceRate = resources.cell_infos.get(type).advanceRate;
                    final int frame = (cell.counter / advanceRate) % frames.length;

                    /*
                     * We could fill entire background with a solid color at one go, but
                     * then we wouldn't have the optimization possibility of redrawing
                     * only those cells that have changed.
                     */
                    g.setColor(backgroundColor);
                    g.fillRect(x * GRID_SIZE, y * GRID_SIZE, GRID_SIZE, GRID_SIZE);
                    g.drawImage(frames[frame % frames.length], null, x * GRID_SIZE, y
                        * GRID_SIZE);
                }
            }

            /*
             * Paint sprites.
             */
            for (ISprite sprite : board.sprites)
            {
                sprite.paint(g);
            }
        }
        g.dispose();
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
        final int gs = resources.gridSize;
        return new Dimension(board.width * gs, board.height * gs);
    }
}
