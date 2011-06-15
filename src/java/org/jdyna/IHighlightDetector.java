package org.jdyna;

public interface IHighlightDetector extends IGameEventListener
{
    class FrameRange {
        public int beginFrame;
        public int endFrame;

        public FrameRange(int beginFrame, int endFrame) {
            this.beginFrame = beginFrame;
            this.endFrame = endFrame;
        }
    }

    boolean isHighlightDetected();
    FrameRange getHighlightFrameRange();
}
