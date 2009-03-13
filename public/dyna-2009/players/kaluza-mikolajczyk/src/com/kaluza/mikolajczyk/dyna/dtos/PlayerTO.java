package com.kaluza.mikolajczyk.dyna.dtos;

import java.awt.Point;

import org.jdyna.Globals;

import com.kaluza.mikolajczyk.dyna.GameStateCollector;
/**
 * DTO for a bomb player. Used in {@link GameStateCollector}
 */
@SuppressWarnings("serial")
public class PlayerTO extends Point
{
    private String name;
    
    private boolean immortal;
    
    private int bombRange;
    
    private int heartBeat;
    
    private int maxBombs;
    
    public PlayerTO(String name, Point pos, int heartBeat, boolean immortal)
    {
        this(name, heartBeat, immortal);
        super.setLocation(pos);
    }
    
    public PlayerTO(String name, int heartBeat, boolean immortal)
    {
        this.name = name;
        this.immortal = immortal;
        this.heartBeat = heartBeat;
        this.bombRange = Globals.DEFAULT_BOMB_RANGE;
        this.maxBombs = Globals.DEFAULT_BOMB_COUNT;
    }
    
    public Point getPosition()
    {
        return super.getLocation();
    }

    public void update(Point position, int heartBeat, boolean immortal)
    {
        super.setLocation(position);
        this.heartBeat = heartBeat;
        this.immortal = immortal;
    }

    public int getBombRange()
    {
        return bombRange;
    }
    
    public int getMaxBombs()
    {
        return maxBombs;
    }
    
    public void incBombRange()
    {
        this.bombRange++;
    }
    
    public void incMaxBombs()
    {
        this.maxBombs++;
    }
    
    public String getName()
    {
        return name;
    }
    
    public int getHeartBeat()
    {
        return heartBeat;
    }
    
    public boolean hasPosition(int x, int y)
    {
        return super.x == x && super.y == y;
    }
    
    public boolean isImmortal()
    {
        return immortal;
    }
    
    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
    
    @Override
    public boolean equals(Object arg0)
    {
        if(arg0 == null || arg0.getClass() != PlayerTO.class)
        {
            return false;
        }
        
        PlayerTO player = (PlayerTO) arg0;
        
        if(this.name.equals(player.getName()))
        {
            return true;
        }
        
        return false;
        
    }
}
