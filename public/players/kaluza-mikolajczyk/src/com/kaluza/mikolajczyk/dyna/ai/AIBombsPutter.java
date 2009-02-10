package com.kaluza.mikolajczyk.dyna.ai;

import java.awt.Point;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.Globals;
import com.kaluza.mikolajczyk.dyna.GameStateCollector;
import com.kaluza.mikolajczyk.dyna.utils.BombUtils;
import com.kaluza.mikolajczyk.dyna.utils.PositionUtils;

/**
 * Class responsible for making decisions about putting new bomb.
 */
public class AIBombsPutter
{
    
    private GameStateCollector gameState;

    public AIBombsPutter(GameStateCollector gameState)
    {
        this.gameState = gameState;
    }
    
    /**
     * When the manhattan distance between AI and target player is less than the player bomb's range - 1 put a bomb in current position.
     * @Deprecated use {@link #putsBomb(Point)} instead
     */
    public boolean putsBombOld(Point target)
    {
        Point me = gameState.getMe();
        int myRange = gameState.getMyRange();
        if(me == null || target == null)
        {
              return false;
        }
        if(PositionUtils.manhattanDistance(me, target) < (myRange - 1))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Method returns true if player should put a bomb on it's current location.
     * The decision is made on the basis of the facts:
     * <ul>
     *  <li> if you are in a range of a bomb that will explode sooner than 25 frames - do not put new bomb </li>
     *  <li> if the distance between you and the selected opponent is larger than half your bomb's range - do not put new bomb </li>
     *  <li> if you and the target are on different levels (have different X and Y values) - do not put a new bomb </li>
     *  <li> if between you and the target is a non-walkable object - do not put bomb</li>
     *  <li> in every other case put a new bomb</li>
     * </ul> 
     */
    public boolean putsBomb(Point target)
    {
        Point me = gameState.getMe();
        Cell[][] cells = gameState.getCells();
        int myRange = gameState.getMyRange();
        if(me == null || target == null)
        {
              return false;
        }
        
        if(isPositionDangerous())
        {
            return false;
        }
        
        int xDistance = Math.abs(me.x - target.x);
        int yDistance = Math.abs(me.y - target.y);
        
        if((xDistance  < (myRange + 1)/2 || xDistance < Globals.DEFAULT_BOMB_RANGE) &&  yDistance == 0 )
        {
            int min = Math.min(me.x, target.x);
            int max = Math.max(me.x, target.x);
            
            for(int i = min + 1; i < max; i++)
            {
                if(!cells[i][me.y].type.isWalkable())
                {
                    return false;
                }
            }
            
            return true;
        }
        else if(xDistance  == 0 &&  (yDistance < myRange + 1  || yDistance < Globals.DEFAULT_BOMB_RANGE))
        {
            int min = Math.min(me.y, target.y);
            int max = Math.max(me.y, target.y);
            
            for(int i = min + 1; i < max; i++)
            {
                if(!cells[me.x][i].type.isWalkable())
                {
                    return false;
                }
            }
            
            return true;
            
        }
        return false;
        
    }

    /**
     * Is the current position dangerous. 
     */
    private boolean isPositionDangerous()
    {
        return !BombUtils.isSafe(gameState.getDangerousCells(), gameState.getMe());

    }
}
