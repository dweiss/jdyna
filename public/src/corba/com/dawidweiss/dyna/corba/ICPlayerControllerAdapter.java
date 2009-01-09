package com.dawidweiss.dyna.corba;

import java.util.EnumSet;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.*;
import com.dawidweiss.dyna.corba.bindings.*;

/**
 * An adapter adapting {@link IPlayerController} for use with Corba's
 * {@link ICPlayerController}. The frames received from a remote server are queued
 * internally. Another thread is spawned to serve these frames to the local controller and
 * pass the controller state back to the Corba server. If processing takes too long, some
 * frames <b>may be dropped</b>.
 */
public final class ICPlayerControllerAdapter extends ICPlayerControllerPOA
{
    private final static Logger logger = LoggerFactory
        .getLogger(ICPlayerControllerAdapter.class);

    private IPlayerController delegateController;
    private IGameEventListener delegateGameListener;

    private ICControllerCallback callback;
    private ControllerState last;

    private CPlayer [] players;
    private CBoardInfo boardInfo;

    /**
     * Drop the following event types from excessive frames. 
     */
    private final static EnumSet<GameEvent.Type> DROP_EVENT_TYPES = EnumSet.of(
        GameEvent.Type.GAME_STATE, GameEvent.Type.SOUND_EFFECT); 

    /**
     * Queue frame data. We rely on event data content to be immutable.
     */
    private static class FrameData
    {
        public final int frame;
        public CGameEvent [] events;

        public FrameData(int frame, CGameEvent [] events)
        {
            this.frame = frame;
            this.events = events;
        }
    }

    /**
     * An unbounded array of pending frame data.
     */
    private final BlockingDeque<FrameData> pendingEvents = new LinkedBlockingDeque<FrameData>();

    /**
     * Frame data consumer thread.
     */
    private Thread frameDataConsumer;

    /*
     * 
     */
    public ICPlayerControllerAdapter(IPlayerController controller)
    {
        this.delegateController = controller;

        if (controller instanceof IGameEventListener)
        {
            this.delegateGameListener = (IGameEventListener) controller;
        }
    }

    /*
     * 
     */
    @Override
    public void onControllerSetup(ICControllerCallback callback)
    {
        this.callback = callback;
    }

    /*
     * 
     */
    @Override
    public void onStart(CBoardInfo bInfo, CPlayer [] p)
    {
        this.boardInfo = bInfo;
        this.players = p;

        frameDataConsumer = new Thread()
        {
            public void run()
            {
                dispatchQueueEvents();
            }
        };
        frameDataConsumer.setDaemon(true);
        frameDataConsumer.setName("frame data pump");
        frameDataConsumer.start();
    }

    /**
     * Dispatch events from {@link #pendingEvents} queue.
     */
    private void dispatchQueueEvents()
    {
        try
        {
            final Thread t = Thread.currentThread();

            FrameData fd;
            while (!t.isInterrupted())
            {
                /*
                 * Wait for the first available queued frame, once received, clear
                 * the queue.
                 */
                int frames = 0;
                int droppedEvents = 0;
                fd = pendingEvents.takeFirst();
                do
                {
                    CGameEvent [] eventsCopy = fd.events;
                    if (frames > 0)
                    {
                        // We're clearing the queue (dropping).
                        for (int i = 0; i < eventsCopy.length; i++)
                        {
                            if (DROP_EVENT_TYPES.contains(
                                Adapters.adapt(eventsCopy[i].discriminator())))
                            {
                                /*
                                 * In case of local JVM (Corba) execution, we need to duplicate 
                                 * the array (shallow) because it is shared among all clients, so 
                                 * clearing the array content would cause problems.
                                 */
                                if (eventsCopy == fd.events)
                                {
                                    eventsCopy = (CGameEvent []) fd.events.clone();
                                }
                                eventsCopy[i] = null;
                                droppedEvents++;
                            }
                        }
                    }

                    if (delegateGameListener != null)
                    {
                        delegateGameListener.onFrame(fd.frame, Adapters.adapt(
                            eventsCopy, boardInfo, players));
                    }

                    frames++;
                }
                while (null != (fd = pendingEvents.pollFirst()));

                if (frames > 1) logger.warn("Dropped frames: " + (frames - 1)
                    + ", dropped events: " + droppedEvents);

                /*
                 * Dispatch controller state, if changed.
                 */
                final ControllerState now = new ControllerState(delegateController);
                if (last == null || !last.equals(now))
                {
                    last = now;
                    callback.update(Adapters.adapt(delegateController));
                }
            }
        }
        catch (InterruptedException e)
        {
            // Ignore and exit.
        }
    }

    /*
     * 
     */
    @Override
    public void onFrame(int frame, CGameEvent [] events)
    {
        pendingEvents.addLast(new FrameData(frame, events));
    }

    /*
     * 
     */
    @Override
    public void onEnd(CGameResult result)
    {
        // Do nothing.
        if (frameDataConsumer != null && frameDataConsumer.isAlive())
        {
            try
            {
                frameDataConsumer.interrupt();
                frameDataConsumer.join();
            }
            catch (InterruptedException e)
            {
                // Shouldn't happen, but if then simply leave the loop.
            }
        }

        frameDataConsumer = null;
        pendingEvents.clear();
        last = null;
    }
}
