package com.kaluza.mikolajczyk.dyna;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.IPlayerSprite;
import com.kaluza.mikolajczyk.dyna.dtos.BombTO;
import com.kaluza.mikolajczyk.dyna.dtos.BonusTO;
import com.kaluza.mikolajczyk.dyna.dtos.PlayerTO;
import com.kaluza.mikolajczyk.dyna.dtos.BonusTO.BonusType;
import com.kaluza.mikolajczyk.dyna.utils.BombUtils;
import com.kaluza.mikolajczyk.dyna.utils.PositionUtils;

/**
 * This class is responsible for obtaining data from the board, processing it and saving wanted information like position of the bombs, 
 * bonuses and players. On the basis of obtained data dangerous cells are generated (in this context dangerous means in the range of a bomb).  
 */
public class GameStateCollector
{
    private PlayerTO me;
    
    private Map<String, PlayerTO> opponents;
    
    private Set<BombTO> bombs;
    
    /**
     * This field indicates all dangerous fields on the board. it is a map of sets (instead of just a set) to reduce the calculations per frame.
     */
    private Map<BombTO, Set<Point>> dangerousCells;
    
    private Set<BonusTO> bonuses;
    
    private int heartBeat;

    private Cell [][] cells;
    
    public GameStateCollector(String myName)
    {
        dangerousCells = new HashMap<BombTO, Set<Point>>();
        opponents = new HashMap<String, PlayerTO>();
        bombs = new HashSet<BombTO>();
        bonuses = new HashSet<BonusTO>();
        me = new PlayerTO(myName, heartBeat, true);
    }
    
    /**
     * Update the state of the collector.
     */
    public void update(Cell[][] cells, List<? extends IPlayerSprite> players, int frame)
    {
        this.cells = cells;
        if(cells == null) return;
        heartBeat++;
        
        for(int x = 0; x < cells.length; x++)
        {
            for(int y = 0; y < cells[x].length; y++)
            {
                switch (cells[x][y].type)
                {
                    case CELL_BONUS_BOMB:
                        addOrUpdateBonus(x,y,BonusType.BOMBS_AMMOUNT);
                        break;

                    case CELL_BONUS_RANGE:
                        addOrUpdateBonus(x,y,BonusType.BOMB_RANGE);
                        break;
                    case CELL_BOOM_XY:
                        removeBomb(x,y);
                        
                        break;
                        
                    case CELL_BOMB:
                        
                        addBomb(x,y, frame);
                        break;
                    
                }
            }
        }
        
        updateDangerousCells(cells);
        updatePlayers(players);
        updatePlayerBonuses();
        updateBombs(frame);
    }

    /**
     * Generate a tic on every bomb in the collector. Simulates time flow.
     */
    private void updateBombs(int frame)
    {
        for(BombTO bomb : bombs)
        {
            bomb.ticTac(frame);
        }
    }

    /**
     * If a new bomb was added fields in range of it must be saved.
     */
    private void updateDangerousCells(Cell [][] cells)
    {
        for(BombTO bomb : bombs)
        {
            if(dangerousCells.containsKey(bomb))
            {
                continue;
            }
            
            dangerousCells.put(bomb, BombUtils.obtainDangerousCells(cells, bomb));
        }
        
    }

    /**
     * If a bonus is already on this field update it. If not put a new bonus here.
     */
    private void addOrUpdateBonus(int x, int y, BonusType type)
    {
        for(BonusTO bonus : bonuses)
        {
            if(bonus.hasPosition(x,y))
            {
                bonus.update(heartBeat);
                return;
            }
        }
        
        bonuses.add(new BonusTO(new Point(x,y), type, heartBeat));
    }

    /**
     * Removes bonuses from the collector that have been taken or have exploded. This assumption is made by the not updated hearbeat value.
     * If a bonus is gone assume that a player that is standing on it's location has taken it. If no one stands here the bonus probably exploded.
     */
    private void updatePlayerBonuses()
    {
        Set<BonusTO> takenBonuses = new HashSet<BonusTO>();
        
        for(BonusTO bonus : bonuses)
        {
            if(bonus.getHeartBeat() < heartBeat)
            {
                PlayerTO bonusTaker = obtainMortalPlayer(bonus.getPosition().x, bonus.getPosition().y);
                
                if(bonusTaker != null)//it might have exploded
                {
                    switch (bonus.getBonusType())
                    {
                        case BOMB_RANGE:
                            bonusTaker.incBombRange();
                            break;
    
                        case BOMBS_AMMOUNT:
                            bonusTaker.incMaxBombs();
                            break;
                    }
                }
                takenBonuses.add(bonus);
            }
        }
        
        for(BonusTO bonus : takenBonuses)
        {
            bonuses.remove(bonus);
        }
    }

