package org.jdyna.view.jme;

import org.jdyna.IPlayerController;
import org.jdyna.frontend.swing.Configuration;
import org.jdyna.frontend.swing.Configuration.KeyBinding;

import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jmex.awt.input.AWTKeyInput;

public class JMEKeyboardController implements IPlayerController
{
    private static final KeyBindingManager km = KeyBindingManager.getKeyBindingManager();
    private final String upCommand;
    private final String downCommand;
    private final String leftCommand;
    private final String rightCommand;
    private final String bombCommand;

    public JMEKeyboardController(int playerNum, int vk_up, int vk_down, int vk_left,
        int vk_right, int vk_bomb)
    {
        upCommand = playerNum + "_up";
        downCommand = playerNum + "_down";
        leftCommand = playerNum + "_left";
        rightCommand = playerNum + "_right";
        bombCommand = playerNum + "_bomb";

        km.set(upCommand, vk_up);
        km.set(downCommand, vk_down);
        km.set(leftCommand, vk_left);
        km.set(rightCommand, vk_right);
        km.set(bombCommand, vk_bomb);
    }

    @Override
    public boolean dropsBomb()
    {
        return km.isValidCommand(bombCommand);
    }

    @Override
    public Direction getCurrent()
    {
        Direction dir = null;

        if (km.isValidCommand(upCommand)) dir = Direction.UP;
        else if (km.isValidCommand(downCommand)) dir = Direction.DOWN;
        else if (km.isValidCommand(leftCommand)) dir = Direction.LEFT;
        else if (km.isValidCommand(rightCommand)) dir = Direction.RIGHT;

        return dir;
    }

    /**
     * Returns "default" keyboard layout for a player numbered <code>num</code>.
     */
    public static IPlayerController getDefaultKeyboardController(int num)
    {
        switch (num)
        {
            case 0:
                return new JMEKeyboardController(num,
                    KeyInput.KEY_UP,
                    KeyInput.KEY_DOWN, 
                    KeyInput.KEY_LEFT, 
                    KeyInput.KEY_RIGHT,
                    KeyInput.KEY_RCONTROL);
            case 1:
                return new JMEKeyboardController(num, 
                    KeyInput.KEY_R,
                    KeyInput.KEY_F, 
                    KeyInput.KEY_D, 
                    KeyInput.KEY_G, 
                    KeyInput.KEY_Z);
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
            return new JMEKeyboardController(num,
                    AWTKeyInput.toInputCode(config.keyBindings[keyOffset]),
                    AWTKeyInput.toInputCode(config.keyBindings[keyOffset + 1]),
                    AWTKeyInput.toInputCode(config.keyBindings[keyOffset + 2]),
                    AWTKeyInput.toInputCode(config.keyBindings[keyOffset + 3]),
                    AWTKeyInput.toInputCode(config.keyBindings[keyOffset + 4]));
        } catch (IndexOutOfBoundsException e)
        {
            throw new RuntimeException("No keyboard mapping for player: " + num);
        }
    }
}
