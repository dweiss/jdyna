package com.dawidweiss.dyna;

import com.dawidweiss.dyna.view.IBoardSnapshot;

/**
 * Events from {@link Game}.
 */
public interface IGameListener
{
    /**
     * This event is sent to each listener after each frame. The board snapshot
     * contains static and dynamic data on the playfield.
     * <p>
     * Snapshot data must not be stored because it can be reused. 
     */
    void onNextFrame(int frame, IBoardSnapshot snapshot);
}
