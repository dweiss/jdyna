package com.dawidweiss.dyna;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.EnumSet;
import java.util.HashSet;

import com.google.common.collect.Sets;

/**
 * {@link IController} based on keyboard events.
 */
public class KeyboardController implements IController
{
    /**
     * All the pressed (and unreleased) key codes. This set is shared by all
     * implementations of this class.
     */
    private static HashSet<Integer> pressedCodes = Sets.newHashSet();

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

    /**
     * Current set of signals.
     */
    private final EnumSet<IController.Direction> signals = EnumSet
        .noneOf(IController.Direction.class);

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

    public EnumSet<Direction> getCurrent()
    {
        synchronized (pressedCodes)
        {
            signals.clear();
            if (pressedCodes.contains(vk_left)) signals.add(Direction.LEFT);
            if (pressedCodes.contains(vk_right)) signals.add(Direction.RIGHT);
            if (pressedCodes.contains(vk_up)) signals.add(Direction.UP);
            if (pressedCodes.contains(vk_down)) signals.add(Direction.DOWN);
        }

        return signals;
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
                final int code = e.getKeyCode();
                synchronized (pressedCodes)
                {
                    switch (e.getID())
                    {
                        case KeyEvent.KEY_PRESSED:
                            pressedCodes.add(code);
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
