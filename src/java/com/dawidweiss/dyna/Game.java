package com.dawidweiss.dyna;

import java.awt.Point;
import java.util.ArrayList;
import java.util.EnumSet;

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
     * Run the game.
     */
    public void run()
    {
        setupPlayers();

        /*
         * Main game loop.
         */
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
     * Move players according to their controller signals.
     */
    private void processPlayers()
    {
        for (int i = 0; i < players.length; i++)
        {
            final PlayerInfo pi = playerInfos[i];
            final IController c = players[i].controller;

            final EnumSet<IController.Direction> signals = c.getCurrent();

            int dx = 0;
            int dy = 0;
            if (signals.contains(IController.Direction.LEFT)) dx -= pi.speed.x;
            if (signals.contains(IController.Direction.RIGHT)) dx += pi.speed.x;
            if (signals.contains(IController.Direction.UP)) dy -= pi.speed.y;
            if (signals.contains(IController.Direction.DOWN)) dy += pi.speed.y;

            pi.controllerState(signals);
            pi.location.translate(dx, dy);
        }
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
        final int GRID_SIZE = boardData.gridSize;
        for (int i = 0; i < players.length; i++)
        {
            pi[i] = new PlayerInfo(images[i % images.length]);
            pi[i].location.x = defaults[i].x * GRID_SIZE + (GRID_SIZE / 2);
            pi[i].location.y = defaults[i].y * GRID_SIZE + (GRID_SIZE / 2);

            board.sprites.add(pi[i]);
        }

        this.playerInfos = pi;
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

                if (type == CellType.CELL_BOMB && cell.counter == 10)
                {
                    final int range = 3;
                    BoardUtilities.explode(board, crates, x, y, range);
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
