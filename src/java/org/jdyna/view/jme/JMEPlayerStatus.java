package org.jdyna.view.jme;

import java.awt.Font;
import java.util.Arrays;
import java.util.LinkedHashMap;

import org.jdyna.CellType;
import org.jdyna.GameConfiguration;
import org.jdyna.IPlayerSprite;

import com.google.common.collect.Maps;
import com.jme.scene.Node;
import com.jmex.font3d.Font3D;

@SuppressWarnings("serial")
public class JMEPlayerStatus extends Node
{
    /**
     * Game configuration.
     */
    private GameConfiguration conf;
    
    private int count = 0;
    private LinkedHashMap<CellType, JMESingleStstus> statuses;

    public JMEPlayerStatus()
    {
        conf = new GameConfiguration();
        statuses = Maps.newLinkedHashMap();
        Font3D font3d = null;
        try {
            Font font = new Font("Arial", Font.BOLD, 16);
            font3d = new Font3D(font, 0.01f, true, true, true);
        } catch (Exception e) {
            // ignore
        }
        
        for (CellType ct : Arrays.asList(
            CellType.CELL_BONUS_LIFE,
            CellType.CELL_BONUS_BOMB,
            CellType.CELL_BONUS_RANGE,
            CellType.CELL_BONUS_MAXRANGE,
            CellType.CELL_BONUS_AHMED,
            CellType.CELL_BONUS_SPEED_UP,
            CellType.CELL_BONUS_CRATE_WALKING,
            CellType.CELL_BONUS_BOMB_WALKING,
            CellType.CELL_BONUS_IMMORTALITY,
            CellType.CELL_BONUS_DIARRHEA,
            CellType.CELL_BONUS_NO_BOMBS,
            CellType.CELL_BONUS_SLOW_DOWN,
            CellType.CELL_BONUS_CONTROLLER_REVERSE
        ))
        {
            statuses.put(ct, new JMESingleStstus(ct, count++, font3d));
        }

        for (JMESingleStstus st : statuses.values())
        {
            attachChild(st);
        }
    }

    public void update(int frame, IPlayerSprite p)
    {
        p.getType();
        statuses.get(CellType.CELL_BONUS_LIFE).update(p.getLifeCount());
        statuses.get(CellType.CELL_BONUS_BOMB).update(p.getBombCount());
        statuses.get(CellType.CELL_BONUS_RANGE).update(p.getBombRange());
        statuses.get(CellType.CELL_BONUS_MAXRANGE).update(ifActiveCounter(p.getMaxRangeEndsAtFrame(), frame));
        statuses.get(CellType.CELL_BONUS_AHMED).update(p.isAhmed() ? 1 : -1);
        statuses.get(CellType.CELL_BONUS_SPEED_UP).update(ifActiveCounter(p.getSpeedUpEndsAtFrame(), frame));
        statuses.get(CellType.CELL_BONUS_CRATE_WALKING).update(ifActiveCounter(p.getCrateWalkingEndsAtFrame(), frame));
        statuses.get(CellType.CELL_BONUS_BOMB_WALKING).update(ifActiveCounter(p.getBombWalkingEndsAtFrame(), frame));
        statuses.get(CellType.CELL_BONUS_IMMORTALITY).update(ifActiveCounter(p.getImmortalityEndsAtFrame(), frame));
        statuses.get(CellType.CELL_BONUS_DIARRHEA).update(ifActiveCounter(p.getDiarrheaEndsAtFrame(), frame));
        statuses.get(CellType.CELL_BONUS_NO_BOMBS).update(ifActiveCounter(p.getNoBombsEndsAtFrame(), frame));
        statuses.get(CellType.CELL_BONUS_SLOW_DOWN).update(ifActiveCounter(p.getSlowDownEndsAtFrame(), frame));
        statuses.get(CellType.CELL_BONUS_CONTROLLER_REVERSE).update(ifActiveCounter(p.getControllerReverseEndsAtFrame(), frame));
    }

    private int ifActiveCounter(int counterMaxFrame, int frame)
    {
        if (counterMaxFrame < 0 || counterMaxFrame - frame < 0) return -1;
        else return secondsLeft(counterMaxFrame, frame);
    }

    private int secondsLeft(int counterMaxFrame, int frame)
    {
        return 1 + ((counterMaxFrame - frame) / conf.DEFAULT_FRAME_RATE);
    }
}
