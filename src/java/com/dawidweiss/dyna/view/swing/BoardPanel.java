package com.dawidweiss.dyna.view.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.JPanel;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.IGameListener;
import com.dawidweiss.dyna.Player;
import com.dawidweiss.dyna.view.*;
import com.dawidweiss.dyna.view.resources.Images;
import com.google.common.collect.Maps;

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
     * When a player dies we need to display its 'dying' state sequence. This is not
     * propagated from the controller because the controller does not know how many
     * frames it would take to display such sequence.
     * <p>
     * This map stores the index of a given player and its 'dead' status frame count.
     * If the frame count equals -1, the player is dead.
     */
    private HashMap<Integer,Integer> dyingPlayers = Maps.newHashMap();

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
             * Paint players.
             */
            final IPlayerSprite [] players = snapshot.getPlayers();
            for (int playerIndex = 0; playerIndex < players.length; playerIndex++)
            {
                final IPlayerSprite player = players[playerIndex];
                int state = player.getAnimationState();
                int frame = player.getAnimationFrame();

                /*
                 * Special handing of the 'dying' state.
                 */
                final int deadState = Player.State.DEAD.ordinal();
                final int dyingState = Player.State.DYING.ordinal();
                if (state == deadState)
                {
                    Integer f = dyingPlayers.get(playerIndex);
                    f = (f == null ? 0 : f);

                    final int max = images.getMaxSpriteImageFrame(
                        player.getType(), dyingState);
                    if (f < max)
                    {
                        state = dyingState;
                        frame = f;
                        dyingPlayers.put(playerIndex, f + 1);
                    }
                }

                /*
                 * Paint the player. 
                 */
                final BufferedImage image = 
                    images.getSpriteImage(player.getType(), state, frame);

                if (image != null)
                {
                    Point p = new Point(player.getPosition());
                    Point offset = images.getSpriteOffset(player.getType(), state, frame);
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
