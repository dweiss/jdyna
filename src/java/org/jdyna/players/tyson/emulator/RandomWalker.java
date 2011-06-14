package org.jdyna.players.tyson.emulator;

import java.util.List;
import java.util.Random;

import org.jdyna.players.tyson.emulator.gamestate.GridCoord;
import org.jdyna.players.tyson.pathfinder.Utils;

/**
 * Randomly walking bot.
 * 
 * @author Bartosz Wesołowski
 */
public final class RandomWalker extends AbstractPlayerEmulator
{
    private final Random random = new Random();
    /** Last bot's position. */
    private GridCoord lastCell;
    /** Bot's direction. It changes only once in a while, so it is remembered here. */
    private Direction direction;
    /** Movement options from last frame. */
    private List<GridCoord> previousNeighbors;

    /** @param name Player's name. */
    public RandomWalker(String name)
    {
        super(name);
    }

    @Override
    public boolean dropsBomb()
    {
        return false;
    }

    @Override
    public Direction getCurrent()
    {
        if (state != null && state.amIAlive())
        {
            final GridCoord myCell = state.getMyCell();
            final List<GridCoord> neighbors = state.getBoard().getWalkableNeighbors(
                myCell);

            if (!myCell.equals(lastCell) || !neighbors.equals(previousNeighbors))
            {
                if (neighbors.size() == 0)
                { // there are no neighbors to move to
                    direction = null;
                }
                else if (neighbors.size() == 1)
                { // there is only one movement option
                    direction = Utils.getDirection(myCell, neighbors.get(0));
                }
                else
                { // there is more than one movement option
                    Direction newDirection;
                    do
                    { // chose a random direction, but don't go in the opposite direction
                        final int index = random.nextInt(neighbors.size());
                        newDirection = Utils.getDirection(myCell, neighbors.get(index));
                    }
                    while (newDirection.equals(Utils.getOpposite(direction)));
                    direction = newDirection;
                }
                lastCell = myCell;
                previousNeighbors = neighbors;
            }
        }

        return direction;
    }

}
