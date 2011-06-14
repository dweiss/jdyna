package org.jdyna;

import java.util.Collections;
import java.util.List;



/**
 * An event dispatched throughout the game when one of the players changed status.
 */
public final class GameStatusEvent extends GameEvent
{
    /**
     * @see GameEvent#serialVersionUID
     */
    private static final long serialVersionUID = 0x200901261355L;

    /**
     * An array of statistics for individual players.
     */
    public List<PlayerStatus> stats;

    /**
     * An array of statistics for teams. May be empty if there
     * are no teams.
     */
    public List<TeamStatus> teamStats;

    /*
     * Serialization.
     */
    protected GameStatusEvent()
    {
        this(null);
    }

    /*
     *  
     */
    public GameStatusEvent(List<PlayerStatus> stats, List<TeamStatus> teamStats)
    {
        super(GameEvent.Type.GAME_STATUS);

        this.stats = Collections.unmodifiableList(stats);
        this.teamStats = Collections.unmodifiableList(teamStats);
    }

    /*
     *  
     */
    public GameStatusEvent(List<PlayerStatus> stats)
    {
        this(stats, Collections.<TeamStatus> emptyList());
    }
}
