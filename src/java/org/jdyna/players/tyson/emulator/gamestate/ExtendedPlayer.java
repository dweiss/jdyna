package org.jdyna.players.tyson.emulator.gamestate;

import java.util.Map;
import java.util.Map.Entry;

import org.jdyna.Globals;
import org.jdyna.IPlayerSprite;

import com.google.common.collect.Maps;
import org.jdyna.players.tyson.pathfinder.Utils;

/**
 * Stores information about player.
 * 
 * @author Michał Kozłowski
 */
public class ExtendedPlayer
{
    private final String name;
    private PointCoord position;
    private GridCoord grid;
    private int range;
    private int bombsCount;
    private Map<GridCoord, Integer> bombs = Maps.newHashMap();
    private Globals conf;

    /**
     * @param src Source of information about player.
     */
    public ExtendedPlayer(Globals conf, IPlayerSprite src)
    {
        name = src.getName();
        position = new PointCoord(src.getPosition());
        grid = Utils.pixelToGrid(position);
        range = conf.DEFAULT_BOMB_RANGE;
        bombsCount = conf.DEFAULT_BOMB_COUNT;
        this.conf = conf;
    }

    /**
     * @param src Source of information about player.
     */
    public void update(final IPlayerSprite src)
    {
        position = new PointCoord(src.getPosition());
        grid = new GridCoord(Utils.pixelToGrid(position));
    }

    /**
     * Increments range of player's bombs.
     */
    public void incRange()
    {
        range++;
    }

    /**
     * @return range Range of player's bombs.
     */
    public int getRange()
    {
        return range;
    }

    /**
     * Increments number of player's bombs that can be dropped.
     */
    public void incBombsCount()
    {
        bombsCount++;
    }

    /**
     * Adds information about dropping bomb.
     */
    public void addBomb()
    {
        bombs.put(grid, conf.DEFAULT_FUSE_FRAMES);
    }

    /**
     * @param src Source of information about bombs and their timers.
     */
    public void updateBombs(final Map<GridCoord, Integer> src)
    {
        bombs.keySet().retainAll(src.keySet());
        for (Entry<GridCoord, Integer> entry : bombs.entrySet())
        {
            entry.setValue(src.get(entry.getKey()));
        }
    }

    /**
     * @return name Player's name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return player's position ({@link PointCoord}
     */
    public PointCoord getPosition()
    {
        return position;
    }

    /**
     * @return player's position ({@link GridCoord}
     */
    public GridCoord getCell()
    {
        return grid;
    }
}
