package org.jdyna.players.tyson.pathfinder;

import java.util.List;

import org.jdyna.players.tyson.emulator.gamestate.GameState;
import org.jdyna.players.tyson.emulator.gamestate.GridCoord;
import org.jdyna.players.tyson.emulator.gamestate.PointCoord;

/**
 * Neighborhood which does not place any artificial bombs.
 * 
 * @author Bartosz Wesołowski
 */
public final class StandardNeighborhoodGenerator extends AbstractNeighborhoodGenerator
{

    @Override
    protected boolean isPeriodSafe(GameState gs, PointCoord point, int from, int to)
    {
        return gs.isPeriodSafe(point, from, to);
    }

    @Override
    protected List<Integer> safeFrames(GameState gs, PointCoord point, int frameShift)
    {
        return gs.safeFrames(point, frameShift);
    }

    @Override
    protected boolean canWalkOn(GameState gs, GridCoord cell, int frameShift)
    {
        return gs.canWalkOn(cell, frameShift);
    }

}
