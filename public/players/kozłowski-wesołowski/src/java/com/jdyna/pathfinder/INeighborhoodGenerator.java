package com.jdyna.pathfinder;

import java.util.List;

import com.jdyna.emulator.gamestate.GameState;
import com.jdyna.emulator.gamestate.GridCoord;

/** @author Bartosz Wesołowski */
public interface INeighborhoodGenerator {
	/** The cost of one frame. */
	int COST_FRAME = 100;
	/** The cost of a turn. */
	int COST_TURN = 5;
	/** The cost of moving straight. */
	int COST_STRAIGHT = 2;
	/** The cost of waiting in one place. */
	int COST_STAY = 0;
	/**
	 * Number of frames you can miss and still stay alive. This value is used in path finding, when we have to determine
	 * whether a path is safe or not. E.g. if you can stay in a cell for another 3 frames and then there will be an
	 * explosion and MARGIN==5 then the path finder will not allow such a move.
	 */
	int MARGIN = 5;

	/** Returns all nodes you can get to from a given point. Destination is necessary to estimate the cost. */
	List<Node> getNeighbors(final GameState gs, final Node node, final GridCoord destination);
}
