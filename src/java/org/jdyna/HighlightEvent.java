package org.jdyna;

public class HighlightEvent extends GameEvent
{
    /**
     * @see GameEvent#serialVersionUID
     */
    private static final long serialVersionUID = 0x201001021324L;

    private transient IHighlightDetector.FrameRange frameRange;

    /* */
    protected HighlightEvent(IHighlightDetector.FrameRange frameRange)
    {
        super(GameEvent.Type.HIGHLIGHT_DATA);
        this.frameRange = frameRange; 
    }

    public IHighlightDetector.FrameRange getFrameRange() {
        return frameRange;
    }
}
