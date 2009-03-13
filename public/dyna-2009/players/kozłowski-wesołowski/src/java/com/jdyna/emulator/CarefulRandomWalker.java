package com.jdyna.emulator;

import java.util.List;
import java.util.Random;

import com.jdyna.emulator.gamestate.GridCoord;
import com.jdyna.emulator.gamestate.PointCoord;
import com.jdyna.pathfinder.Utils;
import com.google.common.collect.Lists;

/**
 * Player who doesn't drop bombs. He just walks around and tries to avoid dangerous cells.
 * 
 * @author Bartosz Wesołowski
 */
public final class CarefulRandomWalker extends AbstractPlayerEmulator {
	private final Random random = new Random();
	private GridCoord lastCell;
	private Direction direction;
	private List<GridCoord> previousNeighbors;

	/** @param name Player's name. */
	public CarefulRandomWalker(String name) {
		super(name);
	}

	@Override
	public boolean dropsBomb() {
		return false;
	}

	@Override
	public Direction getCurrent() {
		if (state != null && state.amIAlive()) {
			final PointCoord myPosition = state.getPlayerPosition();
			final GridCoord myCell = state.getMyCell();
			final List<GridCoord> neighbors = state.getBoard().getWalkableNeighbors(myCell);

			// make a list of all safe neighbors
			final List<GridCoord> safeNeighbors = Lists.newLinkedList();
			// if the bot is safe, it will only try to step on other safe cells
			if (state.isUltimatelySafe(myPosition)) {
				for (GridCoord neighbor : neighbors) {
					if (state.isUltimatelySafe(Utils.gridToPixel(neighbor))) {
						safeNeighbors.add(neighbor);
					}
				}
			} else { // bot's cell is in danger
				// try to find safe neighbors
				boolean containsSafe = false;
				for (GridCoord neighbor : neighbors) {
					if (state.isUltimatelySafe(Utils.gridToPixel(neighbor))) {
						containsSafe = true;
						break;
					}
				}
				if (containsSafe) { // there is at least one safe neighbor
					for (GridCoord neighbor : neighbors) {
						if (state.isUltimatelySafe(Utils.gridToPixel(neighbor))) {
							safeNeighbors.add(neighbor);
						}
					}
				} else { // if no safe naighbors exist then walk in a random direction
					safeNeighbors.addAll(neighbors);
				}
			}

			// change direction only if the bot enters a new cell or some neighboring cells have changed their status
			if (!myCell.equals(lastCell) || !safeNeighbors.equals(previousNeighbors)) {
				if (safeNeighbors.size() == 0) {	// there are no neighbors to move to
					direction = null;
				} else if (safeNeighbors.size() == 1) {	// there is only one movement option
					direction = Utils.getDirection(myCell, safeNeighbors.get(0));
				} else {	// there is more than one movement option
					Direction newDirection;
					do {	// chose a random direction, but don't go in the opposite direction
						final int index = random.nextInt(safeNeighbors.size());
						newDirection = Utils.getDirection(myCell, safeNeighbors.get(index));
					} while (newDirection.equals(Utils.getOpposite(direction)));
					direction = newDirection;
				}
				lastCell = myCell;
				previousNeighbors = safeNeighbors;
			}
		}

		return direction;
	}

}
