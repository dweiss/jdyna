package org.jdyna.serialization;

import java.io.Serializable;
import java.util.List;

import org.jdyna.GameEvent;


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
