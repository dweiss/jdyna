package org.jdyna.players.tyson.emulator;

import java.util.Collection;
import java.util.List;

import org.jdyna.CellType;

import org.jdyna.players.tyson.emulator.gamestate.ExtendedPlayer;
import org.jdyna.players.tyson.emulator.gamestate.GridCoord;
import org.jdyna.players.tyson.emulator.gamestate.PointCoord;
import org.jdyna.players.tyson.pathfinder.Pathfinder;
import org.jdyna.players.tyson.pathfinder.Utils;

/**
 * An early version of our bot, which tries to put a bomb in a cell which is next to some
 * opponent.
 * 
 * @author Bartosz Wesołowski
 */
public final class NeighborhoodBomber extends AbstractPlayerEmulator
{
    private Pathfinder pathfinder = new Pathfinder();
    /** Path to follow. */
    private List<Direction> trip;
    /** Opponent that is being followed. */
    private ExtendedPlayer target;
    /** Cell chosen to place a bomb in. */
    private GridCoord cellToBomb;
    /** Is the bot currently looking for shelter. */
    private boolean findShelter = false;

    /** @param name Player's name. */
    public NeighborhoodBomber(final String name)
    {
        super(name);
    }

    @Override
    public boolean dropsBomb()
    {
        if (state != null && state.amIAlive())
        {
            if (!findShelter)
            {
                // there is no bomb already placed
                if (state.getBoard().cellAt(state.getMyCell()).getType() != CellType.CELL_BOMB)
                {
                    for (GridCoord cell : state.getOpponentsCells())
                    { // iterate over opponents
                        // opponent is in the same row or column as bot
                        if (state.getMyCell().x == cell.x
                            || state.getMyCell().y == cell.y)
                        {
                            final Direction d = Utils.getDirection(state.getMyCell(),
                                cell);
                            // there is a tunnel between bot and opponent
                            if (state.isATunnel(state.getMyCell(), cell, d))
                            {
                                // the explosion can reach the end of the tunnel or the
                                // opponent is just next to bot
                                if (state.cellsToFirstWall(state.getMyCell(), d) <= state
                                    .getRange()
                                    || Utils.isNeighbor(state.getMyCell(), cell))
                                {
                                    // there is a way to escape, if bot places a bomb
                                    if (pathfinder.findShelterWithMyBomb(state, state
                                        .getPlayerPosition()) != null)
                                    {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Direction getCurrent()
    {
        if (state != null && state.amIAlive())
        {
            final PointCoord myPosition = state.getPlayerPosition();
            final GridCoord myCell = state.getMyCell();
            final boolean myCellHasBomb = state.getBombs().hasBomb(cellToBomb, 0);

            // bot is looking for shelter?
            if (findShelter)
            {
                if (state.isUltimatelySafe(myPosition))
                {
                    findShelter = false;
                }
                else
                {
                    trip = pathfinder.findShelter(state, myPosition);
                }
            }

            if (!findShelter)
            {
                // find the closest opponent
                {
                    final Collection<ExtendedPlayer> opponents = state.getOpponents();
                    int minDistance = Integer.MAX_VALUE;
                    for (ExtendedPlayer opponent : opponents)
                    {
                        final List<Direction> path = pathfinder.findTrip(state,
                            myPosition, opponent.getCell());
                        if (path == null)
                        {
                            continue;
                        }
                        final int distance = path.size();
                        if (distance < minDistance)
                        {
                            target = opponent;
                            minDistance = distance;
                        }
                    }
                    if (minDistance == Integer.MAX_VALUE)
                    {
                        target = null;
                    }
                }

                // find cell to place a bomb in (the closest cell next to the target)
                if (target != null)
                {
                    final List<GridCoord> cellsToBomb = state.getBoard()
                        .getWalkableNeighbors(target.getCell());
                    int minDistance = Integer.MAX_VALUE;
                    for (GridCoord cell : cellsToBomb)
                    {
                        if (cell.equals(myCell) && myCellHasBomb)
                        {
                            continue;
                        }
                        final List<Direction> pathToCell = pathfinder.findTrip(state,
                            myPosition, cell);
                        if (pathToCell == null)
                        {
                            continue;
                        }
                        if (pathToCell.size() < minDistance)
                        {
                            minDistance = pathToCell.size();
                            cellToBomb = cell;
                            trip = pathToCell;
                        }
                    }

                    if (minDistance == Integer.MAX_VALUE)
                    {
                        findShelter = true;
                    }
                }
                else
                { // no accessible opponents were found
                    findShelter = true;
                }

                // if there were no targets found, bot may want to find a shelter once
                // more
                if (findShelter)
                {
                    if (state.isUltimatelySafe(myPosition))
                    {
                        findShelter = false;
                    }
                    else
                    {
                        trip = pathfinder.findShelter(state, myPosition);
                    }
                }

            }

            if (trip != null && trip.size() > 0)
            {
                final Direction result = trip.get(0);
                trip.remove(0);
                return result;
            }
        }
        return null;
    }
}
