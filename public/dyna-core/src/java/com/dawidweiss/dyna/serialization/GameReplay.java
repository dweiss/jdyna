package com.dawidweiss.dyna.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameTimer;
import com.dawidweiss.dyna.IGameEventListener;
import com.google.common.collect.Lists;

/**
 * Replay all events from a previously saved game.
 * 
 * @see GameWriter
 */
public final class GameReplay
{
    private final List<IGameEventListener> listeners = Lists.newArrayList();

    /**
     * Replay a stream of saved events at the given frame rate.
     */
    public void replay(double frameRate, InputStream stream)
        throws IOException
    {
        final GameReader reader = new GameReader(stream);

        try
        {
            final GameTimer timer = new GameTimer(frameRate);
            while (reader.nextFrame())
            {
                // Wait for frame.
                timer.waitForFrame();

                final int frame = reader.getFrame();
                final List<GameEvent> events = reader.getEvents();
                for (IGameEventListener l : listeners)
                {
                    l.onFrame(frame, events);
                }
            }
        }
        catch (InterruptedException e)
        {
            // Do nothing.
        }
        finally
        {
            reader.close();
        }
    }

    /*
     * 
     */
    public void addListener(IGameEventListener l)
    {
        this.listeners.add(l);
    }
}