    /**
     * Add bomb only if there is no other bomb on this position.
     */
    private void addBomb(int x, int y, int frameNum)
    {
        for(BombTO bomb : bombs)
        {
            if(bomb.hasPosition(x, y))
            {
                return;
            }
        }
        
        PlayerTO owner = obtainMortalPlayer(x,y);
        
        Point bombsLocation = new Point(x,y);
        
        //this value contains creation time of the bomb. From this value the time of the bomb's explosion is calculated.
        //If this bomb is in range of another bomb it will explode with it, so time of creation is intencionally decreased. 
        int nearBombsCreationTime = frameNum;
        
        for(BombTO bomb : dangerousCells.keySet())
        {
            for(Point dangerousCell : dangerousCells.get(bomb))
            {
                if(dangerousCell.equals(bombsLocation) && bomb.getCreationTime() < nearBombsCreationTime)
                {
                    nearBombsCreationTime = bomb.getCreationTime();
                    break;
                }
            }
        }
        
        if(owner != null)
        {
            bombs.add(new BombTO(owner, nearBombsCreationTime));
        }
        else
        {
            //we don't know the owner (just joined the game) so we must assume that the bomb will explode soon.
            bombs.add(new BombTO(x,y));
        }
    }
    
    /**
     * Obtain mortal player that stands on this location.
     */
    private PlayerTO obtainMortalPlayer(int x, int y)
    {
        for(PlayerTO player : opponents.values())
        {
            if(player.hasPosition(x,y) && !player.isImmortal())
            {
                return player;
            }
        }
        if(me.hasPosition(x, y) && !me.isImmortal())
        {
            return me;
        }
        
        return null;
    }

    /**
     * Remove bomb with the given location with the dangerous cells that it has in range.
     */
    private void removeBomb(int x, int y)
    {
        BombTO bombToRemove = null;
        
        for(BombTO bomb : bombs)
        {
            if(bomb.hasPosition(x,y))
            {
                bombToRemove = bomb;
                break;
            }
        }
        
        if(bombToRemove != null)
        {
            bombs.remove(bombToRemove);
            dangerousCells.remove(bombToRemove);
        }
    }

    /**
     * Update the state of the players or add a new player. If a player is dead remove it from the collector.
     */
    private void updatePlayers(List<? extends IPlayerSprite> players)
    {
        if(players == null) return;
        
        for(IPlayerSprite player : players)
        {
            if(player.isDead())
            {
                if(opponents.containsKey(player.getName()))
                {
                    opponents.remove(player.getName());
                }
            }
            else if(me.getName().equals(player.getName()))
            {
                me.update(PositionUtils.transformToArrayIndices(player.getPosition()), heartBeat, player.isImmortal());
            }
            else
            {
                addOrUpdateOpponent(player);
            }
        }
        removeDeadPlayers();
    }

    /**
     * In case a player disappears from the board remove it from the collector.
     */
    private void removeDeadPlayers()
    {
        for(PlayerTO player : opponents.values())
        {
            if(player.getHeartBeat() < heartBeat)
            {
                opponents.remove(player.getName());
            }
        }
        
    }

    /**
     * If a new player is introduced add him to the collector.
     */
    private void addOrUpdateOpponent(IPlayerSprite playerSprite)
    {
        if(this.opponents.containsKey(playerSprite.getName()))
        {
            PlayerTO player = opponents.get(playerSprite.getName());
            player.update(PositionUtils.transformToArrayIndices(playerSprite.getPosition()), heartBeat, playerSprite.isImmortal());
        }
        else
        {
            this.opponents.put(playerSprite.getName(), 
                                new PlayerTO(playerSprite.getName(), 
                                             PositionUtils.transformToArrayIndices(playerSprite.getPosition()), 
                                             heartBeat, 
                                             playerSprite.isImmortal()
                                             )
                                );
        }
    }
    
    public Set<BonusTO> getBonuses()
    {
        return bonuses;
    }
    
    public Point getMe()
    {
        return me.getLocation();
    }
    
    public int getMyRange()
    {
        return me.getBombRange();
    }
    
    /**
     * Get mortal opponents. If no opponent is mortal return immortals.
     * Method used for picking a next target. 
     */
    public Collection<? extends Point> getOpponents()
    {
        Set<Point> notImmortals = new HashSet<Point>();
        
        for(PlayerTO player : opponents.values())
        {
            if(!player.isImmortal())
            {
                notImmortals.add(player);
            }
        }
        
        return notImmortals.size() > 0 ? notImmortals : opponents.values();
    }

    public Set<? extends BombTO> getBombs()
    {
        return bombs;
    }
    
    public Map<BombTO, Set<Point>> getDangerousCells()
    {
        return dangerousCells;
    }
    
    public Cell [][] getCells()
    {
        return cells;
    }
    
    /**
     * Get all cells that are in range of any bomb.
     */
    public Set<Point> getDangerousPositions()
    {
        Set<Point> dangerous = new HashSet<Point>();
        
        for(BombTO bomb : dangerousCells.keySet())
        {
            dangerous.addAll(dangerousCells.get(bomb));
        }
        
        return dangerous;
    }

    /**
     * Check if you are standing on a dangerous cell.
     */
    public boolean onDangerousPosition()
    {
        return getDangerousPositions().contains(me.getLocation());
    }

}
