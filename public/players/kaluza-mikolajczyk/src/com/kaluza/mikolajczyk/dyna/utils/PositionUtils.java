package com.kaluza.mikolajczyk.dyna.utils;

import java.awt.Point;

import org.jdyna.Globals;
import org.jdyna.IPlayerController.Direction;


public class PositionUtils
{
    /**
     * Warning! Whis method only works when the given two points are in the same X axis or in the Y axis.
     */
    public static Direction obtainDirection(Point actual, Point wanted)
    {
        if(actual.x < wanted.x)
        {
            return Direction.RIGHT;
        }
        if(actual.x > wanted.x)
        {
            return Direction.LEFT;
        }
        if(actual.y < wanted.y)
        {
            return Direction.DOWN;
        }
        if(actual.y > wanted.y)
        {
            return Direction.UP;
        }
        
        return null;
    }
    
    public static Point transformToArrayIndices(Point spritePosition)
    {
        return new Point(spritePosition.x / Globals.DEFAULT_CELL_SIZE, spritePosition.y / Globals.DEFAULT_CELL_SIZE);
    }
    
    /**
     * Calculate manhattan distance between two locations. 
     */
    public static int manhattanDistance(Point a, Point b)
    {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
}
