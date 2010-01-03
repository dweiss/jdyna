package org.jdyna.view.status;

import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.jdyna.CellType;
import org.jdyna.GameConfiguration;
import org.jdyna.GameEvent;
import org.jdyna.GameStartEvent;
import org.jdyna.GameStateEvent;
import org.jdyna.IGameEventListener;
import org.jdyna.IPlayerSprite;
import org.jdyna.view.resources.Images;

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
     * Life count icon.
     */
    private BufferedImage lifeCountIcon;

    /**
     * Default icon in case some required image is missing.
     */
    private BufferedImage defaultIcon = new BufferedImage(16, 16,
        BufferedImage.TYPE_INT_RGB);

    /**
     * Number of statistics for each player.
     */
    private int statusesNum = 13;

    /**
     * List of statistics to display.
     */
    private Status [] statuses = new Status [statusesNum];

    /**
     * 
     */
    public StatusPanel(Images images, GraphicsConfiguration conf)
    {
        this.images = images.createCompatible(conf);
        try
        {
            lifeCountIcon = loadImage("src/graphics/icons/life.png");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        initializeComponents();
    }

    /**
     * 
     */
    private void initializeComponents()
    {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(1, 1, 1, 1);

        statuses[0] = new Status("Lives", lifeCountIcon, 3);
        statuses[1] = new Status("Bombs", getCellImage(CellType.CELL_BONUS_BOMB, 0), 2);
        statuses[2] = new Status("Bomb range",
            getCellImage(CellType.CELL_BONUS_RANGE, 0), 3);
        statuses[3] = new Status("Diarrhea",
            getCellImage(CellType.CELL_BONUS_DIARRHEA, 0), -1);
        statuses[4] = new Status("Immortality", getCellImage(
            CellType.CELL_BONUS_IMMORTALITY, 0), -1);
        statuses[5] = new Status("Max range", getCellImage(CellType.CELL_BONUS_MAXRANGE,
            0), -1);
        statuses[6] = new Status("No bombs",
            getCellImage(CellType.CELL_BONUS_NO_BOMBS, 0), -1);
        statuses[7] = new Status("Speed up",
            getCellImage(CellType.CELL_BONUS_SPEED_UP, 0), -1);
        statuses[8] = new Status("Slow down", getCellImage(CellType.CELL_BONUS_SLOW_DOWN,
            0), -1);
        statuses[9] = new Status("Crate walking", getCellImage(
            CellType.CELL_BONUS_CRATE_WALKING, 0), -1);
        statuses[10] = new Status("Bomb walking", getCellImage(
            CellType.CELL_BONUS_BOMB_WALKING, 0), -1);
        statuses[11] = new Status("Controller reverse", getCellImage(
            CellType.CELL_BONUS_CONTROLLER_REVERSE, 0), -1);
        statuses[12] = new Status("Ahmed", getCellImage(CellType.CELL_BONUS_AHMED, 0), -1);

        for (int i = 0; i < statuses.length; i++)
        {
            add(statuses[i], gbc);
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
        if (players.size() > 0)
        {
            final IPlayerSprite player = players.get(0);
            statuses[0].updateCounter(player.getLifeCount(), 1);
            statuses[1].updateCounter(player.getBombCount(), 1);
            statuses[2].updateCounter(player.getBombRange(), 1);
            statuses[3].updateCounter((player.getDiarrheaEndsAtFrame() < 0) ? player
                .getDiarrheaEndsAtFrame() : player.getDiarrheaEndsAtFrame() - frame
                + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);
            statuses[4].updateCounter((player.getImmortalityEndsAtFrame() < 0) ? player
                .getImmortalityEndsAtFrame() : player.getImmortalityEndsAtFrame() - frame
                + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);
            statuses[5].updateCounter((player.getMaxRangeEndsAtFrame() < 0) ? player
                .getMaxRangeEndsAtFrame() : player.getMaxRangeEndsAtFrame() - frame
                + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);
            statuses[6].updateCounter((player.getNoBombsEndsAtFrame() < 0) ? player
                .getNoBombsEndsAtFrame() : player.getNoBombsEndsAtFrame() - frame
                + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);
            statuses[7].updateCounter((player.getSpeedUpEndsAtFrame() < 0) ? player
                .getSpeedUpEndsAtFrame() : player.getSpeedUpEndsAtFrame() - frame
                + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);
            statuses[8].updateCounter((player.getSlowDownEndsAtFrame() < 0) ? player
                .getSlowDownEndsAtFrame() : player.getSlowDownEndsAtFrame() - frame
                + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);
            statuses[9].updateCounter((player.getCrateWalkingEndsAtFrame() < 0) ? player
                .getCrateWalkingEndsAtFrame() : player.getCrateWalkingEndsAtFrame()
                - frame + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);
            statuses[10].updateCounter((player.getBombWalkingEndsAtFrame() < 0) ? player
                .getBombWalkingEndsAtFrame() : player.getBombWalkingEndsAtFrame() - frame
                + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);
            statuses[11].updateCounter(
                (player.getControllerReverseEndsAtFrame() < 0) ? player
                    .getControllerReverseEndsAtFrame() : player
                    .getControllerReverseEndsAtFrame()
                    - frame + conf.DEFAULT_FRAME_RATE, conf.DEFAULT_FRAME_RATE);
            statuses[12].updateCounter(player.isAhmed() ? 1 : -1, 1);
        }
    }

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

    /**
     * Load an image.
     */
    private BufferedImage loadImage(String filename) throws IOException
    {
        File imageFile = new File(filename);
        if (imageFile.canRead()) return ImageIO.read(imageFile);
        return defaultIcon;
    }
}
