package com.kaluza.mikolajczyk.dyna.ai;

import java.awt.Point;
import java.util.Set;
import java.util.TreeMap;

import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IPlayerController.Direction;
import com.kaluza.mikolajczyk.dyna.GameStateCollector;
import com.kaluza.mikolajczyk.dyna.dtos.BombTO;
import com.kaluza.mikolajczyk.dyna.utils.NeighborhoodUtils;
import com.kaluza.mikolajczyk.dyna.utils.PositionUtils;

/**
 * The target of this class is to evaluate the current field and the all direct neighbors and pick the cell that has the highest evaluation rate.
 */
public class AIRetreatDirectionMaker
{
    private static final int BOMB_FUSE_T = Globals.DEFAULT_FUSE_FRAMES;
    
    private Point myPosition;

    private GameStateCollector gameState;

    public AIRetreatDirectionMaker(GameStateCollector gameState)
    {
        this.gameState = gameState;
    }
    
    public Direction pickDirection()
    {
        myPosition = gameState.getMe();
        
        Point bestLocation = findBestLocation();
        return PositionUtils.obtainDirection(myPosition, bestLocation);
    }

    /**
     * Evaluate rate of safeness of every direct neighbor and current cell. Pick the best.
     */
    private Point findBestLocation()
    {
        TreeMap<Integer, Point> safenessMap = new TreeMap<Integer, Point>();
        
        int currentEvaluation = evaluateSafeness(myPosition);
        safenessMap.put(currentEvaluation, myPosition);
        
        Set<Point> neighbors = NeighborhoodUtils.obtainWalkableNeighbors(gameState.getCells(), myPosition);
        
        for(Point neighbor : neighbors)
        {
            safenessMap.put(evaluateSafeness(neighbor), neighbor);
        }
        
        return currentEvaluation < safenessMap.lastKey() ? safenessMap.get(safenessMap.lastKey()) : myPosition;
        
    }
    
    /**
     * Evaluate the point of safeness of current field.
     * Evaluation is based on:
     * <ul>
     *  <li>Distances from the bombs</li>
     *  <li>Time to the explosion of every bomb that is in range</li>
     *  <li>Number of direct walkable neighbors</li>
     *  <li>Distances to the opponents</li>
     * </ul>
     */
    private int evaluateSafeness(Point current)
    {
        int numOfNeighbors = NeighborhoodUtils.obtainWalkableNeighbors(gameState.getCells(), current).size();
        
        int evaluation = numOfNeighbors * numOfNeighbors * 100;
        
        for (BombTO bomb : gameState.getDangerousCells().keySet())
        {
            if(gameState.getDangerousCells().get(bomb).contains(current))
            {
                int dangerousness = 1000 + (BOMB_FUSE_T - bomb.getTimeToExplosion()) * 5 - PositionUtils.manhattanDistance(current, bomb.getPosition()) ;
                if(bomb.getTimeToExplosion() < BOMB_FUSE_T/2)
                {
                    //extra kick
                    dangerousness += 500;
                }
                if(PositionUtils.manhattanDistance(current, bomb.getPosition()) < 1)
                {
                    dangerousness += 1000;
                }
                dangerousness += bomb.getPosition().equals(current) ? dangerousness : 0;

                evaluation -= dangerousness;
            }
        }
        
        for (Point opp : gameState.getOpponents())
        {
            //further from the opponent is safer
            evaluation += PositionUtils.manhattanDistance(current, opp)/2;
        }
        
        return evaluation;
        
    }
}
