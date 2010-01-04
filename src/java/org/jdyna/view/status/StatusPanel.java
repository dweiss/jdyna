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

        // Load the resource only where it's needed, no need to keep it as a field.
        final BufferedImage lifeCountIcon;
        try
        {
            lifeCountIcon = ImageUtilities.loadResourceImage("icons/life.png");
        }
        catch (IOException e)   
        {
            throw new RuntimeException(e);
        }

        // TODO: the defaults should be initialized to not-available values until 
        // the value is known. The non-available values should also have a visual marker in the
        // user interface (a question mark for example).

        // Linked hash map keeps the order of inserts, so it will preserve the order here.
        statuses = Maps.newLinkedHashMap();
        for (Status s : Arrays.asList(
            new Status(LIVES, lifeCountIcon, 3),
            new Status(BOMBS, getCellImage(CELL_BONUS_BOMB, 0), 2),
            new Status(BOMB_RANGE, getCellImage(CELL_BONUS_RANGE, 0), 3),
            new Status(MAX_RANGE, getCellImage(CELL_BONUS_MAXRANGE, 0), -1),
            new Status(AHMED, getCellImage(CELL_BONUS_AHMED, 0), -1),
            new Status(SPEED_UP, getCellImage(CELL_BONUS_SPEED_UP, 0), -1),
            new Status(CRATE_WALKING, getCellImage(CELL_BONUS_CRATE_WALKING, 0), -1),
            new Status(BOMB_WALKING, getCellImage(CELL_BONUS_BOMB_WALKING, 0), -1),
            new Status(IMMORTALITY, getCellImage(CELL_BONUS_IMMORTALITY, 0), -1),
            new Status(DIARRHOEA, getCellImage(CELL_BONUS_DIARRHEA, 0), -1),
            new Status(NO_BOMBS, getCellImage(CELL_BONUS_NO_BOMBS, 0), -1),
            new Status(SLOW_DOWN, getCellImage(CELL_BONUS_SLOW_DOWN, 0), -1),
            new Status(CTRL_REVERSE, getCellImage(CELL_BONUS_CONTROLLER_REVERSE, 0), -1)))
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
        if (players.size() <= 0)
            return;

        // TODO: determine which player you're tracking (this should be 
        // an initialization parameter -- for example by player name);

        final IPlayerSprite player = players.get(0);

        statuses.get(LIVES)
            .updateValue(player.getLifeCount());

        statuses.get(BOMBS)
            .updateValue(player.getBombCount());

        statuses.get(BOMB_RANGE)
            .updateValue(
                ifActiveCounter(player.getMaxRangeEndsAtFrame(), frame, player.getBombRange(), -1));

        /*
         * TODO: Marta, the rest of the code should follow the above. Don't write spaghetti
         * code, please (repetitive stuff to methods).
         */

        /*
        statuses[2].updateCounter(((player.getMaxRangeEndsAtFrame() < 0) || (player
            .getMaxRangeEndsAtFrame()
            - frame < 0)) ? player.getBombRange() : -1, 1);

        statuses[3].updateCounter((player.getMaxRangeEndsAtFrame() < 0) ? player
            .getMaxRangeEndsAtFrame() : player.getMaxRangeEndsAtFrame() - frame
            + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);

        statuses[4].updateCounter(player.isAhmed() ? 1 : -1, 1);
        statuses[5].updateCounter((player.getSpeedUpEndsAtFrame() < 0) ? player
            .getSpeedUpEndsAtFrame() : player.getSpeedUpEndsAtFrame() - frame
            + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);
        statuses[6].updateCounter((player.getCrateWalkingEndsAtFrame() < 0) ? player
            .getCrateWalkingEndsAtFrame() : player.getCrateWalkingEndsAtFrame()
            - frame + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);
        statuses[7].updateCounter((player.getBombWalkingEndsAtFrame() < 0) ? player
            .getBombWalkingEndsAtFrame() : player.getBombWalkingEndsAtFrame() - frame
            + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);
        statuses[8].updateCounter((player.getImmortalityEndsAtFrame() < 0) ? player
            .getImmortalityEndsAtFrame() : player.getImmortalityEndsAtFrame() - frame
            + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);

        statuses[9].updateCounter((player.getDiarrheaEndsAtFrame() < 0) ? player
            .getDiarrheaEndsAtFrame() : player.getDiarrheaEndsAtFrame() - frame
            + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);
        statuses[10].updateCounter((player.getNoBombsEndsAtFrame() < 0) ? player
            .getNoBombsEndsAtFrame() : player.getNoBombsEndsAtFrame() - frame
            + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);
        statuses[11].updateCounter((player.getSlowDownEndsAtFrame() < 0) ? player
            .getSlowDownEndsAtFrame() : player.getSlowDownEndsAtFrame() - frame
            + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);
        statuses[12].updateCounter(
            (player.getControllerReverseEndsAtFrame() < 0) ? player
                .getControllerReverseEndsAtFrame() : player
                .getControllerReverseEndsAtFrame()
                - frame + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);
        */
    }

    /**
     * Checks if <code>counterMaxFrame</code> is active and if so, returns
     * <code>
     */
    private int ifActiveCounter(int counterMaxFrame, int frame, int inactiveValue, int activeValue)
    {
        if (counterMaxFrame < 0 || counterMaxFrame - frame < 0)
            return inactiveValue;
        else
            return activeValue;
    }

    // TODO: what is this if it's not used?

    /**
     * Return an image for a given cell at the given counter.
     */
    private BufferedImage getCellImage(CellType cell, int cellCounter)
    {
        BufferedImage [] cellImages = images.getCellImage(cell);
        final int advanceRate = images.getCellAdvanceCounter(cell);

        if (cellImages == null || cellImages.length == 0)
        {
            return null;
        }

        final int frame = cellCounter / advanceRate;
        return cellImages[frame % cellImages.length];
    }
}
