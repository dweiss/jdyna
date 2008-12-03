package com.dawidweiss.dyna;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.HashSet;

public class KeyboardPlayerController
{
    public static void main(String [] args)
    {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(
            new KeyEventDispatcher()
            {
                final HashSet<Integer> pressedCodes = new HashSet<Integer>();

                public boolean dispatchKeyEvent(KeyEvent e)
                {
                    final int code = e.getKeyCode();

                    switch (e.getID())
                    {
                        case KeyEvent.KEY_PRESSED:
                            if (!pressedCodes.contains(code))
                            {
                                pressedCodes.add(code);
                                System.out.println(e.toString());
                            }
                            break;
                        case KeyEvent.KEY_RELEASED:
                            pressedCodes.remove(code);
                    }

                    e.consume();
                    return true;
                }
            });
    }
}
