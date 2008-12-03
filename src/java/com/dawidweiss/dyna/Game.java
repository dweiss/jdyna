package com.dawidweiss.dyna;

import java.awt.Point;
import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Game controller.
 */
public final class Game
{
    /* */
    public final Board board;

    /* */
    public final BoardPanelResources boardResources;

    /* */
    private Player [] players;

    /** Single frame delay, in milliseconds. */
    private int framePeriod;

    /** Timestamp of the last frame's start. */
    private long lastFrameTimestamp;

    /** Game listeners. */
    private final ArrayList<IGameListener> listeners = Lists.newArrayList();

    /**
     * Creates a single game.
     */
    public Game(Board board, BoardPanelResources resources, Player... players)
    {
        this.board = board;
        this.boardResources = resources;
        this.players = players;
    }

    /**
     * Run the game.
     */
    public void run()
    {
        int frame = 0;
        while (true)
        {
            waitForFrame();
            processCells();

            fireNextFrameEvent(frame);
            frame++;
        }
    }

    /**
     * Set the frame rate. Zero means no delays.
     */
    public void setFrameRate(double framesPerSecond)
    {
        if (framesPerSecond == 0)
        {
            framePeriod = 0;
        }
        else
        {
            framePeriod = (int) (1000 / framesPerSecond);
        }
    }

    /*
     * 
     */
    public void addListener(IGameListener listener)
    {
        listeners.add(listener);
    }

