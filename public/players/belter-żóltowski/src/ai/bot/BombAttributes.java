package ai.bot;


import java.awt.Point;

import org.jdyna.Globals;


/**
 * Stores bomb properties. Copy-Pasted and adapted from original code because of too many 
 * <code>final</code> (class) modifiers and hidden methods/fields visibility.
 */
public class BombAttributes
{
    /**
     * If bomb has been dropped by a player, we need to keep its reference so that his
     * bomb counter can be restored properly.
     */
    PlayerAttributes player;

    /**
     * How many frames until explosion (down-to-zero counter).
     */
    int fuseCounter = Globals.DEFAULT_FUSE_FRAMES - Globals.DEFAULT_FRAME_RATE * 2;

    /**
     * Explosion range (number of cells in each direction).
     */
    int range = Globals.DEFAULT_BOMB_RANGE;
    
    Point location;

    /*
     * 
     */
    BombAttributes(PlayerAttributes player, Point location) {
        this.player = player;
        this.range = player.maxBombRange;
        this.location = location;
    }
}

