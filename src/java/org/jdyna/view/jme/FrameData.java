package org.jdyna.view.jme;

import org.jdyna.GameStateEvent;

public class FrameData
{
    int frame;
    GameStateEvent state;
    
    
    public FrameData(int frame, GameStateEvent state)
    {
        this.frame = frame;
        this.state = state;
    }
    
    public int getFrameCounter() {
        return frame;
    }
    
    public GameStateEvent getStateEvent() {
        return state;
    }
}
