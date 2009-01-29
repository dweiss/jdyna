package com.dawidweiss.dyna;

import java.io.Serializable;

/**
 * <p>
 * An event in the game.
 * <p>
 * All events should implement {@link Serializable} interface so that they can be written
 * and read from binary streams. The following things are of importance:
 * <ul>
 * <li>a unique <code>serialVersionUID</code> is not really required since serialization
 * will take place at runtime and will not be persisted between game versions with major
 * changes in the serialization API. That said, it may be a problem to exchange serialized
 * data between different VMs if the automatically generated serial UID is different. This
 * will have to be investigated experimentally. I set a custom UID for now. See
 * {@link #serialVersionUID} for more info.</li>
 * <li>strive for performance, if possible. Serializing simple fields is probably easy,
 * but serialization of complex data structures should be done by hand.</li>
 * <li>if the serialization costs are still to large, we may have to switch to entirely
 * hand-written serialization code. This is a last-resort option, of course.</li>
 * </ul>
 * 
 * @see IGameEventListener
 * @see Game#addListener(IGameEventListener)
 */
public abstract class GameEvent implements Serializable
{
    /**
     * Let serial UID encode the date and time of the most recent <b>incompatible</b>
     * change to the structure of each event. Avoid such changes, if possible.
     * <p>
     * The format is: <code>0xyyyymmddhhss</code> (long).
     */
    private static final long serialVersionUID = 0x200812241355L;

    /**
     * Unique code for each event type dispatched from the game.
     */
    public static enum Type
    {
        /** Game state (grid cells, players, sprites) update. */
        GAME_STATE,

        /** A sound effect should be played. */
        SOUND_EFFECT,

        /** A game start event (dispatched at the beginning of a single game). */
        GAME_START,

        /** A game over event (dispatched at the end of a single game). */
        GAME_OVER,

        /**
         * NO-OP event (marker symbol for events removed from a frame, usually due to
         * frame dropping).
         */
        NO_OP, 
        
        /**
         * Game status event (dispatched throughout the game and before game over).
         */
        GAME_STATUS,

        /**
         * Wall time from the game (used for game-saves).
         */
        GAME_WALL_TIME
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
