package org.jdyna;




/**
 * Global defaults.
 */
public final class Globals
{
    /**
     * @see BombCell#range
     * @see PlayerInfo#bombRange
     */
    public static final int DEFAULT_BOMB_RANGE = 3;

    /**
     * @see BombCell#fuseCounter
     */
    public static final int DEFAULT_FUSE_FRAMES = 75;

    /**
     * @see PlayerInfo#bombCount
     */
    public static final int DEFAULT_BOMB_COUNT = 2;

    /**
     * @see PlayerInfo#speed
     */
    public static final int DEFAULT_PLAYER_SPEED = 2;

    /**
     * In classic Dyna, all bombs connected by explosions explode at once. This alters
     * this behavior and only "chains" explosions by speeding up other bombs. This works
     * fine, but the GUI is a bit screwed because explosion overlays don't work so well then.
     */
    public static final boolean DELAYED_BOMB_EXPLOSIONS = false;

    /**
     * Default frame rate for the game's controller.
     */
    public static final int DEFAULT_FRAME_RATE = 25;

    /**
     * Default interval (in frames) between placing a new bonus on the playfield.
     * The default is 15 seconds. Assign to a very large value to effectively prevent
     * bonuses from showing up.
     */
    public static final int DEFAULT_BONUS_PERIOD = 12 * DEFAULT_FRAME_RATE;

    /**
     * Weight for the most often appearing bonuses.
     */
    public static final int FREQUENT_BONUS_WEIGHT = 100;
    
    /**
     * Weight of bonuses appearing with default probability.
     */
    public static final int COMMON_BONUS_WEIGHT = 20;

    /**
     * Weight of bonuses appearing infrequently.
     */
    public static final int INFREQUENT_BONUS_WEIGHT = 5;

    /**
     * Number of frames it takes for a dead player to be resurrected in death match mode.
     */
    public static final int DEFAULT_RESURRECTION_FRAMES = 5 * DEFAULT_FRAME_RATE;

    /**
     * For how many frames is the player immortal after resurrection?
     */
    public static final int DEFAULT_IMMORTALITY_FRAMES = 5 * DEFAULT_FRAME_RATE;

    /**
     * Default number of immortality frames upon joining to an existing game.
     */
    public static final int DEFAULT_JOINING_IMMORTALITY_FRAMES = 3 * DEFAULT_FRAME_RATE;

    /**
     * The default number of lives a player has in the {@link Game.Mode#DEATHMATCH} mode.
     */
    public static final int DEFAULT_LIVES = 3;
    
    /**
     * The default number of frames for which bomb-dropping-diarrhea lasts.
     * It is a bit lower than {@link Globals#DEFAULT_FUSE_FRAMES} so that
     * the player can avoid self-destruction when having many bombs collected.
     */
    public static final int DEFAULT_DIARRHEA_FRAMES = DEFAULT_FUSE_FRAMES - 20;
    
    /**
     * For how many frames the player cannot place any bombs.
     */
    public static final int DEFAULT_NO_BOMBS_FRAMES = 10 * DEFAULT_FRAME_RATE;

    /**
     * The default number of frames in which the player's bombs have maximum explosion 
     * range.
     */
    public static final int DEFAULT_MAXRANGE_FRAMES = 10 * DEFAULT_FRAME_RATE;

    /**
     * The default number of frames player has a speed up or slow down bonus.
     */
    public static final int DEFAULT_SPEED_FRAMES = 10 * DEFAULT_FRAME_RATE;
    
    /**
     * Multiplier for speed up bonus. It should not be changed because
     * other values can cause unreal effects in walking. 
     */
    public static final float SPEED_UP_MULTIPLIER = 1.5f;
    
    /**
     * Multiplier for slow down bonus. It should not be changed because
     * other values can cause unreal effects in walking.
     */
    public static final float SLOW_DOWN_MULTIPLIER = 0.5f;

    /**
     * Duration of the {@link CellType#CELL_BONUS_CRATE_WALKING} bonus.
     */
    public static final int DEFAULT_CRATE_WALKING_FRAMES = 10* DEFAULT_FRAME_RATE;

    /**
     * For how many frames the player can walk through bombs.
     */
    public static final int DEFAULT_BOMB_WALKING_FRAMES = 20 * DEFAULT_FRAME_RATE;

    /**
     * The default number of frames player has a controller reverse disease.
     */
    public static final int DEFAULT_CONTROLLER_REVERSE_FRAMES = 10 * DEFAULT_FRAME_RATE;

    /**
     * Default period for adding crates at random positions on the board
     * if {@link #ADD_RANDOM_CRATES} is enabled.
     */
    public static final int DEFAULT_CRATE_PERIOD = 17 * DEFAULT_FRAME_RATE;

    /**
     * If enabled, new crates are added randomly to the board every
     * {@link #DEFAULT_CRATE_PERIOD}.
     */
    public static boolean ADD_RANDOM_CRATES = false;

    // These should not be configurable.

    /**
     * Default pixel size of each cell in the playfield.
     */
    public static final int DEFAULT_CELL_SIZE = 16;

    /**
     * After the game is over we may want to linger a bit so that views can paint the death
     * sequence of the last player.
     */
    public static final int DEFAULT_LINGER_FRAMES = 45;
    
    /**
     * When the player is on the edge between one cell and another, dropping
     * a bomb may result in placing bombs in two adjacent cells. We add a small
     * delay between placing two bombs, so that the player has a chance to depress
     * the bomb drop key (or whatever stimuli).
     * <p>
     * This field is the number of frames that must pass before the player is allowed
     * to drop another bomb.
     */
    public static final int BOMB_DROP_DELAY = 5;

    /*
     * 
     */
    private Globals()
    {
        // no instances.
    } 
}
