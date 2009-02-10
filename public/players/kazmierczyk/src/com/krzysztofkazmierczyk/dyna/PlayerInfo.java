/**
 * Logic of the game not implemented in dyna project.
 */
package com.krzysztofkazmierczyk.dyna;

import java.awt.Point;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IPlayerSprite;

/**
 * This class contains everything what contains class {@code
 * com.dawidweiss.dyna.PlayerInfo} and is neccessary for me.
 * 
 * @author kazik
 */
public class PlayerInfo implements Cloneable
{

    public boolean isDead()
    {
        return dead;
    }

    /**
     * Movement speed in each direction.
     */
    public static final Point speed = new Point(2, 2);
    
    /**
     * Current arsenal to use (bomb count).
     */
    private int bombCount = Globals.DEFAULT_BOMB_COUNT;

    /**
     * Bomb range for this player. Assigned to {@link BombCell#range}.
     */
    private int bombRange = Globals.DEFAULT_BOMB_RANGE;
    
    private boolean dead = false;

    /** Number of bombs which player is able to drop */
    private int maxBombCount = bombCount;

    private final String name;
    /** I do not know if it is really necessary. */
    private Point position;

    public PlayerInfo(IPlayerSprite sprite)
    {
        this.name = sprite.getName();
        update(sprite);
    }

    /** This constructor is public to help testing. */
    public PlayerInfo(int bombCount, int bombRange, String name, Point position)
    {
        this.bombCount = bombCount;
        this.bombRange = bombRange;
        this.name = name;
        this.position = position;
    }

    public void collectedBonusBomb()
    {
        bombCount++;
        maxBombCount++;
    }
    
    public void collectedBonusRange()
    {
        bombRange++;
    }

    public void droppedBomb()
    {
        bombCount--;
    }

    public void explodedBomb()
    {
        bombCount++;
    }

    public int getBombCount()
    {
        return bombCount;
    }

    public int getBombRange()
    {
        return bombRange;
    }

    public int getMaxBombCount()
    {
        return maxBombCount;
    }

    public String getName()
    {
        return name;
    }

    public Point getPosition()
    {
        return position;
    }

    /**
     * Updates position of player on the board. IT DOES NOT UPDATE BOMB COUNT AND BOMB
     * RANGE!!!
     */
    public void update(IPlayerSprite sprite)
    {
        this.position = sprite.getPosition();
        this.dead = sprite.isDead();
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        PlayerInfo clone = new PlayerInfo(bombCount, bombRange, name, position);
        return clone;
    }
    
    /** returns cell on which player is located. */
    public Point getCell(BoardInfo bi) {
        return bi.pixelToGrid(position);
    }
}
