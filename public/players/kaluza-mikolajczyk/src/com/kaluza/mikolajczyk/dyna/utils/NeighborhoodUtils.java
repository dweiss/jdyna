package com.kaluza.mikolajczyk.dyna.utils;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdyna.Cell;
import org.jdyna.CellType;

import com.kaluza.mikolajczyk.dyna.ai.AIAttackDirectionMaker;
import com.kaluza.mikolajczyk.dyna.dtos.BombTO;

public class NeighborhoodUtils
{
    /**
     * Switch the first and second element of the array.
     */
    public static void switchPlaces(Point[] array)
    {
        Point point = array[0];
        array[0] = array[1];
        array[1] = point;
    }
    
    public static boolean isWalkableNotExplosion(Cell[][] cells, Point neighbor)
    {
        return neighbor.x > 0 && 
                neighbor.x < cells.length &&
                neighbor.y > 0 && 
                neighbor.y < cells[neighbor.x].length && 
                cells[neighbor.x][neighbor.y].type.isWalkable() &&  //walkable 
                !cells[neighbor.x][neighbor.y].type.isExplosion();  //but not explosion
    }
    
    public static Set<Point>obtainWalkableNeighbors(Cell[][] cells, Point current)
    {
        int x = current.x;
        int y = current.y;
        
        Set<Point> neighbors = new HashSet<Point>();
        
        Point neighbor = new Point(x+1, y);
        if(isWalkableNotExplosion(cells, neighbor))
        {
            neighbors.add(neighbor);
        }
        neighbor = new Point(x-1, y);
        if(isWalkableNotExplosion(cells, neighbor))
        {
            neighbors.add(neighbor);
        }
        neighbor = new Point(x, y-1);
        if(isWalkableNotExplosion(cells, neighbor))
        {
            neighbors.add(neighbor);
        }
        neighbor = new Point(x, y+1);
        if(isWalkableNotExplosion(cells, neighbor))
        {
            neighbors.add(neighbor);
        }
        
        return neighbors;
    }
    
    /**
     * Check if the field is valid, is not on forbidden list and can go here according to {@link #canGoHere(Cell[][], Point, Point, Map)}
     */
    private static boolean isWalkableNotExplosionNotForbidden(Point next, 
                                                                Point myPosition, 
                                                                Point targetPosition, 
                                                                Cell[][] cells, 
                                                                List<Point> forbiddenNodesList, 
                                                                Map<BombTO, Set<Point>> dangerousCells)
    {
        if(next.x > 0 && next.x < cells.length && next.y > 0 && next.y < cells[next.x].length 
            && canGoHere(cells, next, targetPosition, dangerousCells))
        {
            if(!forbiddenNodesList.contains(next) /*&& (next.x != myPosition.x || next.y != myPosition.y)*/)
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * This method returns neighbors of the current point. The neighbors are sorted in a way to optimize the work time 
     * of the {@link AIAttackDirectionMaker}. There is a recurrence that searches path in depth so the order of the neighbors is important.
     */
    public static List<Point> obtainWalkableNeighbors(Cell[][] cells, 
                                                Map<BombTO, Set<Point>> dangerousCells, 
                                                Point myPosition, 
                                                Point targetPosition, 
                                                Point current, 
                                                List<Point> forbiddenNodesList
                                                )
    {
        
        int x = current.x;
        int y = current.y;
                                             //  left, right
        Point[] horizontalNeighbors = new Point[]{null, null};
                                             //  up, down
        Point[] verticalNeighbors = new Point[]{null, null};
        
        Point next = new Point(x - 1, y);
        if(isWalkableNotExplosionNotForbidden(next, myPosition, targetPosition, cells, forbiddenNodesList, dangerousCells))
        {
            horizontalNeighbors[0] = next;
        }
        
        next = new Point(x + 1, y);
        if(isWalkableNotExplosionNotForbidden(next, myPosition, targetPosition, cells, forbiddenNodesList, dangerousCells))
        {
            horizontalNeighbors[1] = next;
        }
        
        next = new Point(x, y - 1);
        if(isWalkableNotExplosionNotForbidden(next, myPosition, targetPosition, cells, forbiddenNodesList, dangerousCells))
        {
            verticalNeighbors[0] = next;
        }
        
        next = new Point(x, y + 1);
        if(isWalkableNotExplosionNotForbidden(next, myPosition, targetPosition, cells, forbiddenNodesList, dangerousCells))
        {
            verticalNeighbors[1] = next;
        }
        //if you are on the left of the target make the right neighbor first
        if(x < targetPosition.x)
        {
            NeighborhoodUtils.switchPlaces(horizontalNeighbors);
        }
        //if you are higher than the target mahe the down direction first.
        if(y < targetPosition.y)
        {
            NeighborhoodUtils.switchPlaces(verticalNeighbors);
        }
        
        int yDistance = Math.abs(y - targetPosition.y);
        
        Point[] first;
        Point[] second;
        
        if (yDistance == 0)
        {
            first = horizontalNeighbors;
            second = verticalNeighbors;
        }
        else
        {
            first = verticalNeighbors;
            second = horizontalNeighbors;
        }
        
        List<Point> walkableNeighbors = new ArrayList<Point>();
        //for(int i = 0; i < 2; i++)
        {
            walkableNeighbors.add(first[0]);
            walkableNeighbors.add(second[0]);
            walkableNeighbors.add(second[1]);
            walkableNeighbors.add(first[1]);
            
        }
        
        return walkableNeighbors;
    }
    
    /**
     * Checks if the player can go here. These conditions must be met
     * <ul>
     *  <li>Cell must be walkable</li>
     *  <li>cell must not be a bomb</li>
     *  <li>cell must not be an explosion from a bomb</li>
     *  <li>Cell must be safe according to the {@link BombUtils#isSafe(Map, Point)}</li>
     * </ul>
     */
    private static boolean canGoHere(Cell[][] cells, Point current, Point targetPosition, Map<BombTO, Set<Point>> dangerousCells)
    {
        return cells[current.x][current.y].type.isWalkable() && 
                BombUtils.isSafe(dangerousCells, current) && 
                !cells[current.x][current.y].type.isLethal() && 
                cells[current.x][current.y].type != CellType.CELL_BOMB;
    }
    
    /**
     * Obtains the closes object from the list of targets by the manhattan distance.
     */
    public static Point obtainClosestObject(Collection<? extends Point> targets, Point me)
    {
        int closestDistance = Integer.MAX_VALUE;
        Point closestObject = null;
        
        for(Point target : targets)
        {
            int distance = PositionUtils.manhattanDistance(me, target);
            if(distance < closestDistance)
            {
                closestDistance = distance;
                closestObject = target.getLocation();
            }
                
        }
        
        return closestObject;
    }
}
