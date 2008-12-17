package com.dawidweiss.dyna;

/**
 * An event in the game.
 * 
 * @see IGameEventListener
 * @see Game#addListener(IGameEventListener)
 */
public class GameEvent
{
    /**
     * Unique code for each event type dispatched from the game.
     */
    public static enum Type
    {
        /** Game state (grid cells, players, sprites) update. */
        GAME_STATE, 

        /** A sound effect should be played. */
        SOUND_EFFECT,
    }

    /**
     * This event's type. For switch branchoffs.
     */
    public final Type type;

    /**
     * Package private subclasses.
     */
    protected GameEvent(Type t)
    {
        this.type = t;
    }
}
