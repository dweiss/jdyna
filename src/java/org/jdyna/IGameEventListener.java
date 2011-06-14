package org.jdyna;

import java.util.List;

/**
 * Listener for events happening on a {@link Game}.
 */
public interface IGameEventListener
{
    /**
     * This event is sent to each listener after each frame. Event data <b>must not</b>
     * be stored or referenced because it can be reused. The game is blocked for the time
     * of processing of listener callbacks, so keep the processing time low.
     * 
     * @param frame The current frame number.
     * @param events All events that occurred during the frame.
     */
    void onFrame(int frame, List<? extends GameEvent> events);
}
