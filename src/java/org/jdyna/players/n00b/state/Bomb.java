package org.jdyna.players.n00b.state;

import java.util.List;

import org.jdyna.GameConfiguration;

/**
 * Represents a single bomb planted on the board.
 */
class Bomb
{
    /**
     * Standard duration of the explosion.
     */
    final static int EXPLOSION_DURATION = 14;

    /**
     * The player that planted this bomb.
     */
    final Player owner;

    /**
     * Effective blast range.
     */
    final int range;

    /**
     * Spaces that will be affected by this bomb's explosion.
     */
    private List<Space> affectedSpaces;

    /**
     * Number of frames left until detonation.
     */
    private int timer;

    Bomb(Player owner, GameConfiguration conf)
    {
        this.owner = owner;

        if (owner != null)
        {
            owner.dropBomb();
            range = owner.getRange();
        }
        else
        {
            range = conf.DEFAULT_BOMB_RANGE;
        }
    }

    int getTimer()
    {
        return timer;
    }

    void setTimer(int timer)
    {
        this.timer = timer;
    }

    List<Space> getAffectedSpaces()
    {
        return affectedSpaces;
    }

    void setAffectedSpaces(List<Space> affectedSpaces)
    {
        this.affectedSpaces = affectedSpaces;
    }

    /**
     * Disarms the bomb without detonating it.
     * <p>
     * This removes the timers representing this bomb's explosion from all the spaces
     * affected by it and updates the owner's bomb count.
     */
    void disarm()
    {
        // remove timers
        for (Space space : affectedSpaces)
        {
            space.removeTimer(timer);
        }

        // update the owner's bomb count
        if (owner != null)
        {
            owner.collectBomb();
        }
    }

    /**
     * Detonates the bomb, updating the owner's bomb count.
     */
    void detonate()
    {
        // update the owner's bomb count
        if (owner != null)
        {
            owner.collectBomb();
        }
    }
}
