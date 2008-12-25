package com.dawidweiss.dyna;

/**
 * A timer facilitating waiting for proper frame intervals.
 */
public final class GameTimer
{
    /** Single frame delay, in milliseconds. */
    private int framePeriod;

    /** Timestamp of the last frame's start. */
    private long lastFrameTimestamp;

    /*
     * 
     */
    public GameTimer(double framesPerSecond)
    {
        setFrameRate(framesPerSecond);
    }
    
    /**
     * Passive wait for the next frame. May be slightly inaccurate if 
     * {@link Thread#sleep(long)} is not precise.
     */
    public void waitForFrame()
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

    /**
     * Set the frame rate. Zero means no delays.
     */
    public void setFrameRate(double framesPerSecond)
    {
        framePeriod = (framesPerSecond == 0 ? 0 : (int) (1000 / framesPerSecond));
    }
}
