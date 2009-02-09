package com.kaluza.mikolajczyk.dyna.dtos;

import java.awt.Point;

import com.kaluza.mikolajczyk.dyna.GameStateCollector;

/**
 * DTO for a bomb bonus. Used in {@link GameStateCollector}
 */
@SuppressWarnings("serial")
public class BonusTO extends Point
{
    public enum BonusType { BOMB_RANGE, BOMBS_AMMOUNT};
    
    private BonusType bonusType;
    
    /**
     * On the basis of heartbeat the bonuses are removed from the list of bonuses. 
     * When iterating over whole board every bonus' heartbeat is updated. If it's not - it has been taken or exploded.
     */
    private int heartBeat;
    
    public BonusTO(Point position, BonusType type, int heartBeat)
    {
        super(position);
        this.bonusType = type;
        this.heartBeat = heartBeat;
    }
    
    /**
     * Update the heartbeat of the bonus.
     */
    public void update(int heartBeat)
    {
        this.heartBeat = heartBeat;
    }
    
    public boolean hasPosition(int x, int y)
    {
        return super.x == x && super.y == y;
    }
    
    public int getHeartBeat()
    {
        return heartBeat;
    }
    
    public Point getPosition()
    {
        return super.getLocation();
    }

    public BonusType getBonusType()
    {
        return bonusType;
    }
    
    @Override
    public int hashCode()
    {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object arg0)
    {
        if(arg0 == null || arg0.getClass() != BonusTO.class)
        {
            return false;
        }
        
        BonusTO bonus = (BonusTO) arg0;
        
        if(super.getLocation().equals(bonus.getPosition()))
        {
            return true;
        }
        
        return false;
    }
}
