package org.jdyna.view.swing;

import static org.jdyna.CellType.*;
import static org.jdyna.view.swing.StatusType.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.apache.commons.lang.StringUtils;
import org.jdyna.*;
import org.jdyna.view.resources.ImageUtilities;
import org.jdyna.view.resources.Images;

import com.google.common.collect.Maps;

/**
 * {@link JPanel} displaying the status information (current bonuses,
 * lives, etc.) for a single player in the game.
 */
@SuppressWarnings("serial")
public class PlayerStatusPanel extends JPanel implements IGameEventListener
{
    /**
     * Game configuration.
     */
    private GameConfiguration conf;

    /**
     * Images.
     */
    private final Images images;

    /**
     * List of statistics to display.
     */
    private Map<StatusType, Status> statuses;

    /**
     * Name of the player tracked by this panel.
     */
    private final String playerName;

    /**
     * 
     */
    public PlayerStatusPanel(Images images, String playerName)
    {
        this.images = images;
        this.playerName = playerName;
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
        gbc.insets = new Insets(0, 1, 0, 0);

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
        for (Status s : Arrays.asList(new Status(LIVES, lifeCountIcon, false),
            new Status(BOMBS, getCellImage(CELL_BONUS_BOMB), false), 
            new Status(BOMB_RANGE, getCellImage(CELL_BONUS_RANGE), false), 
            new Status(MAX_RANGE, getCellImage(CELL_BONUS_MAXRANGE), true), 
            new Status(AHMED, getCellImage(CELL_BONUS_AHMED), false), 
            new Status(SPEED_UP, getCellImage(CELL_BONUS_SPEED_UP), true), 
            new Status(CRATE_WALKING, getCellImage(CELL_BONUS_CRATE_WALKING), true), 
            new Status(BOMB_WALKING, getCellImage(CELL_BONUS_BOMB_WALKING), true),
            new Status(IMMORTALITY, getCellImage(CELL_BONUS_IMMORTALITY), true),
            new Status(DIARRHOEA, getCellImage(CELL_BONUS_DIARRHEA), true),
            new Status(NO_BOMBS, getCellImage(CELL_BONUS_NO_BOMBS), true),
            new Status(SLOW_DOWN, getCellImage(CELL_BONUS_SLOW_DOWN), true),
            new Status(CTRL_REVERSE, getCellImage(CELL_BONUS_CONTROLLER_REVERSE), true)))
        {
            statuses.put(s.field, s);
        }

        // Set insets and add all statuses panels.
        for (Map.Entry<StatusType, Status> e : statuses.entrySet())
        {
            switch (e.getKey())
            {
                case MAX_RANGE:
                case DIARRHOEA:
                    add(new JSeparator(), gbc);
                    gbc.gridx++;
                    break;

                default:
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

        for (int playerIndex = 0; playerIndex < players.size(); playerIndex++)
        {
            final IPlayerSprite player = players.get(playerIndex);

            if (StringUtils.equals(playerName, player.getName()))
            {
                statuses.get(LIVES).updateValue(player.getLifeCount());
                statuses.get(BOMBS).updateValue(player.getBombCount());
                statuses.get(BOMB_RANGE).updateValue(player.getBombRange());

                statuses.get(MAX_RANGE).updateValue(
                    ifActiveCounter(player.getMaxRangeEndsAtFrame(), frame));
                statuses.get(AHMED).updateValue(player.isAhmed() ? 1 : -1);
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
        }
    }

    /**
     * Checks if specific counter is active and if so, returns seconds left, otherwise
     * returns -1.
     */
    private int ifActiveCounter(int counterMaxFrame, int frame)
    {
        if (counterMaxFrame < 0 || counterMaxFrame - frame < 0) 
            return -1;
        else 
            return secondsLeft(counterMaxFrame, frame);
    }

    /**
     * Calculates the remaining seconds of the collected bonus/disease.
     */
    private int secondsLeft(int counterMaxFrame, int frame)
    {
        return 1 + ((counterMaxFrame - frame) / conf.DEFAULT_FRAME_RATE);
    }

    /**
     * Return the first image for a given cell.
     */
    private BufferedImage getCellImage(CellType cell)
    {
        BufferedImage [] cellImages = images.getCellImage(cell);
        if (cellImages == null || cellImages.length == 0)
            throw new RuntimeException("Missing image for cell: " + cell);
        return cellImages[0];
    }
}
