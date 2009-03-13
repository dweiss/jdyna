package com.kozmich.dyna.ai;

import org.jdyna.Globals;
import org.jdyna.IPlayerSprite;

/**
 * Represents player state.
 * 
 * @author Lukasz Kozminski
 * @author Tomasz Michalak
 * 
 */
public class PlayerState {

	/**
	 * Reference to player.
	 */
	private IPlayerSprite player;

	/**
	 * Bomb range for player.
	 */
	private int bombRange;

	/**
	 * Bomb count for player.
	 */
	private int bombCount;

	public PlayerState(IPlayerSprite player) {
		this.player = player;
		resetState();
	}

	public void resetState() {
		bombRange = Globals.DEFAULT_BOMB_RANGE;
		bombCount = Globals.DEFAULT_BOMB_COUNT;
	}

	@Override
	public boolean equals(Object obj) {
		return player.getName().equals(((IPlayerSprite) obj).getName());
	}

	public int getBombRange() {
		return bombRange;
	}

	public void setBombRange(int newBombRange) {
		bombRange = newBombRange;
	}

	public void incrementBombRange() {
		bombRange++;
	}

	public int getBombCount() {
		return bombCount;
	}

	public void setBombCount(int newBombCount) {
		bombCount = newBombCount;
	}

	public void incrementBombCount() {
		bombCount++;
	}

	public IPlayerSprite getPlayer() {
		return player;
	}
}
