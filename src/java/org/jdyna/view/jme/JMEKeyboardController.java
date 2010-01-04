package org.jdyna.view.jme;

import org.jdyna.IPlayerController;

import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;

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
}
