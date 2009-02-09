package com.kaluza.mikolajczyk.dyna.utils;

import java.awt.Point;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.IPlayerController.Direction;
import com.kaluza.mikolajczyk.dyna.dtos.BombTO;

public class BombUtils
{
    /**
     * Is the current position of the player safe. In this context safe means that this cell is not in range of a bomb or 
     * time to bomb's explosion is more than 25 frames.
     */
    public static boolean isSafe(Map<BombTO, Set<Point>> dangerousCells, Point position)
    {
        for(BombTO b : dangerousCells.keySet())
        {
            if(dangerousCells.get(b).contains(position))
            {
                if(b.getTimeToExplosion() < 25)
                {
                    return false;
                }
            }
        }
        
        return true; 
    }
    
    private static Set<Point> obtainDangerousCellsInOneDirection(Cell[][] cells, int bombx, int bomby, int range, Direction direction)
    {
        Set<Point> dangerousCells = new LinkedHashSet<Point>();
        
        for(int i = 1; i <= range; i++)
        {
            int x = 0;
            int y = 0;
            
            switch (direction)
            {
                case DOWN:
                    x = bombx;
                    y = bomby + i;
                    break;
                case UP:
                    x = bombx;
                    y = bomby - i;
                    break;
                case LEFT:
                    x = bombx - i;
                    y = bomby;
                    break;
                case RIGHT:
                    x = bombx + i;
                    y = bomby;
                    break;
            }
            
            if(x >= 0 && x < cells.length && y >= 0 && y < cells[x].length)
            {
                if(cells[x][y].type.isWalkable())
                {
                    dangerousCells.add(new Point(x, y));
                }
                //As soon as you meet a non-walkable object the dangerous cells end.
                else
                {
                    return dangerousCells;
                }
            }
        }
        
        return dangerousCells;
    }
    
    public static Set<Point> obtainDangerousCells(Cell [][] cells, BombTO bomb)
    {
        Set<Point> dangerousCells = new LinkedHashSet<Point>();
        dangerousCells.add(bomb.getPosition());
        
        dangerousCells.addAll(obtainDangerousCellsInOneDirection(cells, bomb.getPosition().x, bomb.getPosition().y, bomb.getRange(), Direction.UP));
        dangerousCells.addAll(obtainDangerousCellsInOneDirection(cells, bomb.getPosition().x, bomb.getPosition().y, bomb.getRange(), Direction.DOWN));
        dangerousCells.addAll(obtainDangerousCellsInOneDirection(cells, bomb.getPosition().x, bomb.getPosition().y, bomb.getRange(), Direction.LEFT));
        dangerousCells.addAll(obtainDangerousCellsInOneDirection(cells, bomb.getPosition().x, bomb.getPosition().y, bomb.getRange(), Direction.RIGHT));
        
        return dangerousCells;
    }
}
