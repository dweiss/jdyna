package org.jdyna;

import java.io.Serializable;

/**
 * Game settings and configuration.
 */
public final class GameConfiguration implements Serializable
{
    /**
     * @see GameEvent#serialVersionUID
     */
    private static final long serialVersionUID = 0x200912130117L;


    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        final GameConfiguration objectClone = new GameConfiguration();
        objectClone.DEFAULT_FRAME_RATE = DEFAULT_FRAME_RATE;
        objectClone.DEFAULT_BOMB_RANGE = DEFAULT_BOMB_RANGE;
        objectClone.DEFAULT_FUSE_FRAMES = DEFAULT_FUSE_FRAMES;
        objectClone.DEFAULT_BOMB_COUNT = DEFAULT_BOMB_COUNT;
        objectClone.DEFAULT_BONUS_PERIOD = DEFAULT_BONUS_PERIOD;
        objectClone.DEFAULT_RESURRECTION_FRAMES = DEFAULT_RESURRECTION_FRAMES;
        objectClone.DEFAULT_IMMORTALITY_FRAMES = DEFAULT_IMMORTALITY_FRAMES;
        objectClone.DEFAULT_JOINING_IMMORTALITY_FRAMES = DEFAULT_JOINING_IMMORTALITY_FRAMES;
        objectClone.DEFAULT_LIVES = DEFAULT_LIVES;
        objectClone.DEFAULT_DIARRHEA_FRAMES = DEFAULT_DIARRHEA_FRAMES;
        objectClone.DEFAULT_NO_BOMBS_FRAMES = DEFAULT_NO_BOMBS_FRAMES;
        objectClone.DEFAULT_MAXRANGE_FRAMES = DEFAULT_MAXRANGE_FRAMES;
        objectClone.DEFAULT_SPEED_FRAMES = DEFAULT_SPEED_FRAMES;
        objectClone.SPEED_UP_MULTIPLIER = SPEED_UP_MULTIPLIER;
        objectClone.SLOW_DOWN_MULTIPLIER = SLOW_DOWN_MULTIPLIER;
        objectClone.DEFAULT_CRATE_WALKING_FRAMES = DEFAULT_CRATE_WALKING_FRAMES;
        objectClone.DEFAULT_BOMB_WALKING_FRAMES = DEFAULT_BOMB_WALKING_FRAMES;
        objectClone.DEFAULT_CONTROLLER_REVERSE_FRAMES = DEFAULT_CONTROLLER_REVERSE_FRAMES;
        objectClone.DEFAULT_CRATE_PERIOD = DEFAULT_CRATE_PERIOD;
        objectClone.ADD_RANDOM_CRATES = ADD_RANDOM_CRATES;
        objectClone.ENABLE_HIGHLIGHTS_DATA = ENABLE_HIGHLIGHTS_DATA;
        return objectClone;
    }

    /**
     * Default frame rate (frames per second) for the game's controller.
     */
    public int DEFAULT_FRAME_RATE = 25;

    /**
     * @see BombCell#range
     * @see PlayerInfo#bombRange
     */
    public int DEFAULT_BOMB_RANGE = _DEFAULT_BOMB_RANGE;
    public static final int _DEFAULT_BOMB_RANGE = 3;

    /**
     * @see BombCell#fuseCounter
     */
    public int DEFAULT_FUSE_FRAMES = 3 * DEFAULT_FRAME_RATE;
    public static final int _DEFAULT_FUSE_FRAMES = 75;

    /**
     * @see PlayerInfo#bombCount
     */
    public int DEFAULT_BOMB_COUNT = 2;

    /**
     * Default interval (in frames) between placing a new bonus on the playfield. The
     * default is 15 seconds. Assign to a very large value to effectively prevent bonuses
     * from showing up.
     */
    public int DEFAULT_BONUS_PERIOD = 12 * DEFAULT_FRAME_RATE;

    /**
     * Number of frames it takes for a dead player to be resurrected in death match mode.
     */
    public int DEFAULT_RESURRECTION_FRAMES = 5 * DEFAULT_FRAME_RATE;

    /**
     * For how many frames is the player immortal after resurrection?
     */
    public int DEFAULT_IMMORTALITY_FRAMES = 5 * DEFAULT_FRAME_RATE;

    /**
     * Default number of immortality frames upon joining to an existing game.
     */
    public int DEFAULT_JOINING_IMMORTALITY_FRAMES = 3 * DEFAULT_FRAME_RATE;

    /**
     * The default number of lives a player has in the {@link Game.Mode#DEATHMATCH} mode.
     */
    public int DEFAULT_LIVES = 3;

    /**
     * The default number of frames for which bomb-dropping-diarrhea lasts. It is a bit
     * lower than {@link GameConfiguration#DEFAULT_FUSE_FRAMES} so that the player can avoid
     * self-destruction when having many bombs collected.
     */
    public int DEFAULT_DIARRHEA_FRAMES = DEFAULT_FUSE_FRAMES - 20;

    /**
     * For how many frames the player cannot place any bombs.
     */
    public int DEFAULT_NO_BOMBS_FRAMES = 10 * DEFAULT_FRAME_RATE;

    /**
     * The default number of frames in which the player's bombs have maximum explosion
     * range.
     */
    public int DEFAULT_MAXRANGE_FRAMES = 10 * DEFAULT_FRAME_RATE;

    /**
     * The default number of frames player has a speed up or slow down bonus.
     */
    public int DEFAULT_SPEED_FRAMES = 10 * DEFAULT_FRAME_RATE;

    /**
     * Multiplier for speed up bonus. It should not be changed because other values can
     * cause unreal effects in walking.
     */
    public float SPEED_UP_MULTIPLIER = 1.5f;

    /**
     * Multiplier for slow down bonus. It should not be changed because other values can
     * cause unreal effects in walking.
     */
    public float SLOW_DOWN_MULTIPLIER = 0.5f;

    /**
     * Duration of the {@link CellType#CELL_BONUS_CRATE_WALKING} bonus.
     */
    public int DEFAULT_CRATE_WALKING_FRAMES = 10 * DEFAULT_FRAME_RATE;

    /**
     * For how many frames the player can walk through bombs.
     */
    public int DEFAULT_BOMB_WALKING_FRAMES = 20 * DEFAULT_FRAME_RATE;

    /**
     * The default number of frames player has a controller reverse disease.
     */
    public int DEFAULT_CONTROLLER_REVERSE_FRAMES = 10 * DEFAULT_FRAME_RATE;

    /**
     * Default period for adding crates at random positions on the board if
     * {@link #ADD_RANDOM_CRATES} is enabled.
     */
    public int DEFAULT_CRATE_PERIOD = 17 * DEFAULT_FRAME_RATE;

    /**
     * If enabled, new crates are added randomly to the board every
     * {@link #DEFAULT_CRATE_PERIOD}.
     */
    public boolean ADD_RANDOM_CRATES = false;
    
    /**
     * If enabled, highlights detector is enabled and sending highlights data.
     * {@link IHighlightDetector} 
     */
    public boolean ENABLE_HIGHLIGHTS_DATA = true;
}
