package com.dawidweiss.dyna.serialization;

import java.io.Serializable;
import java.util.List;

import com.dawidweiss.dyna.GameEvent;

/**
 * Frame data.
 */
@SuppressWarnings("serial")
public class FrameData implements Serializable
{
    public List<? extends GameEvent> events;
    public int frame;
    
    protected FrameData()
    {
        // Do nothing.
    }

    public FrameData(int frame, List<? extends GameEvent> events)
    {
        this.frame = frame;
        this.events = events;
    }
}
