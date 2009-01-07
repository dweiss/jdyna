package com.dawidweiss.dyna.view.swing;

import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameStartEvent;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerSprite;
import com.dawidweiss.dyna.Player;
import com.dawidweiss.dyna.view.resources.Images;
import com.google.common.collect.Maps;

/**
 * Swing's {@link JPanel} that displays the state and changes on the playfield during the
 * game.
 */
@SuppressWarnings("serial")
public final class BoardPanel extends JPanel implements IGameEventListener
{
    /**
     * Exclusive lock so that drawing and updating does not take place at the same time.
     */
    private final Object exclusiveLock = new Object();

    /**
     * The latest board's background state.
     */
    private BufferedImage background;

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
     * propagated from the controller because the controller does not know how many frames
     * it would take to display such sequence.
     * <p>
     * This map stores the index of a given player and its 'dead' status frame count. If
     * the frame count equals -1, the player is dead.
     */
    private HashMap<Integer, Integer> dyingPlayers = Maps.newHashMap();

    /**
     * Label font for players.
     */
    private final Font labelFont;

    /**
     * Should player labels be painted or not?
     */
    private boolean paintPlayerLabels = Globals.SWING_VIEW_PAINT_PLAYER_LABELS;
    
    /**
     * Magnification level. Zoom level are fixed to doubling because we're really
     * pixel-oriented people.
     */
    private Magnification magnification;

    /**
     * Hardware graphics configuration.
     */
    private final GraphicsConfiguration conf;

    /**
     * Affine transformation scaling to {@link #magnification} or <code>null</code>
     * if 1:1 rendering is to be used.
     */
    private BufferedImageOp magnificationOp;

    /**
     * Global frame counter, used for flickering.
     */
    private int globalFrameCounter;

    /**
     * Rendering hints that disable bilinear or bicubic interpolation and in general
     * go for "pixelized" style. 
     */
    private final static RenderingHints hints;
    static
    {
        hints = new RenderingHints(Maps.<Key, Object>newHashMap()); 
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }

    /**
     * 
     */
    public BoardPanel(Images images, GraphicsConfiguration conf)
    {
        this.images = images.createCompatible(conf);
        this.conf = conf;

        InputStream is = null;
        try
        {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "fonts/5px2bus.ttf");
            this.labelFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(5f);
            is.close();
        }
        catch (Exception e)
        {
            IOUtils.closeQuietly(is);
            throw new RuntimeException(e);
        }

