package com.kozmich.dyna.ai;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;

/**
 * Keep game state and compare actual and previous state.
 * 
 * @author Lukasz Kozminski
 * @author Tomasz Michalak
 * 
 */
public class StateManager {

	private State state;

	private boolean bombBonusAppear = false;

	private boolean rangeBonusAppear = false;

	public void updateState(State state) {
		if (state == null)
			throw new NullPointerException();
		this.state = state;
	}

	public State getSavedState() {
		if (state == null)
			throw new NullPointerException();
		return state;
	}

	/**
	 * Check is remembered state is different than actual game state. Set
	 * bombBonusAppear if bonus count is on board or rangeBonusAppear if range
	 * bonus is on board.
	 * 
	 * @param actualState
	 *            actual game state
	 * @return
	 */
	public boolean isGameStateChanged(State actualState) {
		if (state == null)
			throw new NullPointerException();
		boolean change = false;
		Cell[][] pCells = state.getCells();
		Cell[][] aCells = actualState.getCells();
		bombBonusAppear = false;
		rangeBonusAppear = false;
		for (int i = 0; i < aCells.length; i++) {
			for (int j = 0; j < aCells[i].length; j++) {
				if (aCells[i][j].type != pCells[i][j].type) {
					change = true;
				}
				if (aCells[i][j].type == CellType.CELL_BONUS_RANGE) {
					rangeBonusAppear = true;
				} else if (aCells[i][j].type == CellType.CELL_BONUS_BOMB) {
					bombBonusAppear = true;
				}
			}
		}
		return change;
	}

	/**
	 * Gets information if bomb count bonus is on board.
	 * @return
	 */
	public boolean isBombBonusAppear() {
		return bombBonusAppear;
	}

	/**
	 * Gets information if bomb range bonus is on board.
	 * @return
	 */
	public boolean isRangeBonusAppear() {
		return rangeBonusAppear;
	}
}