package com.dawidweiss.dyna;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import com.google.common.collect.Lists;

/**
 * {@link IController} based on keyboard events.
 */
public class KeyboardController implements IController
{
    /**
     * All the pressed (and unreleased) key codes, the most
     * recently pressed codes appear at the end of the list.
     */
    private static ArrayList<Integer> pressedCodes = Lists.newArrayList();
    
    /**
     * Hook into events manager.
     */
    static
    {
        keyboardHook();
    }

    /*
     * VK_ (virtual key codes) for directions this controller recognizes.
     */

    private final Integer vk_left;
    private final Integer vk_right;
    private final Integer vk_up;
    private final Integer vk_down;

    /*
     * 
     */
    public KeyboardController(int vk_up, int vk_down, int vk_left, int vk_right)
    {
        this.vk_left = vk_left;
        this.vk_right = vk_right;
        this.vk_up = vk_up;
        this.vk_down = vk_down;
    }

    public Direction getCurrent()
    {
        synchronized (pressedCodes)
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
                synchronized (pressedCodes)
                {
                    switch (e.getID())
                    {
                        case KeyEvent.KEY_PRESSED:
                            if (!pressedCodes.contains(code))
                            {
                                pressedCodes.add(code);
                            }
                            break;

                        case KeyEvent.KEY_RELEASED:
                            pressedCodes.remove(code);
                    }
                }

                e.consume();
                return true;
            }
        });
    }
}
