package org.jdyna.players.n00b.state;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single space on the board.
 */
public class Space
{
    /**
     * Horizontal grid coordinate.
     */
    public final int x;

    /**
     * Vertical grid coordinate.
     */
    public final int y;

    /**
     * List of timers representing time left until the explosions that will affect this
     * space.
     */
    private final List<Integer> timers;

    /**
     * Current type.
     */
    private SpaceType type;

    /**
     * Bomb planted on this space (if there is one).
     */
    private Bomb bomb;

    Space(int x, int y)
    {
        this.x = x;
        this.y = y;
        this.timers = new ArrayList<Integer>();
    }

    public SpaceType getType()
    {
        return type;
    }

    void setType(SpaceType type)
    {
        this.type = type;
    }

    Bomb getBomb()
    {
        return this.bomb;
    }

    void setBomb(Bomb bomb)
    {
        this.bomb = bomb;
        if (bomb != null)
        {
            this.type = SpaceType.BOMB;
        }
        else
        {
            this.type = SpaceType.CLEAR;
        }
    }

    List<Integer> getTimers()
    {
        return timers;
    }

    void addTimer(int timer)
    {
        timers.add(timer);
    }

    void removeTimer(int timer)
    {
        timers.remove((Integer) timer);
    }

    public Point toPoint()
    {
        return new Point(x, y);
    }

    /**
     * Checks if there are any ongoing or expected explosions affecting this space.
     */
    public boolean isHot()
    {
        if (timers.size() > 0)
        {
            return true;
        }
        return false;
    }

    /**
     * Checks if there are no ongoing explosions affecting this space.
     */
    public boolean isSafe()
    {
        return willBeSafe(0, 0);
    }

    /**
     * Checks if there will be no explosions affecting this space in the given time
     * window.
     * 
     * @param from number of frames until the start of the window
     * @param to number of frames until the end of the window
     */
    public boolean willBeSafe(int from, int to)
    {
        for (Integer timer : timers)
        {
            if (!(timer - from < -Bomb.EXPLOSION_DURATION || timer - to > 0))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether players can walk over this space.
     * 
     * @return
     */
    public boolean isWalkable()
    {
        if (this.type == SpaceType.BOMB || this.type == SpaceType.CRATE
            || this.type == SpaceType.WALL)
        {
            return false;
        }
        return true;
    }

    /**
     * Checks how much time is left until the beginning of the next expected explosion
     * affecting this space.
     * <p>
     * Any ongoing explosions affecting this space are ignored.
     * 
     * @return the number of frames left until the explosion, or <code>null</code> if no
     *         explosions are expected on this space
     */
    public Integer getNextExplosion()
    {
        Integer next = null;
        for (Integer timer : timers)
        {
            if (timer > 0 && (next == null || next > timer))
            {
                next = timer;
            }
        }
        return next;
    }

    /**
     * Describes the type of the space (wall, crate, empty etc.).
     */
    public enum SpaceType
    {
        /**
         * Solid, indestructible obstacle.
         */
        WALL,

        /**
         * Solid, destructible obstacle.
         */
        CRATE,

        /**
         * Space with a bomb planted on it.
         */
        BOMB,

        /**
         * Space with a range power-up present.
         */
        BONUS_RANGE,

        /**
         * Space with a bomb limit power-up present.
         */
        BONUS_BOMB,

        /**
         * Simple, empty space.
         */
        CLEAR
    }
}
