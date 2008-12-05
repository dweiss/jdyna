package com.dawidweiss.dyna;

import static java.lang.Math.*;
import java.awt.Point;
import java.util.ArrayList;

import com.dawidweiss.dyna.IController.Direction;
import com.google.common.collect.Lists;

/**
 * Game controller. The controller and <b>all the objects involved in the game</b> are
 * single-threaded and should be accessed from within the game loop only.
 */
public final class Game
{
    /* */
    public final Board board;

    /* */
    public final BoardData boardData;

    /* */
    private Player [] players;

    /**
     * Information about player positions and other attributes.
     */
    private PlayerInfo [] playerInfos;

    /** Single frame delay, in milliseconds. */
    private int framePeriod;

    /** Timestamp of the last frame's start. */
    private long lastFrameTimestamp;

    /** Game listeners. */
    private final ArrayList<IGameListener> listeners = Lists.newArrayList();

    /**
     * Creates a single game.
     */
    public Game(Board board, BoardData resources, Player... players)
    {
        this.board = board;
        this.boardData = resources;
        this.players = players;
    }

    /**
     * Starts the main game loop and runs the whole thing.
     */
    public void run()
    {
        setupPlayers();

        int frame = 0;
        while (true)
        {
            waitForFrame();
            processCells();
            processPlayers();

            fireNextFrameEvent(frame);
            frame++;
        }
    }

