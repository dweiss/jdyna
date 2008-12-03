package com.dawidweiss.dyna;

import java.util.ArrayList;

import com.google.common.collect.Lists;

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

    /** Counters for advancing frames (to keep the desired frame rate for slower cells). */
    private final int [] counters;

    /**
     * Creates a single game.
     */
    public Game(Board board, BoardPanelResources resources, Player... players)
    {
        this.board = board;
        this.boardResources = resources;
        this.players = players;
        this.counters = new int [board.cells.length];
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
     * Advance each cell's frame number, if they contain animations of some sort
     * (bombs, explosions).
     */
    private void processCells()
    {
        final short [] cells = board.cells;
        for (int offset = cells.length - 1; offset >= 0; offset--)
        {
            final int y = offset / board.width;
            final int x = offset - (y * board.width);

            final short code = cells[offset];
            final Cell cell = Cell.valueOf((byte) (code & 0x00ff));
            final int frame = (code >>> 8);

            final int advanceRate = boardResources.cell_infos.get(cell).advanceRate;
            final int frames = boardResources.cell_images.get(cell).length;
            if (frames > 1 && (counters[offset]++ % advanceRate) == 0)
            {
                final int nextFrame = (frame + 1) % frames;
                cells[offset] = (short) ((nextFrame << 8) | code & 0x00ff);
            }
        }

        // TODO: advance cell frame counters, perform actions on certain cells.
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
