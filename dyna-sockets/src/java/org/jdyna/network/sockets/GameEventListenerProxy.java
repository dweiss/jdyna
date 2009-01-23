package org.jdyna.network.sockets;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.IGameEventListener;
import com.google.common.collect.Lists;

/**
 * A proxy interface to a remote game.
 */
public class GameEventListenerProxy implements IGameEventListener
{
    private final static Logger logger = LoggerFactory.getLogger(GameEventListenerProxy.class);

    /** Game listeners. */
    private final ArrayList<IGameEventListener> listeners = Lists.newArrayList();
    
    /**
     * 
     */
    public void addListener(IGameEventListener l)
    {
        listeners.add(l);
    }

    /*
     * 
     */
    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
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
