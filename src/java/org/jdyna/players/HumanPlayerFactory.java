package org.jdyna.players;

import java.awt.event.KeyEvent;

import org.jdyna.*;
import org.jdyna.frontend.swing.Configuration.ViewType;
import org.jdyna.input.KeyboardController;
import org.jdyna.view.jme.JMEKeyboardController;

/**
 * Factory of human players bound to the keyboard controller. Not for real use, I guess.
 */
public final class HumanPlayerFactory implements IPlayerFactory
{
    private final IPlayerController controller;
    private final String playerName;

    public HumanPlayerFactory()
    {
        this(HumanPlayerFactory.getDefaultKeyboardController(0, ViewType.SWING_VIEW));
    }

    public HumanPlayerFactory(IPlayerController controller)
    {
        this(controller, "Human");
    }

    public HumanPlayerFactory(IPlayerController controller, String defaultPlayerName)
    {
        this.controller = controller;
        this.playerName = defaultPlayerName;
    }

    /**
     * Returns an implementation of {@link IPlayerController2} and {@link IPlayerController}.
     */
    @Override
    public IPlayerController getController(String playerName)
    {
        return controller;
    }

    @Override
    public String getDefaultPlayerName()
    {
        return playerName;
    }

    @Override
    public String getVendorName()
    {
        return "Dawid Weiss";
    }

    /**
     * Returns "default" keyboard layout for a player numbered <code>num</code>.
     */
    public static IPlayerController getDefaultKeyboardController(int num, ViewType viewType)
    {
        switch (viewType)
        {
            case SWING_VIEW:
                switch (num)
                {
                    case 0:

                        return new KeyboardController(KeyEvent.VK_UP, KeyEvent.VK_DOWN,
                            KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_CONTROL);
                    case 1:
                        return new KeyboardController(KeyEvent.VK_R, KeyEvent.VK_F,
                            KeyEvent.VK_D, KeyEvent.VK_G, KeyEvent.VK_Z);
                }

            case JME_VIEW:
                switch (num)
                {
                    case 0:
                        return new JMEKeyboardController(num, KeyEvent.VK_UP,
                            KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
                            KeyEvent.VK_CONTROL);
                    case 1:
                        return new JMEKeyboardController(num, KeyEvent.VK_R,
                            KeyEvent.VK_F, KeyEvent.VK_D, KeyEvent.VK_G, KeyEvent.VK_Z);
                }

        }
        throw new RuntimeException("No default keyboard mapping for player: " + num);
    }
}
