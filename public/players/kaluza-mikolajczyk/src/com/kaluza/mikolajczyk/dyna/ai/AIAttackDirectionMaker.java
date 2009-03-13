package com.kaluza.mikolajczyk.dyna.ai;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdyna.CellType;
import org.jdyna.IPlayerController.Direction;

import com.kaluza.mikolajczyk.dyna.GameStateCollector;
import com.kaluza.mikolajczyk.dyna.utils.BombUtils;
import com.kaluza.mikolajczyk.dyna.utils.NeighborhoodUtils;
import com.kaluza.mikolajczyk.dyna.utils.PositionUtils;

/**
 * This direction maker is more complex than the {@link AIRetreatDirectionMaker}. 
 * It recurrencivelly (rekurencyjnie?) searches path to the given target. The search is stopped after some iterations to not slow down the AI.
 */
public class AIAttackDirectionMaker
{
    private final int defaultNumOfIters = 200; 
    
    private Point myPosition;

    private Point targetPosition;
    
    private int shortestDistance = Integer.MAX_VALUE;
    
    private int manhattanDistance;
    
    private boolean foundBest = false;
    
    private List<Point> shortestPathToTarget;
    
    private List<Point> forbiddenNodes;

    private int numOfIters;

    private GameStateCollector gameState;

    public AIAttackDirectionMaker(GameStateCollector gameState)
    {
        this.gameState = gameState;
        this.numOfIters = defaultNumOfIters;
    }

    private void reset()
    {
        this.shortestDistance = Integer.MAX_VALUE;
        
        this.shortestPathToTarget = new ArrayList<Point>();
        numOfIters = defaultNumOfIters;
        
        this.forbiddenNodes = new ArrayList<Point>();
        this.foundBest = false;
        
    }
    
    /**
     * Pick next direction based on the target position and current position of the AI.
     */
    public Direction pickDirection(Point target)
    {
        
        init(target);
        
        if(gameState.getCells() == null || myPosition == null || targetPosition == null)
        {
            return null;
        }
        
        Point next = findOnLastPath(shortestPathToTarget);
        
        //if the next cell is not null and is still safe return direction to this cell.
        if(next != null && 
            BombUtils.isSafe(gameState.getDangerousCells(), next) && 
            gameState.getCells()[next.x][next.y].type != CellType.CELL_BOMB &&
            !gameState.getCells()[next.x][next.y].type.isLethal())
        {
            return PositionUtils.obtainDirection(myPosition, next);
        }
        else
        {
            reset();
        }
        
        //try to find a new path to the target.
        boolean pathFound = findPath(0, new ArrayList<Point>(), myPosition);
        
        if(pathFound)
        {   
            next = shortestPathToTarget.get(0);
        
            return PositionUtils.obtainDirection(myPosition, next);
        }
        //logger.info("no path found.");
            
        return null;
    }

    
    /**
     * If the path to the target has already been evaluated check if the current position of the target is still 
     * in the path. Return the next position from the current if the target is still on the path and then shorten the saved path path.
     */
    private Point findOnLastPath(List<Point> shortestPath)
    {
        if(shortestPath == null) return null;
        
        int i = shortestPath.indexOf(myPosition);
        
        if(shortestPath.contains(myPosition) && shortestPath.contains(targetPosition))
        {
            int j = shortestPath.indexOf(targetPosition);
            
            if(i < j)
            {
                shortestPath = shortestPath.subList(i, j + 1);
                if(shortestPath.size() > 1 && isSafe(shortestPath.get(1)))
                {
                    return shortestPath.get(1);
                }
            }
            else
            {
                List<Point> sublist = new ArrayList<Point>(shortestPath.subList(j, i + 1));
                
                Collections.reverse(sublist);
                shortestPath = sublist;
                
                if(sublist.size() > 1 && isSafe(sublist.get(1)))
                {
                    return sublist.get(1);
                }
            }
        }
        
        return null;
    }

    private void init(Point target)
    {
        targetPosition = target;
        
        if(gameState.getMe() != null && target != null)
        {
            this.myPosition = gameState.getMe();
            
            forbiddenNodes = new ArrayList<Point>();
            forbiddenNodes.add(myPosition);
        
            manhattanDistance = PositionUtils.manhattanDistance(myPosition, targetPosition);
        }
    }
    
    private List<Point> obtainWalkableNeighbors(Point current, List<Point> forbiddenNodesList)
    {
        
        List<Point> neighbors = NeighborhoodUtils.obtainWalkableNeighbors(gameState.getCells(), gameState.getDangerousCells(), myPosition, targetPosition, current, forbiddenNodesList);
        
        return neighbors;
    }
    
    /**
     * Recurrence method of obtaining the path to the target player. The efficiency of this method depends of the obtainWalkableNeighbors method.
     */
    private boolean findPath(int currentDistance, List<Point> currentPath, Point current)
    {
        //fast cutoffs
        if(foundBest) return true;
        if(--numOfIters < 0) return false;
        
        boolean pathFound = false; 
        
        List<Point> neighbors = obtainWalkableNeighbors(current, forbiddenNodes);

        currentDistance++;
        
        if(currentDistance >= shortestDistance)
        {
            return false;
        }
        
        forbiddenNodes.addAll(neighbors);
        
        if(neighbors.contains(targetPosition))
        {
            if(currentDistance < shortestDistance)
            {
                currentPath.add(targetPosition);
                shortestDistance = currentDistance;
                saveShortestPath(currentPath);
                pathFound = true;
                
                if(currentDistance == manhattanDistance)
                {
                    foundBest = true;
                    return true;
                }
                
                currentPath.remove(targetPosition);
            }
        }
        
        for(Point neighbor : neighbors)
        {
            if(neighbor == null) continue;
            
            currentPath.add(neighbor);
            
            boolean result = findPath(currentDistance, currentPath, neighbor);
            pathFound = !pathFound ? result : true;
            
            currentPath.remove(neighbor);
            
            forbiddenNodes.remove(neighbor);
            if(pathFound)
            {
                break;
            }
        }
        
        return pathFound;
    }

    private void saveShortestPath(List<Point> currentPath)
    {
        shortestPathToTarget = new ArrayList<Point>(currentPath);
    }
    
    private boolean isSafe(Point position)
    {
        return BombUtils.isSafe(gameState.getDangerousCells(), position);
    }
}
