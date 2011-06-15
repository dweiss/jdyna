package org.jdyna.view.swing;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

import org.jdyna.*;
import org.jdyna.frontend.swing.Configuration;
import org.jdyna.frontend.swing.Configuration.KeyBinding;

import com.google.common.collect.Maps;

/**
 * Player controller ({@link IPlayerController}) based on keyboard events.
 */
public final class AWTKeyboardController implements IPlayerController, IPlayerController2
{
    /**
     * Global lock for accessing static data structures.
     */
    private final static Object lock = new Object();

    /**
     * All the pressed (and unreleased) key codes, the most recent codes appear at the end
     * of the list.
     */
    private static ArrayList<Integer> pressedCodes = new ArrayList<Integer>();

    /**
     * Counter for keystrokes. This is not strictly necessary, but useful to track keys
     * whose detection should always be triggered (bomb drop), even in between controller
     * calls.
     */
    private static HashMap<Integer, Integer> keystrokes = Maps.newHashMap();

    /**
     * Hook into keyboard manager.
     */
    static
    {
        keyboardHook();
    }

    /*
     * VK_ (virtual key codes) this controller recognizes.
     */

    private final Integer vk_left;
    private final Integer vk_right;
    private final Integer vk_up;
    private final Integer vk_down;
    private final Integer vk_bomb;

    /**
     * Creates a new keyboard controller bound to the set of virtual key codes.
     */
    public AWTKeyboardController(int vk_up, int vk_down, int vk_left, int vk_right,
        int vk_bomb)
    {
        this.vk_left = vk_left;
        this.vk_right = vk_right;
        this.vk_up = vk_up;
        this.vk_down = vk_down;
        this.vk_bomb = vk_bomb;
    }

    /*
     * 
     */
    public boolean dropsBomb()
    {
        return getState().dropsBomb;
    }

    /*
     * 
     */
    public Direction getCurrent()
    {
        return getState().direction;
    }

    /*
     * 
     */
    @Override
    public ControllerState getState()
    {
        synchronized (lock)
        {
            boolean dropsBomb = false;
            Direction direction = null;

            for (int index = pressedCodes.size() - 1; index >= 0; index--)
            {
                if (pressedCodes.get(index) == vk_bomb) dropsBomb = true;
            }
            
            for (int index = pressedCodes.size() - 1; index >= 0; index--)
            {
                final int i = pressedCodes.get(index);

                if (i == vk_left) direction = Direction.LEFT;
                else if (i == vk_right) direction = Direction.RIGHT;
                else if (i == vk_down) direction = Direction.DOWN;
                else if (i == vk_up) direction = Direction.UP;
            }

            /*
             * We allow the keyboard controller to send "indefinite" state validity,
             * otherwise (even on the local wireless network), delayed packets cause weird
             * jerking of players.
             */
            final int validFrames = 0;
            return new ControllerState(direction, dropsBomb, validFrames);
        }
    }

    /**
     * Returns "default" keyboard layout for a player numbered <code>num</code>.
     */
    public static IPlayerController getDefaultKeyboardController(int num)
    {
        switch (num)
        {
            case 0:
                return new AWTKeyboardController(KeyEvent.VK_UP, KeyEvent.VK_DOWN,
                    KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_CONTROL);
            case 1:
                return new AWTKeyboardController(KeyEvent.VK_R, KeyEvent.VK_F,
                    KeyEvent.VK_D, KeyEvent.VK_G, KeyEvent.VK_Z);
        }
    
        throw new RuntimeException("No default keyboard mapping for player: " + num);
    }

    /**
     * Returns configured keyboard layout for a player numbered <code>num</code> as obtained from specified <code>config</code>.
     */
    public static IPlayerController getKeyboardController(int num, Configuration config)
    {
        final int keyOffset = num * KeyBinding.values().length;
        try
        {
            return new AWTKeyboardController(
                    config.keyBindings[keyOffset],
                    config.keyBindings[keyOffset + 1],
                    config.keyBindings[keyOffset + 2],
                    config.keyBindings[keyOffset + 3],
                    config.keyBindings[keyOffset + 4]);
        } catch (IndexOutOfBoundsException e)
        {
            throw new RuntimeException("No keyboard mapping for player: " + num);
        }
    }

    /*
     * 
     */
    public static void keyboardHook()
    {
        final KeyboardFocusManager km = KeyboardFocusManager
            .getCurrentKeyboardFocusManager();
        
        km.addKeyEventDispatcher(new KeyEventDispatcher()
        {
            public boolean dispatchKeyEvent(KeyEvent e)
            {
                final Integer code = e.getKeyCode();
                synchronized (lock)
                {
                    switch (e.getID())
                    {
                        case KeyEvent.KEY_PRESSED:
                            if (!pressedCodes.contains(code))
                            {
                                pressedCodes.add(code);
                                keystrokes.put(code, 1 + getKeystrokeCount(code));
                            }
                            break;

                        case KeyEvent.KEY_RELEASED:
                            pressedCodes.remove(code);
                            break;
                    }
                }

                /*
                 * We used to consume keyboard events, but since this hook is global
                 * and static, it consumes everything -- even keyboard actions from
                 * input components... At the same time we _do_ want a global
                 * keyboard grabber because we want our players to move regardless of
                 * the component with active focus. 
                 */

                /*
                 * e.consume();
                 * return true;
                 */

                return false;
            }
        });
    }

    /*
     * 
     */
    static int getKeystrokeCount(Integer code)
    {
        final Integer i = keystrokes.get(code);
        return i == null ? 0 : i;
    }
}
