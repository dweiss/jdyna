package com.kaluza.mikolajczyk.dyna.dtos;

import java.awt.Point;

import org.jdyna.Globals;

import com.kaluza.mikolajczyk.dyna.GameStateCollector;

/**
 * DTO for a bomb object. Used in {@link GameStateCollector}
 */
public class BombTO
{
    private int range;
    
    private Point position;
    
    private int creationTime;

    private int lastTickTime;
    
    public Point getPosition()
    {
        return position;
    }

    public BombTO(PlayerTO owner, int frameNumber)
    {
        this.position = new Point(owner.getPosition().x, owner.getPosition().y);
        this.range = owner.getBombRange();
        this.creationTime = this.lastTickTime = frameNumber;
    }
    
    public BombTO(int x, int y)
    {
        this.position = new Point(x,y);
        //assume that the bomb is from a player with some bonuses...
        this.range = Globals.DEFAULT_BOMB_RANGE * 2; 
    }
    
    public int getCreationTime()
    {
        return creationTime;
    }
    
    public int getRange()
    {
        return range;
    }
    
    /**
     * Tic the bomb's timer.
     * 
     * @param frameNumber
     */
    public void ticTac(int frameNumber)
    {
        lastTickTime = frameNumber;
    }
    
    public int getTimeToExplosion()
    {
        int time = Globals.DEFAULT_FUSE_FRAMES - lastTickTime + creationTime;
        time = time < 0 ? 0 : time;
        return time;
    }

    public boolean hasPosition(int x, int y)
    {
        return this.position.x == x && this.position.y == y;
    }
    
    @Override
    public int hashCode()
    {
        return position.hashCode();
    }
    
    
    @Override
    public boolean equals(Object arg0)
    {
        if(arg0 == null || arg0.getClass() != BombTO.class)
        {
            return false;
        }
        
        BombTO bomb = (BombTO) arg0;
        
        if(this.position.equals(bomb.getPosition()))
        {
            return true;
        }
        
        return false;
        
    }
}
