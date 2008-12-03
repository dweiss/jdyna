package com.dawidweiss.dyna;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import javax.swing.JPanel;

/**
 * Swing's {link JPanel} that displays the state and changes to a {@link Board}.
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
     * Static image resources.
     */
    private BoardPanelResources resources;

    /**
     * The board we're painting.
     */
    private Board board;

    /**
     * The latest board's background state.
     */
    private final BufferedImage background;

    /**
     * 
     */
    public BoardPanel(BoardPanelResources resources, Board board)
    {
        this.resources = resources;
        this.board = board;

        /*
         * Use double buffering, although we will draw off-screen anyway.
         */
        this.setDoubleBuffered(false);

        /*
         * Initial board update.
         */
        final Dimension size = getPreferredSize();
        background = resources.conf.createCompatibleImage(size.width, size.height);

        updateBoard();
    }

    /**
     * Update {@link #background} because the board changed.
     */
    public void updateBoard()
    {
        synchronized (exclusiveLock)
        {
            /*
             * TODO: Possible optimization, update only those cells that changed from the
             * previous state. This is easy, keep track of changes in board.cells.
             */
            final int GRID_SIZE = resources.cell_info.GRID_SIZE;
            final Graphics2D g = background.createGraphics();
            final short [] cells = board.cells;
            for (int offset = cells.length - 1; offset >= 0; offset--)
            {
                final int y = offset / board.width;
                final int x = offset - (y * board.width);

                final short cell = board.cells[offset];
                final byte code = (byte) (cell & 0x00ff);
                final int frame = (cell >>> 8);

                final BufferedImage [] frames = resources.cell_images.get(Cell
                    .valueOf(code));

                if (frames == null)
                {
                    logger.warning("There is no image for this cell: " + code);
                    continue;
                }

                if (frames.length < frame)
                {
                    logger.warning("There is no frame " + frame + " for this cell: "
                        + Cell.valueOf(code));
                    continue;
                }

                g.drawImage(frames[frame], null, x * GRID_SIZE, y * GRID_SIZE);
            }
            g.dispose();
        }
    }

    /*
     * 
     */
    @Override
    public void paint(Graphics g)
    {
        synchronized (exclusiveLock)
        {
            ((Graphics2D) g).drawImage(background, null, 0, 0);
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
        final int gs = resources.cell_info.GRID_SIZE;
        return new Dimension(board.width * gs, board.height * gs);
    }
}
