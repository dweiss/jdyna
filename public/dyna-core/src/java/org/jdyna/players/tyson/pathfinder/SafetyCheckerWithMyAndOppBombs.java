package org.jdyna.players.tyson.pathfinder;

import org.jdyna.players.tyson.emulator.gamestate.GameState;
import org.jdyna.players.tyson.emulator.gamestate.PointCoord;

/**
 * SafetyChecker which regards opponents and bot as additional bombs.
 * 
 * @author Bartosz Wesołowski
 */
final class SafetyCheckerWithMyAndOppBombs implements ISafetyChecker
{

    @Override
    public boolean isUltimatelySafe(GameState gs, PointCoord point, int frameShift)
    {
        return gs.isUltimatelySafeWithOppAndMyBombs(point);
    }

}
