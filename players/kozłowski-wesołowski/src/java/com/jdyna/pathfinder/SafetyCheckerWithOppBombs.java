package com.jdyna.pathfinder;

import com.jdyna.emulator.gamestate.GameState;
import com.jdyna.emulator.gamestate.PointCoord;

/** 
 * SafetyChecker which regards opponents as additional bombs. 
 * 
 * @author Bartosz Wesołowski
 */
final class SafetyCheckerWithOppBombs implements ISafetyChecker {

	@Override
	public boolean isUltimatelySafe(GameState gs, PointCoord point, int frameShift) {
		return gs.isUltimatelySafeWithOppBombs(point);
	}

}
