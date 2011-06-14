package org.jdyna;

/**
 * A marker event for other events dropped from the frame's processing pipeline for some reason.
 */
public final class NoOpEvent extends GameEvent
{
    /**
     * @see GameEvent#serialVersionUID
     */
    private static final long serialVersionUID = 0x200901090955L;
    
    /**
     * The original event that was dropped from processing. May be <code>null</code>. 
     */
    public final GameEvent droppedEvent;

    /*
     *  
     */
    public NoOpEvent(GameEvent droppedEvent)
    {
        super(GameEvent.Type.NO_OP);
        this.droppedEvent = droppedEvent;
    }
}
