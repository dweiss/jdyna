package com.dawidweiss.dyna;

import java.awt.event.KeyEvent;

import com.dawidweiss.dyna.input.KeyboardController;
import com.dawidweiss.dyna.view.swing.BoardPanel;
import com.dawidweiss.dyna.view.swing.Magnification;

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
    public static final int DEFAULT_BOMB_COUNT = 4;

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
     * Default view scaling in {@link BoardPanel}.
     */
    public static final Magnification DEFAULT_VIEW_MAGNIFICATION = Magnification.TIMES_2;

    /**
     * Paint player labels by default.
     */
    public static final boolean SWING_VIEW_PAINT_PLAYER_LABELS = false;


    /**
     * In classic Dyna, all bombs connected by explosions explode at once. This alters
     * this behavior and only "chains" explosions by speeding up other bombs. This works
     * fine, but the GUI is a bit screwed because explosion overlays don't work so well then.
     */
    public static final boolean DELAYED_BOMB_EXPLOSIONS = false;

    /**
     * When the player is on the edge between one cell and another, dropping
     * a bomb may result in placing bombs in two adjecent cells. We add a small
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
    
    /**
     * Returns "default" keyboard layout for a player numbered <code>num</code>.
     */
    public static IPlayerController getDefaultKeyboardController(int num)
    {
        switch (num)
        {
            case 0:
                return new KeyboardController(KeyEvent.VK_UP, KeyEvent.VK_DOWN,
                    KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_CONTROL);                
            case 1:
                return new KeyboardController(KeyEvent.VK_R, KeyEvent.VK_F,
                    KeyEvent.VK_D, KeyEvent.VK_G, KeyEvent.VK_Z);
        }
        throw new RuntimeException("No default keyboard mapping for player: " + num);
    } 
}
