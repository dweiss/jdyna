package org.jdyna.players.tyson.pathfinder;

import org.jdyna.players.tyson.emulator.gamestate.GameState;
import org.jdyna.players.tyson.emulator.gamestate.PointCoord;

/** @author Bartosz Wesołowski */
interface ISafetyChecker
{
    /**
     * Checks if a point is not endangered by any bombs from the moment defined by
     * <code>frameShift</code>.
     */
    boolean isUltimatelySafe(GameState gs, PointCoord point, int frameShift);
}
