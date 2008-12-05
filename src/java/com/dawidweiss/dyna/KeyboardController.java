package com.dawidweiss.dyna;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.*;

import com.google.common.collect.Maps;

/**
 * {@link IController} based on keyboard events. 
 */
public final class KeyboardController implements IController
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
     * Counter for keystrokes. This is not strictly necessary, but useful
     * to track keys whose detection should always be triggered (bomb drop),
     * even in between controller calls.
     */
    private static HashMap<Integer, Integer> keystrokes = Maps.newHashMap();
    
    /**
     * Hook into events manager.
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
    public KeyboardController(int vk_up, int vk_down, int vk_left, int vk_right,
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
        synchronized (lock)
        {
            for (int index = pressedCodes.size() - 1; index >= 0; index--)
            {
                if (pressedCodes.get(index) == vk_bomb) return true;
            }
        }

        return false;
    }

    /*
     * 
     */
    public Direction getCurrent()
    {
        synchronized (lock)
        {
            for (int index = pressedCodes.size() - 1; index >= 0; index--)
            {
                final int i = pressedCodes.get(index);

                if (i == vk_left) return Direction.LEFT;
                if (i == vk_right) return Direction.RIGHT;
                if (i == vk_down) return Direction.DOWN;
                if (i == vk_up) return Direction.UP;
            }
        }

        // No direction key pressed.
        return null;
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

                e.consume();
                return true;
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
