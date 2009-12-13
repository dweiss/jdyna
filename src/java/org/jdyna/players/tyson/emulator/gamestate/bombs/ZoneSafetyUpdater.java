package org.jdyna.players.tyson.emulator.gamestate.bombs;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jdyna.CellType;
import org.jdyna.Constants;
import org.jdyna.IPlayerController.Direction;

import org.jdyna.players.tyson.emulator.gamestate.Board;
import org.jdyna.players.tyson.emulator.gamestate.GridCoord;
import org.jdyna.players.tyson.emulator.gamestate.bombs.BombState.BombStatus;

/**
 * <p>
 * Updates information on cells threatened by bomb explosion, that don't contain that
 * bomb.
 * </p>
 * 
 * @author Michał Kozłowski
 */
final class ZoneSafetyUpdater
{
    private final static Logger logger = Logger.getLogger(ZoneSafetyUpdater.class);
    private int nrOfFrames;
    private Board board;
    private Bombs bombs;
    private final Map<GridCoord, ZoneSafety> zoneSafety = new HashMap<GridCoord, ZoneSafety>();

    /**
     * Updates information about grids threatened by bombs.
     * 
     * @param nrOfFrames Describes future in number of frames.
     * @param board Source of information about bombs.
     * @param bombs Source of information about board.
     */
    public void update(final int nrOfFrames, final Board board, final Bombs bombs)
    {
        this.nrOfFrames = nrOfFrames;
        this.board = board;
        this.bombs = bombs;

        // get bombs to update zone around them
        final Map<GridCoord, Integer> uncheckedBombs = this.bombs
            .getReadyBombsWithTimers();

        // update zones around grids with bombs
        while (!uncheckedBombs.isEmpty())
        {
            final GridCoord nearest = getNearestExplosion(uncheckedBombs);
            uncheckedBombs.keySet().remove(nearest);
            setBombRadius(nearest);
        }

        // update zones around grids with explosions
        for (GridCoord p : this.bombs.getExplosions())
        {
            setExplosionRadius(p);
        }

        // remove bombs and explosions that don't result from given state of board
        removeOutOfDate();
    }

    /**
     * @param grid To check bomb on this location.
     * @param framesShift Describes future in number of frames.
     * @return <code>true</code> if at least one bomb that threaten this cell will be
     *         after explosion.
     */
    public boolean someBombZoneWillExploded(final GridCoord grid, final int framesShift)
    {
        final ZoneSafety zs = zoneSafety.get(grid);
        if (zs == null)
        {
            return false;
        }
        return zs.willExploded(framesShift);
    }

    boolean isSafe(final GridCoord grid, final int framesShift)
    {
        final ZoneSafety zs = zoneSafety.get(grid);
        if (zs == null)
        {
            return true;
        }
        else
        {
            return zs.isSafe(framesShift);
        }
    }

    boolean isUltimatelySafe(final GridCoord grid)
    {
        final ZoneSafety zs = zoneSafety.get(grid);
        if (zs == null)
        {
            return true;
        }
        else
        {
            return zs.isUltimatelySafe();
        }
    }

    boolean isUltimatelySafe(final GridCoord grid, final int framesShift)
    {
        final ZoneSafety zs = zoneSafety.get(grid);
        if (zs == null)
        {
            return true;
        }
        else
        {
            return zs.isUltimatelySafe(framesShift);
        }
    }

    private void removeOutOfDate()
    {
        for (ZoneSafety zs : zoneSafety.values())
        {
            zs.removeOutOfDate();
        }
    }

    private void addBombZone(final GridCoord grid, final GridCoord bombLocation,
        final BombState bzs)
    {
        if (zoneSafety.get(grid) == null)
        {
            zoneSafety.put(grid, new ZoneSafety());
        }
        zoneSafety.get(grid).addZoneBomb(bombLocation, bzs);
    }

    private void checkBombDirection(final GridCoord grid, final int range,
        final Direction direction)
    {
        GridCoord next = board.nextCell(grid, direction);
        while (next != null && distance(grid, next, direction) <= range)
        {
            final CellType type = board.cellAt(next).getType();
            // break before wall
            if (type == CellType.CELL_WALL)
            {
                break;
            }
            else
            {
                updateReadyBomb(nrOfFrames, next, grid);
                // break after detonating crate
                if (type == CellType.CELL_CRATE)
                {
                    break;
                }
                next = board.nextCell(next, direction);
            }
        }
    }

    private void checkExplosionDirection(final GridCoord gird, final int range,
        final Direction direction)
    {
        GridCoord next = board.nextCell(gird, direction);
        while (next != null && distance(gird, next, direction) <= range)
        {
            final CellType type = board.cellAt(next).getType();
            if (!type.isExplosion())
            {
                break;
            }
            else
            {
                updateExplodedBomb(nrOfFrames, next, gird);
                next = board.nextCell(next, direction);
            }
        }
    }

    private void updateExplodedBomb(final int nrOfFrames, final GridCoord gird,
        final GridCoord bombLocation)
    {
        addBombZone(gird, bombLocation, bombs.getBombState(bombLocation));
    }

    private void updateReadyBomb(final int nrOfFrames, final GridCoord grid,
        final GridCoord bombLocation)
    {
        final BombState bs = bombs.getBombState(bombLocation);
        addBombZone(grid, bombLocation, bs);
        // check if recursive explosion will occur
        if (bombs.isBombReady(grid))
        {
            bombs.updateTimerIfSmaller(grid, bs.getTimer());
        }
    }

    private void setBombRadius(final GridCoord grid)
    {
        final int range = getRange(grid, BombStatus.READY);
        // check up
        checkBombDirection(grid, range, Direction.UP);
        // check down
        checkBombDirection(grid, range, Direction.DOWN);
        // check left
        checkBombDirection(grid, range, Direction.LEFT);
        // check right
        checkBombDirection(grid, range, Direction.RIGHT);
    }

    private void setExplosionRadius(final GridCoord grid)
    {
        final int range = getRange(grid, BombStatus.EXPLODED);
        // check up
        checkExplosionDirection(grid, range, Direction.UP);
        // check down
        checkExplosionDirection(grid, range, Direction.DOWN);
        // check left
        checkExplosionDirection(grid, range, Direction.LEFT);
        // check right
        checkExplosionDirection(grid, range, Direction.RIGHT);
    }

    private int getRange(final GridCoord grid, final BombStatus state)
    {
        final BombState bs = bombs.getBombState(grid);
        if (bs == null || bs.getStatus() != state)
        {
            logger.error("Unexpected bomb state inside cell");
            return Constants.DEFAULT_CELL_SIZE;
        }
        else
        {
            return bs.getRange();
        }
    }

    private int distance(final GridCoord first, final GridCoord second,
        final Direction direction)
    {
        final int dist;
        switch (direction)
        {
            case DOWN:
                dist = second.y - first.y;
                break;
            case UP:
                dist = first.y - second.y;
                break;
            case LEFT:
                dist = first.x - second.x;
                break;
            case RIGHT:
                dist = second.x - first.x;
                break;
            default:
                dist = 0;
        }
        return dist;
    }

    /**
     * @param pairs: location of bomb, and its timer.
     * @return Location of bomb that will explode first.
     */
    private GridCoord getNearestExplosion(final Map<GridCoord, Integer> unchecked)
    {
        Entry<GridCoord, Integer> min = null;
        for (Entry<GridCoord, Integer> entry : unchecked.entrySet())
        {
            if (min == null)
            {
                min = entry;
            }
            if (entry.getValue() < min.getValue())
            {
                min = entry;
            }
        }
        return min.getKey();
    }
}
