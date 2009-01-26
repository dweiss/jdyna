package org.jdyna.network.sockets;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.*;

/**
 * An adapter for {@link IGameEventListener} that internally queues external events and
 * returns immediately from {@link IGameEventListener#onFrame(int, java.util.List)}
 * method. Some of the queued events are dropped if the queue grows at a faster rate than
 * the processing thread is able to digest.
 */
public final class AsyncPlayerController implements IGameEventListener, IPlayerController2
{
    private final static Logger logger = LoggerFactory
        .getLogger(AsyncPlayerController.class);

    private IPlayerController delegateController;
    private IGameEventListener delegateGameListener;

    /**
     * Monitor around updates to {@link #lastState}.
     */
    private final Object stateUpdateMonitor = new Object();
    
    /**
     * Last state saved by the player's controller.
     */
    private ControllerState lastState;

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
        public GameEvent [] events;

        public FrameData(int frame, GameEvent [] events)
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
    public AsyncPlayerController(IPlayerController controller)
    {
        this.delegateController = controller;

        if (controller instanceof IGameEventListener)
        {
            this.delegateGameListener = (IGameEventListener) controller;
        }

        frameDataConsumer = new Thread()
        {
            public void run()
            {
                dispatchQueueEvents();
            }
        };
        frameDataConsumer.setDaemon(true);
        frameDataConsumer.setName("QueuePump");
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
                 * Wait for the first queued frame, once received, clear the queue as
                 * quickly, as possible.
                 */
                int frames = 0;
                int droppedEvents = 0;
                fd = pendingEvents.takeFirst();
                do
                {
                    final GameEvent [] events = fd.events;
                    int to = events.length;
                    if (frames > 0)
                    {
                        /*
                         * We're clearing the queue (dropping events we don't care about) by
                         * rewriting the input list and clearing unneeded entries.
                         */
                        final int size = events.length;
                        to = 0;
                        for (int from = 0; from < size; from++)
                        {
                            final GameEvent event = events[from];
                            if (to != from) events[to] = events[from];

                            if (DROP_EVENT_TYPES.contains(event.type)) droppedEvents++;
                            else to++;
                        }
                    }

                    if (delegateGameListener != null && to > 0)
                    {
                        delegateGameListener.onFrame(fd.frame, Arrays.asList(events)
                            .subList(0, to));
                    }

                    frames++;
                }
                while (null != (fd = pendingEvents.pollFirst()));

                /*
                 * We save a snapshot of the last state, valid for exactly one future
                 * frame on the controller. This is a workaround for slow clients.
                 */
                synchronized (stateUpdateMonitor)
                {
                    if (delegateController instanceof IPlayerController2)
                    {
                        this.lastState = ((IPlayerController2) delegateController).getState();
                    }
                    else
                    {
                        this.lastState = new ControllerState(delegateController.getCurrent(),
                            delegateController.dropsBomb(), 1);
                    }
                }

                if (frames > 1 && logger.isWarnEnabled())
                {
                    logger.warn("Dropped frames: " + (frames - 1) + ", dropped events: "
                        + droppedEvents);
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
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        pendingEvents.addLast(new FrameData(frame, events.toArray(new GameEvent [events
            .size()])));
    }

    /**
     * Return the last state saved by the player's controller.
     */
    @Override
    public ControllerState getState()
    {
        synchronized (stateUpdateMonitor)
        {
            final ControllerState state = lastState;
            lastState = null;
            return state;
        }
    }
}
