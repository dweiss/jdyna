package com.dawidweiss.dyna;

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
     * An array of statistics for players after the game is over. Statistics depend on the
     * game mode.
     */
    public List<PlayerStatus> stats;

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
    public GameStatusEvent(List<PlayerStatus> stats)
    {
        super(GameEvent.Type.GAME_STATUS);

        this.stats = Collections.unmodifiableList(stats);
    }
}
