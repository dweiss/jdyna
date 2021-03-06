package org.jdyna.network.sockets;

import java.util.*;

import org.jdyna.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * A multiplexer of events from one {@link IGameEventListener} source to multiple clients.
 */
public final class GameEventListenerMultiplexer implements IGameEventListener, IGameEventListenerHolder
{
    private final static Logger logger = LoggerFactory
        .getLogger(GameEventListenerMultiplexer.class);

    /** Game listeners. */
    private final ArrayList<IGameEventListener> listeners = Lists.newArrayList();

    /**
     * 
     */
    public synchronized void addListener(IGameEventListener l)
    {
        listeners.add(l);
    }

    /*
     * 
     */
    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        synchronized (this)
        {
            for (IGameEventListener l : listeners)
            {
                try
                {
                    l.onFrame(frame, events);
                }
                catch (Throwable t)
                {
                    logger.warn("Listener dispatch error: " + t.getMessage(), t);
                }
            }
        }
    }

    /*
     * 
     */
    @Override
    public synchronized void removeListener(IGameEventListener l)
    {
        listeners.remove(l);
    }

    /*
     * 
     */
    @Override
    public Collection<IGameEventListener> getListeners()
    {
        return Collections.unmodifiableList(listeners);
    }
}
