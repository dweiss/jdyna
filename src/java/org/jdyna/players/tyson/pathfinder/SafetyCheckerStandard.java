package org.jdyna.players.tyson.pathfinder;

import org.jdyna.players.tyson.emulator.gamestate.GameState;
import org.jdyna.players.tyson.emulator.gamestate.PointCoord;

/**
 * SafetyChecker which does not take into account any artificial bombs.
 * 
 * @author Bartosz Wesołowski
 */
final class SafetyCheckerStandard implements ISafetyChecker
{

    @Override
    public boolean isUltimatelySafe(GameState gs, PointCoord point, int frameShift)
    {
        return gs.isUltimatelySafe(point, frameShift);
    }

}
