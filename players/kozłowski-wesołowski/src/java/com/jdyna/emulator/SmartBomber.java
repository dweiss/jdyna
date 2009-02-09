package com.jdyna.emulator;

import java.util.List;

import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.ControllerState;
import com.dawidweiss.dyna.IPlayerController2;
import com.jdyna.emulator.gamestate.ExtendedPlayer;
import com.jdyna.emulator.gamestate.GridCoord;
import com.jdyna.pathfinder.Pathfinder;
import com.jdyna.pathfinder.Utils;

/**
 * Our final player, which is very smart and fast ;)
 * 
 * @author Bartosz Wesołowski
 */
public final class SmartBomber extends AbstractPlayerEmulator implements IPlayerController2 {
	private Pathfinder pathfinder = new Pathfinder();
	/** Path the bot is following. */
	private List<Direction> path;
	/** The closest opponent that was found in previous frame. */
	private String closestOpponentsName;
	/**
	 * Number of the same directions at the beginning of the path. Used to implement factories where you can explicitly
	 * define the number of frames to move in the same direction.
	 */
	public int directionCount;

	/**
	 * @param name Player's name.
	 */
	public SmartBomber(final String name) {
		super(name);
	}

	@Override
	public boolean dropsBomb() {
		if (state != null && state.amIAlive()) { // bot is alive
			// there is no bomb already placed
			if (state.getBoard().cellAt(state.getMyCell()).getType() != CellType.CELL_BOMB) {
				for (GridCoord cell : state.getOpponentsCells()) { // iterate over opponents
					// opponent is in the same row or column as bot
					if (state.getMyCell().x == cell.x || state.getMyCell().y == cell.y) {
						final Direction d = Utils.getDirection(state.getMyCell(), cell);
						// there is a tunnel between bot and opponent
						if (state.isATunnel(state.getMyCell(), cell, d)) {
							// the explosion can reach the end of the tunnel or the opponent is just next to bot
							if (state.cellsToFirstWall(state.getMyCell(), d) <= state.getRange()
									|| Utils.isNeighbor(state.getMyCell(), cell)) {
								// there is a way to escape, if bot places a bomb
								if (pathfinder.findShelterWithMyBomb(state, state.getPlayerPosition()) != null) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public Direction getCurrent() {
		if (state != null && state.amIAlive()) {
			// find the closest cell that is next to some opponent's cell
			int minDistance = Integer.MAX_VALUE;
			path = null;

			if (closestOpponentsName != null && state.isPlayerAlive(closestOpponentsName)) {
				final GridCoord opponentsCell = state.getPlayerCell(closestOpponentsName);
				for (GridCoord neighbor : state.getBoard().getWalkableNeighbors(opponentsCell)) {
					final List<Direction> p = pathfinder.findTrip(state, state.getPlayerPosition(), neighbor,
							minDistance);
					if (p == null) {
						continue;
					}
					final int distance = p.size();
					if (distance < minDistance) {
						path = p;
						minDistance = distance;
					}
				}
			}

			for (ExtendedPlayer opponent : state.getOpponents()) {
				if (opponent.getName().equals(closestOpponentsName)) {
					continue;
				}
				final GridCoord opponentsCell = opponent.getCell();
				for (GridCoord neighbor : state.getBoard().getWalkableNeighbors(opponentsCell)) {
					final List<Direction> p = pathfinder.findTrip(state, state.getPlayerPosition(), neighbor,
							minDistance);
					if (p == null) {
						continue;
					}
					final int distance = p.size();
					if (distance < minDistance) {
						path = p;
						minDistance = distance;
						closestOpponentsName = opponent.getName();
					}
				}
			}

			for (GridCoord bonusCell : state.getBonusCells()) {
				final List<Direction> p = pathfinder.findTrip(state, state.getPlayerPosition(), bonusCell, minDistance);
				if (p == null) {
					continue;
				}
				final int distance = p.size();
				if (distance < minDistance) {
					path = p;
					minDistance = distance;
				}
			}

			if (path == null) {
				path = pathfinder.findShelter(state, state.getPlayerPosition());
			}

			if (path == null) {
				path = pathfinder.findShelterDesperately(state, state.getPlayerPosition());
			}

		}

		if (path != null && path.size() > 0) {
			return path.get(0);
		}

		return null;
	}

	@Override
	public ControllerState getState() {
		return new ControllerState(getCurrent(), dropsBomb(), 3);
	}

}
