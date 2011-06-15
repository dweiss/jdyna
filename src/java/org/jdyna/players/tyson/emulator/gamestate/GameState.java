package org.jdyna.players.tyson.emulator.gamestate;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdyna.CellType;
import org.jdyna.GameStateEvent;
import org.jdyna.GameConfiguration;
import org.jdyna.IPlayerController.Direction;

import com.google.common.collect.Lists;
import org.jdyna.players.tyson.emulator.gamestate.bombs.AllSimulatedBombs;
import org.jdyna.players.tyson.emulator.gamestate.bombs.Bombs;
import org.jdyna.players.tyson.emulator.gamestate.bombs.OpponentsSimulatedBombs;
import org.jdyna.players.tyson.emulator.gamestate.bombs.SimulatedBombs;
import org.jdyna.players.tyson.pathfinder.Utils;

/**
 * <p>
 * Stores information about state of game.
 * </p>
 * <p>
 * Consists of:
 * <ul>
 * <li>{@link Board} - information about cells.</li>
 * <li>{@link Bombs} - information about bombs.</li>
 * <li>{@link Players} - information about players.</li>
 * </ul>
 * </p>
 * 
 * @author Michał Kozłowski
 */
public class GameState
{
    private final static Logger logger = Logger.getLogger(GameState.class);
    private final Board board;
    private final Bombs bombs;
    private final SimulatedBombs allPossibleBombs;
    private final SimulatedBombs opponentsPossibleBombs;
    private Players players;
    private int lastFrame;

    /**
     * @param frame The current frame number.
     * @param event Source of information about state of game.
     * @param currentPlayer Name of player who asks for state of game.
     */
    public GameState(GameConfiguration conf, final int frame, final GameStateEvent event,
        final String currentPlayer)
    {
        lastFrame = frame - 1;
        board = new Board(event.getCells());
        bombs = new Bombs(board, conf);
        players = new Players(conf, event.getPlayers(), currentPlayer);
        players.addRangesListener(bombs);

        // initialize object with simulated bombs on opponents positions
        opponentsPossibleBombs = new OpponentsSimulatedBombs(board, conf);
        players.addPlayersPositionsListener(opponentsPossibleBombs);
        players.addRangesListener(opponentsPossibleBombs);

        // initialize object with simulated bombs on all players positions
        allPossibleBombs = new AllSimulatedBombs(board, conf);
        players.addPlayersPositionsListener(allPossibleBombs);
        players.addRangesListener(allPossibleBombs);

        update(frame, event);
    }

    public boolean amIAlive()
    {
        return players.amIAlive();
    }

    /**
     * Returns <code>true</code> if a player can walk on the grid's given coordinates.
     * Copied from com.jdyna.Game class and adapted.
     */
    public boolean canWalkOn(final GridCoord txy, final int frame)
    {
        final ExtendedCell cell = getBoard().cellAt(txy);
        if (cell.getType() == CellType.CELL_WALL)
        {
            return false;
        }

        // for (GridCoord opponentsCell : players.getOpponentsCells()) {
        // if (txy.equals(opponentsCell)) {
        // return false;
        // }
        // }

        if (cell.getType().isWalkable())
        {
            return true;
        }
        else
        {
            if (cell.getType().equals(CellType.CELL_BOMB))
            {
                return !hasBomb(frame, txy);
            }
            return isWalkable(frame, txy);
        }
    }

