package com.jdyna.pathfinder;

import com.jdyna.emulator.gamestate.GameState;
import com.jdyna.emulator.gamestate.PointCoord;

/** 
 * SafetyChecker which does not take into account any artificial bombs. 
 * 
 * @author Bartosz Wesołowski
 */
final class SafetyCheckerStandard implements ISafetyChecker {

	@Override
	public boolean isUltimatelySafe(GameState gs, PointCoord point, int frameShift) {
		return gs.isUltimatelySafe(point, frameShift);
	}

}
