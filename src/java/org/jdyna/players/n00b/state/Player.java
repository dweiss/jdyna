package org.jdyna.players.n00b.state;

import java.awt.Point;

import org.jdyna.GameConfiguration;

/**
 * Represents a single player taking part in the game.
 */
public class Player
{
    /**
     * Unique identifier.
     */
    private final String name;

    /**
     * Current blast range.
     */
    private int range;

    /**
     * Number of bombs that can currently be planted.
     */
    private int bombsRemaining;

    /**
     * Current position in pixel coordinates.
     */
    private Point position;

    /**
     * Current state the player is in.
     */
    private PlayerState state;

    /**
     * Configuration and settings.
     */
    GameConfiguration conf;

    Player(GameConfiguration conf, String name, Point position)
    {
        this.name = name;
        this.range = conf.DEFAULT_BOMB_RANGE;
        this.bombsRemaining = conf.DEFAULT_BOMB_COUNT;
        this.position = position;
        this.state = PlayerState.CHEATING;
        
        this.conf = conf;
    }

    public Point getPosition()
    {
        return position;
    }

    void setPosition(Point position)
    {
        this.position = position;
    }

    void setPosition(int x, int y)
    {
        this.position = new Point(x, y);
    }

    public PlayerState getState()
    {
        return state;
    }

    void setState(PlayerState state)
    {
        this.state = state;
        if (state == PlayerState.PWNED)
        {
            reset();
        }
    }

    public String getName()
    {
        return name;
    }

    public int getRange()
    {
        return range;
    }

    void increaseRange()
    {
        range++;
    }

    public int getBombsRemaining()
    {
        return bombsRemaining;
    }

    void dropBomb()
    {
        bombsRemaining--;
    }

    void collectBomb()
    {
        if (state != PlayerState.PWNED)
        {
            bombsRemaining++;
        }
    }

    /**
     * Resets the blast range and bomb count.
     */
    private void reset()
    {
        this.range = conf.DEFAULT_BOMB_RANGE;
        this.bombsRemaining = conf.DEFAULT_BOMB_COUNT;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Player other = (Player) obj;
        if (name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        }
        else if (!name.equals(other.name))
        {
            return false;
        }
        return true;
    }

    /**
     * Describes the state the player is in (normal, dead or immortal).
     */
    public enum PlayerState
    {
        /**
         * Normal. Can move, claim power-ups, plant bombs and get killed.
         */
        PWNING,

        /**
         * Dead. Can't do anything.
         */
        PWNED,

        /**
         * Immortal. Can move, but can't claim power-ups, plant bombs or get killed.
         */
        CHEATING
    }
}
