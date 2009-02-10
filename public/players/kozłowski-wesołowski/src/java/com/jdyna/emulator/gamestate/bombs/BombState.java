package com.jdyna.emulator.gamestate.bombs;

import com.dawidweiss.dyna.Globals;

/**
 * <p>
 * Describes state of cell with bomb.
 * </p>
 * 
 * @author Michał Kozłowski
 * 
 */
public class BombState {
	private BombStatus status;
	/**
	 * Nr of frames when this state will disappear from board. In {@link BombStatus#READY} it means time of explosion,
	 * in state {@link BombStatus#EXPLODED} it means time of end of explosion.
	 */
	private int timer;

	private int range = Globals.DEFAULT_BOMB_RANGE;

	public enum BombStatus {
		READY, EXPLODED;
	};

	/**
	 * @param framesShift Describes future in number of frames.
	 * @return <code>true</code> if standing on cells threatened by this bomb will be safe for player, otherwise
	 *         <code>false</code>
	 */
	public boolean isSafe(final int framesShift) {
		if (status == BombStatus.READY) {
			return ((framesShift <= timer) || (framesShift > (timer + Bombs.EXPLOSION_FRAMES)));
		} else {
			return (framesShift > timer);
		}
	}

	/**
	 * Creates object with default range value.
	 */
	public BombState(final int timer, final BombStatus status) {
		this.timer = timer;
		this.status = status;
	}

	public BombState(final int timer, final int range, final BombStatus status) {
		this.timer = timer;
		this.range = range;
		this.status = status;
	}

	public BombState(final BombState src) {
		this.timer = src.timer;
		this.status = src.getStatus();
		this.range = src.getRange();
	}

	public BombStatus getStatus() {
		return status;
	}

	public void setStatus(final BombStatus status) {
		this.status = status;
	}

	public int getTimer() {
		return timer;
	}

	public void setTimer(int timer) {
		this.timer = timer;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}
}
