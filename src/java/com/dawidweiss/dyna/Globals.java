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

    /*
     * 
     */
    private Globals()
    {
        // no instances.
    }
}
