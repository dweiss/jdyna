package org.jdyna.view.jme;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import org.jdyna.IPlayerController;

import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;

public class JMEKeyboardController implements IPlayerController
{
    private static final KeyBindingManager km = KeyBindingManager.getKeyBindingManager();
    private int playerNum;

    private static final HashMap<Integer, Integer> awtJmeKeyMap = new HashMap<Integer, Integer>()
    {
        {
            put(KeyEvent.VK_LEFT, KeyInput.KEY_LEFT);
            put(KeyEvent.VK_RIGHT, KeyInput.KEY_RIGHT);
            put(KeyEvent.VK_UP, KeyInput.KEY_UP);
            put(KeyEvent.VK_DOWN, KeyInput.KEY_DOWN);
            put(KeyEvent.VK_CONTROL, KeyInput.KEY_LCONTROL);
            put(KeyEvent.VK_A, KeyInput.KEY_A);
            put(KeyEvent.VK_B, KeyInput.KEY_B);
            put(KeyEvent.VK_C, KeyInput.KEY_C);
            put(KeyEvent.VK_D, KeyInput.KEY_D);
            put(KeyEvent.VK_E, KeyInput.KEY_E);
            put(KeyEvent.VK_F, KeyInput.KEY_F);
            put(KeyEvent.VK_G, KeyInput.KEY_G);
            put(KeyEvent.VK_H, KeyInput.KEY_H);
            put(KeyEvent.VK_I, KeyInput.KEY_I);
            put(KeyEvent.VK_J, KeyInput.KEY_J);
            put(KeyEvent.VK_K, KeyInput.KEY_K);
            put(KeyEvent.VK_L, KeyInput.KEY_L);
            put(KeyEvent.VK_M, KeyInput.KEY_M);
            put(KeyEvent.VK_N, KeyInput.KEY_N);
            put(KeyEvent.VK_O, KeyInput.KEY_O);
            put(KeyEvent.VK_P, KeyInput.KEY_P);
            put(KeyEvent.VK_Q, KeyInput.KEY_Q);
            put(KeyEvent.VK_R, KeyInput.KEY_R);
            put(KeyEvent.VK_S, KeyInput.KEY_S);
            put(KeyEvent.VK_T, KeyInput.KEY_T);
            put(KeyEvent.VK_U, KeyInput.KEY_U);
            put(KeyEvent.VK_V, KeyInput.KEY_V);
            put(KeyEvent.VK_W, KeyInput.KEY_W);
            put(KeyEvent.VK_X, KeyInput.KEY_X);
            put(KeyEvent.VK_Y, KeyInput.KEY_Y);
            put(KeyEvent.VK_Z, KeyInput.KEY_Z);
            put(KeyEvent.VK_0, KeyInput.KEY_0);
            put(KeyEvent.VK_1, KeyInput.KEY_1);
            put(KeyEvent.VK_2, KeyInput.KEY_2);
            put(KeyEvent.VK_3, KeyInput.KEY_3);
            put(KeyEvent.VK_4, KeyInput.KEY_4);
            put(KeyEvent.VK_5, KeyInput.KEY_5);
            put(KeyEvent.VK_6, KeyInput.KEY_6);
            put(KeyEvent.VK_7, KeyInput.KEY_7);
            put(KeyEvent.VK_8, KeyInput.KEY_8);
            put(KeyEvent.VK_9, KeyInput.KEY_9);
        }
    };

    public JMEKeyboardController(int playerNum, int vk_up, int vk_down, int vk_left,
        int vk_right, int vk_bomb)
    {
        km.set(playerNum + "_up", awtJmeKeyMap.get(vk_up));
        km.set(playerNum + "_down", awtJmeKeyMap.get(vk_down));
        km.set(playerNum + "_left", awtJmeKeyMap.get(vk_left));
        km.set(playerNum + "_right", awtJmeKeyMap.get(vk_right));
        km.set(playerNum + "_bomb", awtJmeKeyMap.get(vk_bomb));
        this.playerNum = playerNum;
    }

    @Override
    public boolean dropsBomb()
    {

        return km.isValidCommand(this.playerNum + "_bomb");
    }

    @Override
    public Direction getCurrent()
    {

        Direction dir = null;

        if (km.isValidCommand(this.playerNum + "_up")) dir = Direction.UP;
        else if (km.isValidCommand(this.playerNum + "_down")) dir = Direction.DOWN;
        else if (km.isValidCommand(this.playerNum + "_left")) dir = Direction.LEFT;
        else if (km.isValidCommand(this.playerNum + "_right")) dir = Direction.RIGHT;

        return dir;
    }

}
