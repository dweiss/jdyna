package com.dawidweiss.dyna;

/**
 * Global defaults.
 */
final class Globals
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
    public static final int DEFAULT_BOMB_COUNT = 4;

    /**
     * In classic Dyna, all bombs connected by explosions explode at once. This alters
     * this behavior and only "chains" explosions by speeding up other bombs. This works
     * fine, but the GUI is a bit screwed because explosion overlays don't work so well then.
     */
    public static final boolean DELAYED_BOMB_EXPLOSIONS = false;

    /*
     * 
     */
    private Globals()
    {
        // no instances.
    }
}
