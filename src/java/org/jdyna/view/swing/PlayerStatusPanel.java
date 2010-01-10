package org.jdyna.view.swing;

import static org.jdyna.CellType.CELL_BONUS_AHMED;
import static org.jdyna.CellType.CELL_BONUS_BOMB;
import static org.jdyna.CellType.CELL_BONUS_BOMB_WALKING;
import static org.jdyna.CellType.CELL_BONUS_CONTROLLER_REVERSE;
import static org.jdyna.CellType.CELL_BONUS_CRATE_WALKING;
import static org.jdyna.CellType.CELL_BONUS_DIARRHEA;
import static org.jdyna.CellType.CELL_BONUS_IMMORTALITY;
import static org.jdyna.CellType.CELL_BONUS_MAXRANGE;
import static org.jdyna.CellType.CELL_BONUS_NO_BOMBS;
import static org.jdyna.CellType.CELL_BONUS_RANGE;
import static org.jdyna.CellType.CELL_BONUS_SLOW_DOWN;
import static org.jdyna.CellType.CELL_BONUS_SPEED_UP;
import static org.jdyna.view.swing.StatusType.AHMED;
import static org.jdyna.view.swing.StatusType.BOMBS;
import static org.jdyna.view.swing.StatusType.BOMB_RANGE;
import static org.jdyna.view.swing.StatusType.BOMB_WALKING;
import static org.jdyna.view.swing.StatusType.CRATE_WALKING;
import static org.jdyna.view.swing.StatusType.CTRL_REVERSE;
import static org.jdyna.view.swing.StatusType.DIARRHOEA;
import static org.jdyna.view.swing.StatusType.IMMORTALITY;
import static org.jdyna.view.swing.StatusType.LIVES;
import static org.jdyna.view.swing.StatusType.MAX_RANGE;
import static org.jdyna.view.swing.StatusType.NO_BOMBS;
import static org.jdyna.view.swing.StatusType.SLOW_DOWN;
import static org.jdyna.view.swing.StatusType.SPEED_UP;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;
import org.jdyna.CellType;
import org.jdyna.GameConfiguration;
import org.jdyna.GameEvent;
import org.jdyna.GameStartEvent;
import org.jdyna.GameStateEvent;
import org.jdyna.IGameEventListener;
import org.jdyna.IPlayerSprite;
import org.jdyna.ISprite;
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
     * Plater type (jersey).
     */
    private ISprite.Type playerType;

    /**
     * 
     */
    public PlayerStatusPanel(Images images, String playerName, ISprite.Type playerType)
    {
        this.images = images;
        this.playerName = playerName;
        this.playerType = playerType;

        initializeComponents();
    }

    /**
     * 
     */
    private void initializeComponents()
    {
        setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = GridBagConstraints.RELATIVE;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 2);

        // Add the player's icon.
        final JLabel playerIcon = new JLabel();
        playerIcon.setIcon(new ImageIcon(images.getPlayerStatusImage(playerType)));
        playerIcon.setHorizontalAlignment(SwingConstants.HORIZONTAL);
        add(playerIcon, gbc);

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
        for (Status s : Arrays.asList(
            new Status(LIVES, lifeCountIcon, false),
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
            s.setVisible(false);
            statuses.put(s.field, s);
        }
        
        // Configure those status panels that should always be visible.
        for (StatusType s : Arrays.asList(
            StatusType.LIVES,
            StatusType.BOMBS,
            StatusType.BOMB_RANGE
            ))
        {
            statuses.get(s).hideWhenZero = false;
        }

        // Set insets and add all statuses panels.
        for (Map.Entry<StatusType, Status> e : statuses.entrySet())
        {
            switch (e.getKey())
            {
                case MAX_RANGE:
                case DIARRHOEA:
                    add(new JSeparator(), gbc);
                    break;

                default:
                    break;
            }

            add(e.getValue(), gbc);
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
