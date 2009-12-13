package org.jdyna;

/**
 * These are game-wide constants that we don't expect to change (ever).
 */
public final class Constants
{
    /**
     * Default pixel size of each cell in the playfield.
     */
    public static final int DEFAULT_CELL_SIZE = 16;

    /**
     * After the game is over we may want to linger a bit so that views can paint the
     * death sequence of the last player.
     */
    public static final int DEFAULT_LINGER_FRAMES = 45;

    /**
     * When the player is on the edge between one cell and another, dropping a bomb may
     * result in placing bombs in two adjacent cells. We add a small delay between placing
     * two bombs, so that the player has a chance to depress the bomb drop key (or
     * whatever stimuli).
     * <p>
     * This field is the number of frames that must pass before the player is allowed to
     * drop another bomb.
     */
    public static final int BOMB_DROP_DELAY = 5;

    /**
     * In classic Dyna, all bombs connected by explosions explode at once. This alters
     * this behavior and only "chains" explosions by speeding up other bombs. This works
     * fine, but the GUI is a bit screwed because explosion overlays don't work so well
     * then.
     */
    public static final boolean DELAYED_BOMB_EXPLOSIONS = false;
    
    /**
     * Default speed for a player.
     * 
     * @see PlayerInfo#speed
     */
    public static final int DEFAULT_PLAYER_SPEED = 2;

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
}
