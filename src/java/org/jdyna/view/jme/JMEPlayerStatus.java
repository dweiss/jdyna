package org.jdyna.view.jme;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.jdyna.GameConfiguration;
import org.jdyna.IPlayerSprite;
import org.jdyna.view.swing.StatusType;

import com.google.common.collect.Maps;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;

@SuppressWarnings("serial")
public class JMEPlayerStatus extends Node
{
    /**
     * Game configuration.
     */
    private GameConfiguration conf;
    
    /**
     * List of all monitoring bonuses/diseases.
     */
    private LinkedHashMap<StatusType, JMESingleStatus> statuses = Maps.newLinkedHashMap();

    /**
     * Create status bar for the 3d view. Odd @statusIndex value place status in left
     * side, even in right side.
     */
    public JMEPlayerStatus(GameConfiguration conf, Renderer renderer, int statusIndex)
    {
        this.conf = conf;
        int position = 0;

        for (StatusType st : Arrays.asList(
            StatusType.LIVES,
            StatusType.BOMBS,
            StatusType.BOMB_RANGE,
            StatusType.MAX_RANGE,
            StatusType.AHMED,
            StatusType.SPEED_UP,
            StatusType.CRATE_WALKING,
            StatusType.BOMB_WALKING,
            StatusType.IMMORTALITY,
            StatusType.DIARRHOEA,
            StatusType.NO_BOMBS,
            StatusType.SLOW_DOWN,
            StatusType.CTRL_REVERSE
        ))
        {
            statuses.put(st, new JMESingleStatus(renderer, st, position++, statusIndex));
        }

        for (JMESingleStatus st : statuses.values())
        {
            attachChild(st);
        }
    }

    /**
     * Update the values of all statuses.
     */
    public void update(int frame, IPlayerSprite player)
    {
        statuses.get(StatusType.LIVES).update(player.getLifeCount());
        statuses.get(StatusType.BOMBS).update(player.getBombCount());
        statuses.get(StatusType.BOMB_RANGE).update(player.getBombRange());
        statuses.get(StatusType.MAX_RANGE).update(ifActiveCounter(player.getMaxRangeEndsAtFrame(), frame));
        statuses.get(StatusType.AHMED).update(player.isAhmed() ? 1 : -1);
        statuses.get(StatusType.SPEED_UP).update(ifActiveCounter(player.getSpeedUpEndsAtFrame(), frame));
        statuses.get(StatusType.CRATE_WALKING).update(ifActiveCounter(player.getCrateWalkingEndsAtFrame(), frame));
        statuses.get(StatusType.BOMB_WALKING).update(ifActiveCounter(player.getBombWalkingEndsAtFrame(), frame));
        statuses.get(StatusType.IMMORTALITY).update(ifActiveCounter(player.getImmortalityEndsAtFrame(), frame));
        statuses.get(StatusType.DIARRHOEA).update(ifActiveCounter(player.getDiarrheaEndsAtFrame(), frame));
        statuses.get(StatusType.NO_BOMBS).update(ifActiveCounter(player.getNoBombsEndsAtFrame(), frame));
        statuses.get(StatusType.SLOW_DOWN).update(ifActiveCounter(player.getSlowDownEndsAtFrame(), frame));
        statuses.get(StatusType.CTRL_REVERSE).update(ifActiveCounter(player.getControllerReverseEndsAtFrame(), frame));
    }

    /**
     * Checks if specific counter is active and if so, returns seconds left, otherwise
     * returns -1.
     */
    private int ifActiveCounter(int counterMaxFrame, int frame)
    {
        if (counterMaxFrame < 0 || counterMaxFrame - frame < 0) return -1;
        else return secondsLeft(counterMaxFrame, frame);
    }

    /**
     * Calculates the remaining seconds of the collected bonus/disease.
     */
    private int secondsLeft(int counterMaxFrame, int frame)
    {
        int fps = this.conf.DEFAULT_FRAME_RATE; 
        return 1 + ((counterMaxFrame - frame) / fps);
    }
}
