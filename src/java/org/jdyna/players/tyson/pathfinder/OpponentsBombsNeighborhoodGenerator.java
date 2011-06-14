package org.jdyna.players.tyson.pathfinder;

import java.util.List;

import org.jdyna.players.tyson.emulator.gamestate.GameState;
import org.jdyna.players.tyson.emulator.gamestate.PointCoord;

/**
 * Neighborhood which places artificial bombs in opponents' cells.
 * 
 * @author Bartosz Wesołowski
 */
public final class OpponentsBombsNeighborhoodGenerator extends
    AbstractNeighborhoodGenerator
{

    @Override
    protected boolean isPeriodSafe(GameState gs, PointCoord point, int from, int to)
    {
        return gs.isPeriodSafeWithOppBombs(point, from, to);
    }

    @Override
    protected List<Integer> safeFrames(GameState gs, PointCoord point, int frameShift)
    {
        return gs.safeFramesWithOppBombs(point, frameShift);
    }

}
