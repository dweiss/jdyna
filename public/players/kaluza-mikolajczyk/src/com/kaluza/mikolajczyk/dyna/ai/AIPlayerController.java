package com.kaluza.mikolajczyk.dyna.ai;

import java.awt.Point;
import java.util.List;
import java.util.logging.Logger;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameOverEvent;
import com.dawidweiss.dyna.GameStartEvent;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerSprite;
import com.kaluza.mikolajczyk.dyna.GameStateCollector;
import com.kaluza.mikolajczyk.dyna.utils.NeighborhoodUtils;

/**
 *  Main class of the Artificial Intelligence player. Obtains Events from the game engine and. processes them and invokes
 *  classes responsible for choosing next direction and decision about putting new bomb on board.
 */
public class AIPlayerController implements IGameEventListener, IPlayerController
{
    Logger logger = Logger.getAnonymousLogger();
    
    private String myName;
    private Direction direction;
    private boolean putsBomb;
    private AIAttackDirectionMaker directionMaker;
    private AIRetreatDirectionMaker retreatDirectionMaker;
    private GameStateCollector gameState;

    private AIBombsPutter bomber;

    public AIPlayerController(String name)
    {
        this.myName = name;
        gameState = new GameStateCollector(myName);
        this.directionMaker = new AIAttackDirectionMaker(gameState);
        retreatDirectionMaker = new AIRetreatDirectionMaker(gameState);
        bomber = new AIBombsPutter(gameState);
        
    }
    
    @Override
    public boolean dropsBomb()
    {
        return putsBomb;
    }

    @Override
    public Direction getCurrent()
    {
        return direction;
    }

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        List<? extends IPlayerSprite> players = null;
        Cell [][] cells = null;

        if(events != null && events.size() > 0)
        {
            for(GameEvent event : events)
            {
                if(event.getClass() == GameStateEvent.class)
                {
                    GameStateEvent gameEvent = (GameStateEvent) event;
                    
                    players = gameEvent.getPlayers();
                    
                    cells = gameEvent.getCells();
                    
                    //update the game state collector.
                    gameState.update(cells, players, frame);
                    
                    break;
                }
                else if(event.getClass() == GameStartEvent.class)
                {
                    //NOP
                }
                else if(event.getClass() == GameOverEvent.class)
                {
                    this.bomber = null;
                    this.direction = null;
                    this.directionMaker = null;
                    this.retreatDirectionMaker = null;
                    this.gameState = null;
                    
                    return;
                }
            }
        }
        
        
        
        Point bonus = NeighborhoodUtils.obtainClosestObject(gameState.getBonuses(), gameState.getMe());
        Point target = NeighborhoodUtils.obtainClosestObject(gameState.getOpponents(), gameState.getMe());

        Direction newDirection = null;
        if(cells == null || gameState.getMe() == null)
        {
            return;
        }
        
        //if you are standing on a bomb retreat from this position
        if(cells[gameState.getMe().x][gameState.getMe().y].type == CellType.CELL_BOMB)
        {
            direction = retreatDirectionMaker.pickDirection();
        }
        else
        {
            //The AI is bonus greedy - it goes for the bonus first and then for the opponent. 
            //Disadvantage - if there is many bonuses on board it is collecting them and does not go directly to the opponent. 
            //But if the opponent comes to close it puts bomb :>
            if(bonus != null)
            {
                newDirection = directionMaker.pickDirection(bonus);
            }
            if (newDirection == null)
            {
                //go for the opponent
                newDirection = directionMaker.pickDirection( target);
            }
            if (newDirection == null)
            {
                //path to the opponent not found. Go to the best position in the neighborhood.
                direction = retreatDirectionMaker.pickDirection();
            }
            else
            {
                direction = newDirection;
            }
        }
        
        putsBomb = bomber.putsBomb(target);
    }

}
