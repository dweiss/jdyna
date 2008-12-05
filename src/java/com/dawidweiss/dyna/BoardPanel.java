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
    private BoardData resources;

    /**
     * The board we're painting.
     */
    private Board board;

    /**
     * The latest board's background state.
     */
    private final BufferedImage background;
    
    /**
     * Last frame received by {@link #gameListener}.
     */
    @SuppressWarnings("unused")
    private volatile int frame;

    /**
     * Update board state.
     */
    private IGameListener gameListener = new IGameListener()
    {
        public void onNextFrame(int frame)
        {
            updateBoard();

            BoardPanel.this.frame = frame;
            BoardPanel.this.repaint();
        }
    };

    /**
     * 
     */
    public BoardPanel(BoardData resources, Game game)
    {
        this.resources = resources;
        this.board = game.board;

        /*
         * Use double buffering, although we will draw off-screen anyway.
         */
        this.setDoubleBuffered(false);

        /*
         * Initial board update.
         */
        final Dimension size = getPreferredSize();
        background = resources.conf.createCompatibleImage(size.width, size.height);

        /*
         * Update board, attach a listener.
         */
        updateBoard();
        game.addListener(gameListener);
    }

    /**
     * Update {@link #background} because the board changed.
     */
    public void updateBoard()
    {
        final Graphics2D g = background.createGraphics();
        
        final BufferedImage backgroundImage = resources.cell_images.get(CellType.CELL_EMPTY)[0];
        final Color backgroundColor = new Color(backgroundImage.getRGB(0, 0));

        synchronized (exclusiveLock)
        {
            /*
             * TODO: Possible optimization, update only those cells that changed from the
             * previous state? If so, we would have to move drawing sprites
             * to either paint() or a separate buffer.
             */
            final int GRID_SIZE = resources.gridSize;
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
                     * then we wouldn't have the optimization possibility of redrawing only
                     * those cells that have changed.
                     */
                    g.setColor(backgroundColor);
                    g.fillRect(x * GRID_SIZE, y * GRID_SIZE, GRID_SIZE, GRID_SIZE);
                    g.drawImage(frames[frame % frames.length], null, x * GRID_SIZE, y * GRID_SIZE);
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