    /*
     * 
     */
    public void removeListener(IGameListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * Fire next frame event to listeners.
     */
    private void fireNextFrameEvent(int frame)
    {
        for (IGameListener gl : listeners)
        {
            gl.onNextFrame(frame);
        }
    }

    /**
     * Advance each cell's frame number, if they contain animations of some sort (bombs,
     * explosions).
     */
    private void processCells()
    {
        final Cell [][] cells = board.cells;
        for (int y = board.height - 1; y >= 0; y--)
        {
            for (int x = board.width - 1; x >= 0; x--)
            {
                final Cell cell = cells[x][y];
                final CellType type = cell.type;

                /*
                 * Advance counter frame for cells that use it.
                 */
                cell.counter++;

                /*
                 * Clean up cells that have finished animating.
                 */
                if (ANIMATING_CELLS.contains(type))
                {
                    final int maxFrames = boardResources.cell_infos.get(type).advanceRate
                        * boardResources.cell_images.get(type).length;

                    if (cell.counter == maxFrames)
                    {
                        cells[x][y] = new Cell(CellType.CELL_EMPTY);
                        continue;
                    }
                }
            }
        }
        
        /*
         * Detect and propagate explosions.
         */
        final ArrayList<Point> crates = Lists.newArrayList();
        for (int y = board.height - 1; y >= 0; y--)
        {
            for (int x = board.width - 1; x >= 0; x--)
            {
                final Cell cell = cells[x][y];
                final CellType type = cell.type;

                if (type == CellType.CELL_BOMB && cell.counter == 10)
                {
                    final int range = 3;
                    explode(crates, x, y, range);
                }
            }
        }
        removeCrates(crates);
    }

    /**
     * Remove the crates that have been bombed out.
     */
    private void removeCrates(ArrayList<Point> crates)
    {
        for (Point p : crates)
        {
            board.cells[p.x][p.y] = new Cell(CellType.CELL_CRATE_OUT);
        }
    }

    /**
     * Explode <code>range</code> cells around (x,y), recursively propagating if other
     * bombs are found in the range.
     */
    private void explode(List<Point> crates, int x, int y, int range)
    {
        // Horizontal mode.
        final int xmin = Math.max(0, x - range);
        final int xmax = Math.min(board.width - 1, x + range);
        final int ymin = Math.max(0, y - range);
        final int ymax = Math.min(board.height - 1, y + range);

        // Centerpoint.
        board.cells[x][y] = new Cell(CellType.CELL_BOOM_XY);

        // Left from the centerpoint.
        explode0(crates, range, x - 1, xmin, -1, x, y, true,  CellType.CELL_BOOM_X, CellType.CELL_BOOM_LX);
        explode0(crates, range, x + 1, xmax, +1, x, y, true,  CellType.CELL_BOOM_X, CellType.CELL_BOOM_RX);
        explode0(crates, range, y - 1, ymin, -1, x, y, false, CellType.CELL_BOOM_Y, CellType.CELL_BOOM_TY);
        explode0(crates, range, y + 1, ymax, +1, x, y, false, CellType.CELL_BOOM_Y, CellType.CELL_BOOM_BY);
    }

    /**
     * Helper method for {@link #explode(List, int, int, int)}, propagation
     * of the explosion. 
     */
    private void explode0(
        List<Point> crates, int range,
        int from, int to, int step,
        final int x, final int y,
        boolean horizontal, CellType during, CellType last)
    {
        for (int i = from; i != to + step; i += step)
        {
            final int lx = (horizontal ? i : x);
            final int ly = (horizontal ? y : i);

            /*
             * Note the fall-throughs in switch statements below,
             * they are intentional.
             */
            switch (board.cells[lx][ly].type)
            {
                case CELL_CRATE:
                    crates.add(new Point(lx, ly));
                case CELL_WALL:
                    return;

                case CELL_BOMB:
                    explode(crates, lx, ly, range);
                    break;
            }
            
            board.cells[lx][ly] =
                overlap(board.cells[lx][ly], new Cell(((i == to) ? last : during)));        
        }
    }

    private final static EnumSet<CellType> ANIMATING_CELLS = EnumSet.of(
        CellType.CELL_BOMB, CellType.CELL_BOOM_BY, CellType.CELL_BOOM_TY,
        CellType.CELL_BOOM_Y, CellType.CELL_BOOM_X, CellType.CELL_BOOM_LX,
        CellType.CELL_BOOM_RX, CellType.CELL_BOOM_XY, CellType.CELL_CRATE_OUT);

    private final static EnumSet<CellType> EXPLOSION_CELLS = EnumSet.of(
        CellType.CELL_BOMB, CellType.CELL_BOOM_BY, CellType.CELL_BOOM_TY,
        CellType.CELL_BOOM_Y, CellType.CELL_BOOM_X, CellType.CELL_BOOM_LX,
        CellType.CELL_BOOM_RX, CellType.CELL_BOOM_XY);

    private final static EnumMap<CellType, EnumMap<CellType, CellType>> EXPLOSION_OVERLAPS;
    static
    {
        final CellType [][] pairs = new CellType [][] {
            {CellType.CELL_BOOM_LX, CellType.CELL_BOOM_RX, CellType.CELL_BOOM_X},
            {CellType.CELL_BOOM_LX, CellType.CELL_BOOM_TY, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_LX, CellType.CELL_BOOM_BY, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_LX, CellType.CELL_BOOM_Y, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_LX, CellType.CELL_BOOM_X, CellType.CELL_BOOM_X},
            {CellType.CELL_BOOM_LX, CellType.CELL_BOOM_XY, CellType.CELL_BOOM_XY},
            
            {CellType.CELL_BOOM_RX, CellType.CELL_BOOM_TY, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_RX, CellType.CELL_BOOM_BY, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_RX, CellType.CELL_BOOM_Y, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_RX, CellType.CELL_BOOM_X, CellType.CELL_BOOM_X},
            {CellType.CELL_BOOM_RX, CellType.CELL_BOOM_XY, CellType.CELL_BOOM_XY},

            {CellType.CELL_BOOM_TY, CellType.CELL_BOOM_BY, CellType.CELL_BOOM_Y},
            {CellType.CELL_BOOM_TY, CellType.CELL_BOOM_Y, CellType.CELL_BOOM_Y},
            {CellType.CELL_BOOM_TY, CellType.CELL_BOOM_X, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_TY, CellType.CELL_BOOM_XY, CellType.CELL_BOOM_XY},
            
            {CellType.CELL_BOOM_BY, CellType.CELL_BOOM_Y, CellType.CELL_BOOM_Y},
            {CellType.CELL_BOOM_BY, CellType.CELL_BOOM_X, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_BY, CellType.CELL_BOOM_XY, CellType.CELL_BOOM_XY},            
            
            {CellType.CELL_BOOM_Y, CellType.CELL_BOOM_X, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_Y, CellType.CELL_BOOM_XY, CellType.CELL_BOOM_XY},            

            {CellType.CELL_BOOM_X, CellType.CELL_BOOM_XY, CellType.CELL_BOOM_XY},            
        };

        EXPLOSION_OVERLAPS = Maps.newEnumMap(CellType.class);
        for (CellType [] t : pairs)
        {
            if (!EXPLOSION_OVERLAPS.containsKey(t[0]))
            {
                final EnumMap<CellType, CellType> sub = Maps.newEnumMap(CellType.class);
                EXPLOSION_OVERLAPS.put(t[0], sub);
            }
            if (!EXPLOSION_OVERLAPS.containsKey(t[1]))
            {
                final EnumMap<CellType, CellType> sub = Maps.newEnumMap(CellType.class);
                EXPLOSION_OVERLAPS.put(t[1], sub);
            }

            EXPLOSION_OVERLAPS.get(t[0]).put(t[1], t[2]);
            EXPLOSION_OVERLAPS.get(t[1]).put(t[0], t[2]);
        }
    }

    /**
     * Overlap explosion images.
     */
    private static Cell overlap(Cell c1, Cell explosion)
    {
        if (!EXPLOSION_CELLS.contains(c1.type))
        {
            return explosion;
        }

        if (c1.type == explosion.type)
        {
            return c1;
        }

        return new Cell(EXPLOSION_OVERLAPS.get(c1.type).get(explosion.type));
    }

    /**
     * Passive wait for the next frame.
     */
    private void waitForFrame()
    {
        try
        {
            if (lastFrameTimestamp > 0)
            {
                final long nextFrameStart = lastFrameTimestamp + framePeriod;
                long now;
                while ((now = System.currentTimeMillis()) < nextFrameStart)
                {
                    Thread.sleep(nextFrameStart - now);
                }
                this.lastFrameTimestamp = nextFrameStart;
            }
            else
            {
                lastFrameTimestamp = System.currentTimeMillis();
            }
        }
        catch (InterruptedException e)
        {
            // Exit immediately.
        }
    }
}
