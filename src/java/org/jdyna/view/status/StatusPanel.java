package org.jdyna.view.status;

import static org.jdyna.CellType.*;
import static org.jdyna.view.status.StatusField.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

import javax.swing.JPanel;

import org.jdyna.*;
import org.jdyna.view.resources.ImageUtilities;
import org.jdyna.view.resources.Images;

import com.google.common.collect.Maps;

@SuppressWarnings("serial")
public class StatusPanel extends JPanel implements IGameEventListener
{
    /**
     * Game configuration.
     */
    private GameConfiguration conf;

    /**
     * Images.
     */
    private Images images;

    /**
     * List of statistics to display.
     */
    private Map<StatusField, Status> statuses;

    /**
     * 
     */
    public StatusPanel(Images images, GraphicsConfiguration conf)
    {
        // TODO: determine player.
        this.images = images.createCompatible(conf);
        initializeComponents();
    }

    /**
     * 
     */
    private void initializeComponents()
    {
        setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(1, 1, 1, 1);

        // Load an icon for the life counter.
        final BufferedImage lifeCountIcon;
        try
        {
            lifeCountIcon = ImageUtilities.loadResourceImage("icons/life.png");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        // Linked hash map of statuses initialization.
        statuses = Maps.newLinkedHashMap();
        for (Status s : Arrays.asList(
            new Status(LIVES, lifeCountIcon, "?"), 
            new Status(BOMBS, getCellImage(CELL_BONUS_BOMB), "?"), 
            new Status(BOMB_RANGE, getCellImage(CELL_BONUS_RANGE), "?"), 
            new Status(MAX_RANGE, getCellImage(CELL_BONUS_MAXRANGE), "?"), 
            new Status(AHMED, getCellImage(CELL_BONUS_AHMED), "?"), 
            new Status(SPEED_UP, getCellImage(CELL_BONUS_SPEED_UP), "?"), 
            new Status(CRATE_WALKING, getCellImage(CELL_BONUS_CRATE_WALKING), "?"), 
            new Status(BOMB_WALKING, getCellImage(CELL_BONUS_BOMB_WALKING), "?"), 
            new Status(IMMORTALITY, getCellImage(CELL_BONUS_IMMORTALITY), "?"), 
            new Status(DIARRHOEA, getCellImage(CELL_BONUS_DIARRHEA), "?"), 
            new Status(NO_BOMBS, getCellImage(CELL_BONUS_NO_BOMBS), "?"), 
            new Status(SLOW_DOWN, getCellImage(CELL_BONUS_SLOW_DOWN), "?"), 
            new Status(CTRL_REVERSE, getCellImage(CELL_BONUS_CONTROLLER_REVERSE), "?")))
        {
            statuses.put(s.field, s);
        }

        // Set insets.
        for (Map.Entry<StatusField, Status> e : statuses.entrySet())
        {
            switch (e.getKey())
            {
                case MAX_RANGE:
                case DIARRHOEA:
                    gbc.insets = new Insets(1, 5, 1, 1);
                    break;

                default:
                    gbc.insets = new Insets(1, 1, 1, 1);
                    break;
            }

            add(e.getValue(), gbc);
            gbc.gridx++;
        }
    }

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        for (GameEvent e : events)
        {
            if (e.type == GameEvent.Type.GAME_START)
            {
                GameStartEvent se = (GameStartEvent) e;
                this.conf = se.getConfiguration();
            }
            if (e.type == GameEvent.Type.GAME_STATE)
            {
                updatePanel((GameStateEvent) e, frame);
            }
        }
    }

    public void updatePanel(GameStateEvent gameState, int frame)
    {
        final List<? extends IPlayerSprite> players = gameState.getPlayers();
        if (players.size() <= 0) return;

        final IPlayerSprite player = players.get(0);

        statuses.get(LIVES).updateValue(Integer.toString(player.getLifeCount()));
        statuses.get(BOMBS).updateValue(Integer.toString(player.getBombCount()));
        statuses.get(BOMB_RANGE).updateValue(
            (player.getBombRange() == Integer.MAX_VALUE) ? "\u221E" : Integer
                .toString(player.getBombRange()));

        statuses.get(MAX_RANGE).updateValue(
            ifActiveCounter(player.getMaxRangeEndsAtFrame(), frame));
        statuses.get(AHMED).updateValue(player.isAhmed() ? "1" : "");
        statuses.get(SPEED_UP).updateValue(
            ifActiveCounter(player.getSpeedUpEndsAtFrame(), frame));
        statuses.get(CRATE_WALKING).updateValue(
            ifActiveCounter(player.getCrateWalkingEndsAtFrame(), frame));
        statuses.get(BOMB_WALKING).updateValue(
            ifActiveCounter(player.getBombWalkingEndsAtFrame(), frame));
        statuses.get(IMMORTALITY).updateValue(
            ifActiveCounter(player.getImmortalityEndsAtFrame(), frame));

        statuses.get(DIARRHOEA).updateValue(
            ifActiveCounter(player.getDiarrheaEndsAtFrame(), frame));
        statuses.get(NO_BOMBS).updateValue(
            ifActiveCounter(player.getNoBombsEndsAtFrame(), frame));
        statuses.get(SLOW_DOWN).updateValue(
            ifActiveCounter(player.getSlowDownEndsAtFrame(), frame));
        statuses.get(CTRL_REVERSE).updateValue(
            ifActiveCounter(player.getControllerReverseEndsAtFrame(), frame));
    }

    /**
     * Checks if <code>counterMaxFrame</code> is active and if so, returns <code>
     */
    private String ifActiveCounter(int counterMaxFrame, int frame)
    {
        if (counterMaxFrame < 0 || counterMaxFrame - frame < 0) return "";
        else return secondsLeft(counterMaxFrame, frame);
    }

    /**
     * Calculates the remaining seconds of the collected bonus/disease and returns it as a
     * text.
     */
    private String secondsLeft(int counterMaxFrame, int frame)
    {
        return Integer.toString((counterMaxFrame - frame) / conf.DEFAULT_FRAME_RATE + 1);
    }

    /**
     * Return the first image for a given cell.
     */
    private BufferedImage getCellImage(CellType cell)
    {
        BufferedImage [] cellImages = images.getCellImage(cell);
        if (cellImages == null || cellImages.length == 0)
        {
            return null;
        }
        return cellImages[0];
    }
}