    public boolean canWalkOnExcludeOpponents(final GridCoord txy, final int frame)
    {
        if (canWalkOn(txy, frame))
        {
            for (GridCoord opponentsCell : players.getOpponentsCells())
            {
                if (txy.equals(opponentsCell))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public int cellsToFirstWall(GridCoord cell, Direction d)
    {
        int count = 0;
        GridCoord next = board.nextCell(cell, d);
        while (board.cellAt(next).isWalkable())
        {
            count++;
            next = board.nextCell(next, d);
        }
        return count;
    }

    public Board getBoard()
    {
        return board;
    }

    public Bombs getBombs()
    {
        return bombs;
    }

    public List<GridCoord> getBonusCells()
    {
        return board.getBonusCells();
    }

    public Collection<ExtendedPlayer> getOpponents()
    {
        return players.getOpponents();
    }

    public List<GridCoord> getOpponentsCells()
    {
        return players.getOpponentsCells();
    }

    public GridCoord getPlayerCell(String playerName)
    {
        return players.getPlayerCell(playerName);
    }

    public GridCoord getMyCell()
    {
        return players.getMyCell();
    }

    public PointCoord getPlayerPosition()
    {
        return players.getPlayerPosition();
    }

    /**
     * @return Range of current player.
     */
    public int getRange()
    {
        return players.getRange();
    }

    /**
     * @param framesShift Describes future in number of frames.
     * @param location Coordinates of cell.
     * @return <code>true<code> if cell will have bomb, otherwise <code>false</code>
     */
    public boolean hasBomb(final int framesShift, final GridCoord location)
    {
        return bombs.hasBomb(location, framesShift);
    }

    public boolean isATunnel(final GridCoord start, final GridCoord destination,
        final Direction d)
    {
        GridCoord next = board.nextCell(start, d);
        while (!destination.equals(next) && board.cellAt(next).isWalkable()
            && board.getWalkableNeighbors(next).size() <= 2)
        {
            next = board.nextCell(next, d);
        }
        if (destination.equals(next))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isLineWalkable(final GridCoord start, final GridCoord destination,
        final Direction d)
    {
        GridCoord next = board.nextCell(start, d);
        while (!destination.equals(next) && board.cellAt(next).isWalkable())
        {
            next = board.nextCell(next, d);
        }
        if (destination.equals(next))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns <code>true</code> if frames from given period are safe.
     * 
     * @param from First frame of period (inclusive).
     * @param to One before last frame of period.
     */
    public boolean isPeriodSafe(final PointCoord point, final int from, final int to)
    {
        final int timeDiff = to - 1 - from;
        if (timeDiff <= Bombs.EXPLOSION_FRAMES)
        {
            return isSafe(point, from) && isSafe(point, to - 1);
        }
        for (int frame = from; frame < to; frame = frame + Bombs.EXPLOSION_FRAMES)
        {
            if (!isSafe(point, frame))
            {
                return false;
            }
        }
        return isSafe(point, to - 1);
    }

    /**
     * Returns <code>true</code> if frames from given period are safe.
     * 
     * @param from First frame of period (inclusive).
     * @param to One before last frame of period.
     */
    public boolean isPeriodSafeWithOppAndMyBombs(final PointCoord point, final int from,
        final int to)
    {
        final int timeDiff = to - 1 - from;
        if (timeDiff <= Bombs.EXPLOSION_FRAMES)
        {
            return isSafeWithOppAndMyBombs(point, from)
                && isSafeWithOppAndMyBombs(point, to - 1);
        }
        for (int frame = from; frame < to; frame = frame + Bombs.EXPLOSION_FRAMES)
        {
            if (!isSafeWithOppAndMyBombs(point, frame))
            {
                return false;
            }
        }
        return isSafeWithOppAndMyBombs(point, to - 1);
    }

    /**
     * Returns <code>true</code> if frames from given period are safe. Considers bombs on
     * opponents positions.
     * 
     * @param from First frame of period (inclusive).
     * @param to One before last frame of period.
     */
    public boolean isPeriodSafeWithOppBombs(final PointCoord point, final int from,
        final int to)
    {
        final int timeDiff = to - 1 - from;
        if (timeDiff <= Bombs.EXPLOSION_FRAMES)
        {
            return isSafeWithOppBombs(point, from) && isSafeWithOppBombs(point, to - 1);
        }
        for (int frame = from; frame < to; frame = frame + Bombs.EXPLOSION_FRAMES)
        {
            if (!isSafeWithOppBombs(point, frame))
            {
                return false;
            }
        }
        return isSafeWithOppBombs(point, to - 1);
    }

    public boolean isPlayerAlive(final String name)
    {
        return players.isPlayerAlive(name);
    }

    /**
     * @param point To check its safety.
     * @param framesShift Describes future in number of frames.
     * @return <code>true<code> if cell will be safe, otherwise <code>false</code>.
     */
    public boolean isSafe(final PointCoord point, final int framesShift)
    {
        final GridCoord grid = Utils.pixelToGrid(point);
        return bombs.isSafe(grid, framesShift);
    }

    /**
     * @param point To check its safety.
     * @param framesShift Describes future in number of frames.
     * @return <code>true<code> if cell will be safe, otherwise <code>false</code>.
     *         Considers bombs on all players positions.
     */
    public boolean isSafeWithOppAndMyBombs(final PointCoord point, final int framesShift)
    {
        final GridCoord grid = Utils.pixelToGrid(point);
        return allPossibleBombs.isSafe(grid, framesShift);
    }

    /**
     * @param point To check its safety.
     * @param framesShift Describes future in number of frames.
     * @return <code>true<code> if cell will be safe, otherwise <code>false</code>.
     *         Considers bombs on opponents positions.
     */
    public boolean isSafeWithOppBombs(final PointCoord point, final int framesShift)
    {
        final GridCoord grid = Utils.pixelToGrid(point);
        return opponentsPossibleBombs.isSafe(grid, framesShift);
    }

    /**
     * @param point To check its safety.
     * @return
     *         <code>true<code> if cell is ultimately safe (without any bombs in future), otherwise <code>false</code>
     */
    public boolean isUltimatelySafe(final PointCoord point)
    {
        return bombs.isUltimatelySafe(Utils.pixelToGrid(point));
    }

    /**
     * @param point To check its safety.
     * @param framesShift Describes future in number of frames.
     * @return
     *         <code>true<code> if cell will be ultimately safe (without any bombs in future), otherwise <code>false</code>
     *         .
     */
    public boolean isUltimatelySafe(final PointCoord point, final int framesShift)
    {
        return bombs.isUltimatelySafe(Utils.pixelToGrid(point), framesShift);
    }

    /**
     * @param point To check its safety.
     * @return
     *         <code>true<code> if cell is ultimately safe (without any bombs in future), otherwise <code>false</code>
     *         . Considers bombs on all players positions.
     */
    public boolean isUltimatelySafeWithOppAndMyBombs(final PointCoord point)
    {
        return allPossibleBombs.isUltimatelySafe(Utils.pixelToGrid(point));
    }

    /**
     * @param point To check its safety.
     * @return
     *         <code>true<code> if cell is ultimately safe (without any bombs in future), otherwise <code>false</code>
     *         . Considers bombs on opponents positions.
     */
    public boolean isUltimatelySafeWithOppBombs(final PointCoord point)
    {
        return opponentsPossibleBombs.isUltimatelySafe(Utils.pixelToGrid(point));
    }

    /**
     * @param framesShift Describes future in number of frames.
     * @param location Coordinates of cell.
     * @return <code>true<code> if cell will be walkable, otherwise <code>false</code>
     */
    public boolean isWalkable(final int framesShift, final GridCoord location)
    {
        final CellType type = board.cellAt(location).getType();
        if (type == CellType.CELL_WALL)
        {
            return false;
        }
        else if (type == CellType.CELL_CRATE)
        {
            return bombs.someBombZoneWillExploded(location, framesShift);
        }
        else
        {
            return true;
        }
    }

    /**
     * Returns first safe frames after explosions.
     */
    public List<Integer> safeFrames(final PointCoord point, final int elapsedFrames)
    {
        final List<Integer> result = Lists.newLinkedList();
        int lastExplosion = elapsedFrames;
        final int maxFrame = Math.max(elapsedFrames + 1, Bombs.BOMB_LIFETIME + 1);
        for (int frame = elapsedFrames + 1; frame <= maxFrame; frame++)
        {
            if (isSafe(point, frame))
            {
                if (frame == lastExplosion + 1)
                {
                    result.add(frame);
                }
            }
            else
            {
                lastExplosion = frame;
            }
        }
        return result;
    }

    /**
     * Returns first safe frames after explosions. Considers bombs on all players
     * positions.
     */
    public List<Integer> safeFramesWithOppAndMyBombs(final PointCoord point,
        final int elapsedFrames)
    {
        final List<Integer> result = Lists.newLinkedList();
        int lastExplosion = elapsedFrames;
        final int maxFrame = Math.max(elapsedFrames + 1, Bombs.BOMB_LIFETIME + 1);
        for (int frame = elapsedFrames + 1; frame <= maxFrame; frame++)
        {
            if (isSafeWithOppAndMyBombs(point, frame))
            {
                if (frame == lastExplosion + 1)
                {
                    result.add(frame);
                }
            }
            else
            {
                lastExplosion = frame;
            }
        }
        return result;
    }

    /**
     * Returns first safe frames after explosions. Considers bombs on opponents positions.
     */
    public List<Integer> safeFramesWithOppBombs(final PointCoord point,
        final int elapsedFrames)
    {
        final List<Integer> result = Lists.newLinkedList();
        int lastExplosion = elapsedFrames;
        final int maxFrame = Math.max(elapsedFrames + 1, Bombs.BOMB_LIFETIME + 1);
        for (int frame = elapsedFrames + 1; frame <= maxFrame; frame++)
        {
            if (isSafeWithOppBombs(point, frame))
            {
                if (frame == lastExplosion + 1)
                {
                    result.add(frame);
                }
            }
            else
            {
                lastExplosion = frame;
            }
        }
        return result;
    }

    /**
     * @param frame The current frame number.
     * @param event Source of information about state of game.
     */
    public void update(final int frame, final GameStateEvent event)
    {
        final int nrOfFrames = frame - lastFrame;
        if (nrOfFrames <= 0)
        {
            logger.warn("Frame with id from past was ignored, lastFrame: " + lastFrame
                + ", newFrame: " + frame);
            return;
        }

        // update board
        board.update(event.getCells());

        // update cells with bombs
        bombs.updateBombs(nrOfFrames, board);
        // update cells with simulated bombs on opponents positions
        opponentsPossibleBombs.update(bombs);
        // update cells with simulated bombs on all players positions
        allPossibleBombs.update(bombs);

        // update information about players
        players.update(this, event.getPlayers());

        // TODO Czy to nie powinno wylecieć?
        // if (!players.playerExists()) {
        // return;
        // }

        // update cells threatened by bombs
        bombs.updateZoneSafety(nrOfFrames, board);
        opponentsPossibleBombs.updateZoneSafety(nrOfFrames, board);
        allPossibleBombs.updateZoneSafety(nrOfFrames, board);

        lastFrame = frame;
    }

}
