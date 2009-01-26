package com.dawidweiss.dyna;

/**
 * An interface allowing hooking into game controller loop of frame processing.
 */
public interface IFrameListener
{
    /**
     * About to start processing <code>frame</code>.
     */
    public void preFrame(int frame);

    /**
     * About to finish processing <code>frame</code>.
     * Dispatched after {@link IGameEventListener#onFrame(int, java.util.List)}.
     */
    public void postFrame(int frame);
}