    /**
     * Set the frame rate. Zero means no delays.
     */
    public void setFrameRate(double framesPerSecond)
    {
        framePeriod = (framesPerSecond == 0 ? 0 : (int) (1000 / framesPerSecond));
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
     * Fire "next frame" event to listeners.
     */
    private void fireNextFrameEvent(int frame)
    {
        for (IGameListener gl : listeners)
        {
            gl.onNextFrame(frame);
        }
    }

    /**
     * Move players according to their controller signals.
     */
    private void processPlayers()
    {
        for (int i = 0; i < players.length; i++)
        {
            final PlayerInfo pi = playerInfos[i];
            final IController c = players[i].controller;

            final IController.Direction signal = c.getCurrent();
            pi.controllerState(signal);

            if (signal != null)
            {
                movePlayer(pi, signal);
            }

            if (c.dropsBomb())
            {
                dropBomb(pi);
            }
        }
    }

    /**
     * Drop a bomb at the given location.
     */
    private void dropBomb(PlayerInfo pi)
    {
        final Point xy = BoardUtilities.pixelToGrid(boardData, pi.location);
        if (board.cellAt(xy).type == CellType.CELL_EMPTY)
        {
            board.cells[xy.x][xy.y] = new Cell(CellType.CELL_BOMB);
        }
    }

    /**
     * The movement-constraint code below has been engineered
     * by trial and error by looking at the behavior of the original
     * Dyna Blaster game. The basic logic is that we attempt
     * to figure out the "target" cell towards which the player 
     * is heading and "guide" the player's coordinates towards
     * the target. This way there is a possibility to run on
     * diagonals (crosscut along the edge of a cell).
     */
    private void movePlayer(PlayerInfo pi, Direction signal)
    {
        final Point xy = BoardUtilities.pixelToGrid(boardData, pi.location);
        final Point txy;

        switch (signal)
        {
            case LEFT:
                txy = new Point(xy.x - 1, xy.y);
                break;
            case RIGHT:
                txy = new Point(xy.x + 1, xy.y);
                break;
            case UP:
                txy = new Point(xy.x, xy.y - 1);
                break;
            case DOWN:
                txy = new Point(xy.x, xy.y + 1);
                break;
            default:
                throw new RuntimeException(/* Unreachable. */);
        }

        final Point p = BoardUtilities.gridToPixel(boardData, txy);

        // Relative distance between the target cell and current position.
        final int rx = p.x - pi.location.x;
        final int ry = p.y - pi.location.y;

        // Steps towards the target.
        int dx = (rx < 0 ? -1 : 1) * min(pi.speed.x, abs(rx));
        int dy = (ry < 0 ? -1 : 1) * min(pi.speed.y, abs(ry));

        if (max(abs(rx), abs(ry)) <= boardData.gridSize)
        {
            if (!canWalkOn(pi, txy))
            {
                /*
                 * We try to perform 'easing', that is moving
                 * the player towards the cell from which he or she will
                 * be able to move further.
                 */
                final Point offset = 
                    BoardUtilities.pixelToGridOffset(boardData, pi.location);

                final boolean easingApplied;
                switch (signal)
                {
                    case LEFT:
                        easingApplied = ease(pi, xy, offset.y, 
                            0, 1, -1, 1, Direction.DOWN, 0, -1, -1, -1, Direction.UP);
                        break;

                    case RIGHT:
                        easingApplied = ease(pi, xy, offset.y, 
                            0, 1, 1, 1, Direction.DOWN, 0, -1, 1, -1, Direction.UP);
                        break;

                    case DOWN:
                        easingApplied = ease(pi, xy, offset.x, 
                            1, 0, 1, 1, Direction.RIGHT, -1, 0, -1, 1, Direction.LEFT);
                        break;

                    case UP:
                        easingApplied = ease(pi, xy, offset.x, 
                            1, 0, 1, -1, Direction.RIGHT, -1, 0, -1, -1, Direction.LEFT);
                        break;

                    default:
                        throw new RuntimeException(/* unreachable */);
                }

                if (easingApplied) return;

                /*
                 * We can't step over a cell that has contents,
                 * no easing.
                 */
                dx = 0;
                dy = 0;
            }
        }

        pi.location.translate(dx, dy);
    }

    /**
     * A helper function that tests if we can apply easing in one 
     * of the directions. This is generalized for all the possibilities,
     * so it may be vague a bit.
     */
    private boolean ease(
        PlayerInfo pi, Point xy, int o,
        int x1, int y1, int x2, int y2, Direction d1,
        int x3, int y3, int x4, int y4, Direction d2)
    {
        final int easeMargin = boardData.gridSize / 3;
        
        if (o > boardData.gridSize - easeMargin
            && canWalkOn(pi, new Point(xy.x + x1, xy.y + y1)) 
            && canWalkOn(pi, new Point(xy.x + x2, xy.y + y2)))
        {
            movePlayer(pi, d1);
            return true;
        }

        if (o < easeMargin
            && canWalkOn(pi, new Point(xy.x + x3, xy.y + y3)) 
            && canWalkOn(pi, new Point(xy.x + x4, xy.y + y4)))
        {
            movePlayer(pi, d2);
            return true;
        }
        
        return false;
    }

    /**
     * Returns <code>true</code> if a player can walk on the grid's
     * given coordinates.
     */
    @SuppressWarnings("unused")
    private boolean canWalkOn(PlayerInfo pi, Point txy)
    {
        return board.cellAt(txy).type == CellType.CELL_EMPTY;        
    }

    /**
     * Assign players to their default board positions.
     */
    private void setupPlayers()
    {
        final PlayerInfo [] pi = new PlayerInfo [players.length];
        final Point [] defaults = board.defaultPlayerPositions;
        if (defaults.length < pi.length)
        {
            throw new RuntimeException("The board has fewer positions than players: "
                + defaults.length + " < " + pi.length);
        }

        final PlayerImageData [] images = boardData.player_images;
        for (int i = 0; i < players.length; i++)
        {
            pi[i] = new PlayerInfo(images[i % images.length]);
            pi[i].location.setLocation(
                BoardUtilities.gridToPixel(boardData, defaults[i]));
            board.sprites.add(pi[i]);
        }
    
        this.playerInfos = pi;
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
                if (BoardUtilities.ANIMATING_CELLS.contains(type))
                {
                    final int maxFrames = boardData.cell_infos.get(type).advanceRate
                        * boardData.cell_images.get(type).length;

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

                if (type == CellType.CELL_BOMB && cell.counter == 100)
                {
                    final int range = 3;
                    BoardUtilities.explode(board, crates, x, y, range);
                }
            }
        }

        /*
         * Remove the crates that have been bombed out.
         */
        for (Point p : crates)
        {
            board.cells[p.x][p.y] = new Cell(CellType.CELL_CRATE_OUT);
        }
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
