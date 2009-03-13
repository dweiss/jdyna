package com.jdyna.emulator;

import java.util.List;

import com.jdyna.emulator.gamestate.GridCoord;

/**
 * Player that executes a given plan of moves and events of dropping bombs.
 * 
 * @author Bartosz Wesołowski
 */
public final class ControlledBomber extends AbstractPlayerEmulator {
	public static enum Move {
		UP, DOWN, LEFT, RIGHT, BOMB
	}

	/** A list of orders to follow. */
	private final List<Move> plan;
	/** Index of the next move to follow. */
	private int index = 0;
	/** Bot's last position. */
	private GridCoord lastPosition;

	/**
	 * @param name Player's name.
	 * @param plan Plan to execute.
	 */
	public ControlledBomber(final String name, final List<Move> plan) {
		super(name);
		this.plan = plan;
	}

	@Override
	public boolean dropsBomb() {
		if (state == null || !state.amIAlive()) {
			return false;
		}
		if (index >= plan.size()) {
			return false;
		}
		// return true if there is BOMB in the plan
		if (plan.get(index) == Move.BOMB) {
			index++;
			return true;
		}
		return false;
	}

	@Override
	public Direction getCurrent() {
		if (state == null || !state.amIAlive()) {
			return null;
		}
		// remember bot's last position to determine if his cell changed
		if (lastPosition == null) {
			lastPosition = state.getMyCell();
		}
		// if bot entered a new cell, retrieve one move from the plan
		if (!state.getMyCell().equals(lastPosition)) {
			lastPosition = state.getMyCell();
			if (plan.get(index) != Move.BOMB) {
				index++;
			}
		}
		// if there is no further plan than stay in one place
		if (index >= plan.size()) {
			return null;
		}
		// return a direction according to the value retrieved from the plan
		if (plan.get(index) != Move.BOMB) {
			final Move result = plan.get(index);
			if (result.equals(Move.UP)) {
				return Direction.UP;
			}
			if (result.equals(Move.DOWN)) {
				return Direction.DOWN;
			}
			if (result.equals(Move.LEFT)) {
				return Direction.LEFT;
			}
			if (result.equals(Move.RIGHT)) {
				return Direction.RIGHT;
			}
		}
		
		return null;
	}

}
