package org.jdyna.network.sockets.packets;

import java.io.Serializable;
import java.util.List;

import com.dawidweiss.dyna.GameEvent;

/**
 * 
 */
public class FrameData implements Serializable
{
    private static final long serialVersionUID = 1L;

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