        /*
         * TODO: correct buffering strategy. Reimplementation based on AWT's Canvas turned
         * out to be much slower under Linux. I don't see any sense in this...
         */
        this.setDoubleBuffered(false);
        this.setMagnification(Globals.DEFAULT_VIEW_MAGNIFICATION);
    }

    /**
     * Update {@link #background} because the board changed.
     */
    public void updateBoard(GameStateEvent gameState)
    {
        final Graphics2D g = background.createGraphics();
        g.setRenderingHints(hints);

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
            final Cell [][] cells = gameState.getCells();
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
            final List<? extends IPlayerSprite> players = gameState.getPlayers();
            for (int playerIndex = 0; playerIndex < players.size(); playerIndex++)
            {
                final IPlayerSprite player = players.get(playerIndex);
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

                    final int max = images.getMaxSpriteImageFrame(player.getType(),
                        dyingState);
                    if (f < max)
                    {
                        state = dyingState;
                        frame = f;
                        dyingPlayers.put(playerIndex, f + 1);
                    }
                }

                /*
                 * Paint the player. Players should immediately advance to the next
                 * animation frame if they move. However, if they keep moving, we need
                 * to cycle through all the animation frames. The logic below tries
                 * to express this.
                 */
                int animFrame = 0;
                if (frame > 0)
                {
                    animFrame = (frame - 1) / images.getSpriteAdvanceRate(player.getType());

                    if (state != dyingState && state != deadState)
                    {
                        animFrame++;
                    }
                }

                BufferedImage image = images.getSpriteImage(player.getType(), state, animFrame);
                
                if (image != null)
                {
                    final Composite c = g.getComposite();
                    if (player.isImmortal())
                    {
                        /*
                         * Flicker immortal players.
                         */
                        final float alpha = (globalFrameCounter & 4) != 0 ? 0.25f : 0.8f;
                        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    }

                    final Point p = new Point(player.getPosition());
                    final Point offset = images.getSpriteOffset(player.getType(), state,
                        frame);
                    final Point label = new Point(p);

                    p.translate(offset.x, offset.y);
                    g.drawImage(image, null, p.x, p.y);

                    if (paintPlayerLabels)
                    {
                        final String playerLabel = player.getName().toUpperCase();
                        g.setFont(labelFont);
                        final FontMetrics fm = g.getFontMetrics();
                        label.translate(0, -image.getHeight() / 2);
                        label.translate(-fm.stringWidth(player.getName()) / 2, -(fm
                            .getDescent() + 2));
                        g.setColor(Color.YELLOW);
                        g.drawString(playerLabel, label.x, label.y);
                    }
                    
                    g.setComposite(c);
                }
            }
        }
        g.dispose();
    }

    /**
     * @see IGameEventListener
     */
    public void onFrame(int frame, List<GameEvent> events)
    {
        for (GameEvent e : events)
        {
            if (e.type == GameEvent.Type.GAME_START)
            {
                initializeBoard((GameStartEvent) e);
                fireSizeChanged();

                globalFrameCounter = 0;
            }

            if (e.type == GameEvent.Type.GAME_STATE)
            {
                updateBoard((GameStateEvent) e);                
                globalFrameCounter++;
            }
        }
        BoardPanel.this.repaint();
    }

    /**
     * Initialize board data.
     */
    private void initializeBoard(GameStartEvent e)
    {
        this.boardInfo = e.getBoardInfo();

        synchronized (exclusiveLock)
        {
            /*
             * Create the background image for the board.
             */
            final Dimension size = getPreferredSize();
            background = conf.createCompatibleImage(size.width, size.height);
    
            final Graphics2D g = (Graphics2D) background.getGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, size.width, size.height);
            g.dispose();
        }
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
        final Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHints(hints);

        synchronized (exclusiveLock)
        {
            if (this.background == null)
            {
                final Rectangle r = g.getClipBounds();
                g.setColor(Color.BLACK);
                g.fillRect(r.x, r.y, r.width, r.height);
            }
            else
            {
                g2d.drawImage(background, magnificationOp, 0, 0);
            }
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
        synchronized (exclusiveLock)
        {
            if (this.boardInfo == null)
            {
                return new Dimension(300, 200);
            }

            final Dimension size = new Dimension(boardInfo.pixelSize);
            size.width *= magnification.scaleFactor;
            size.height *= magnification.scaleFactor;
            return size;
        }
    }
    
    /**
     * Set board pixel doubling to the desired value.
     */
    public void setMagnification(Magnification magnification)
    {
        synchronized (exclusiveLock)
        {
            if (ObjectUtils.equals(this.magnification, magnification)) return;
            this.magnification = magnification;

            magnificationOp = null;
            if (magnification != Magnification.TIMES_1)
            {
                final double scale = magnification.scaleFactor;
                
                magnificationOp = new AffineTransformOp(
                    AffineTransform.getScaleInstance(scale, scale),
                    AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            }
        }
        
        fireSizeChanged();
    }

    /*
     * Fire size changed event.
     */
    private void fireSizeChanged()
    {
        final Runnable r = new Runnable() {
            public void run()
            {
                BoardPanel.super.setSize(getPreferredSize());
            }
        };

        if (SwingUtilities.isEventDispatchThread())
        {
            r.run();
        }
        else
        {
            SwingUtilities.invokeLater(r);
        }
    }
}
