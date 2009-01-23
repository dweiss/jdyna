package org.jdyna.network.sockets;

import java.util.List;

import org.jdyna.network.sockets.packets.FrameData;

import com.dawidweiss.dyna.*;
import com.google.common.collect.Lists;

/**
 * 
 */
final class GameContext
{
    private final GameHandle handle;
    private final Game game;
    private final List<IFrameDataListener> listeners = Lists.newArrayList();

    private GameThread thread;

    private final IGameEventListener frameDataBroadcaster = new IGameEventListener() { 
        public void onFrame(int frame, List<? extends GameEvent> events)
        {
            final FrameData fd = new FrameData(frame, events);

            for (IFrameDataListener fdl : listeners)
            {
                fdl.onFrame(fd);
            }
        }
    };

    /*
     * 
     */
    public GameContext(GameHandle handle, Game game)
    {
        this.handle = handle;
        this.game = game;
    }

    public GameHandle getHandle()
    {
        return handle;
    }

    /*
     * 
     */
    public synchronized void startGame()
    {
        if (this.thread != null)
        {
            throw new IllegalStateException("Already started.");
        }

        this.game.addListener(frameDataBroadcaster);
        this.thread = new GameThread(this);
        this.thread.start();
    }

    final Game getGame()
    {
        return game;
    }

    public void addFrameDataListener(IFrameDataListener l)
    {
        this.listeners.add(l);
    }
    
    /*
     * 
     */
    public synchronized void dispose()
    {
        thread.interrupt();
    }
}
