package com.jdyna.emulator.gamestate;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;

/**
 * Stores information about cell type and its changes.
 * 
 * @author Michał Kozłowski
 */
public final class ExtendedCell {
	private CellType type;
	private TypeChangedEvent typeChangedEvent;

	/**
	 * Describes event of changing type of this cell during last update.
	 */
	protected enum TypeChangedEvent {
		BOMB_BONUS_TAKEN, RANGE_BONUS_TAKEN, BOMB_SET, NONE;
	}

	/**
	 * @param cell Cell to copy type from it.
	 */
	public ExtendedCell(final Cell cell) {
		type = cell.type;
	}

	/**
	 * @param cell Cell to copy type from it.
	 */
	public void update(final Cell cell) {
		setTypeChangedEvent(cell.type);
		type = cell.type;
	}

	/**
	 * @return type Type of cell.
	 */
	public CellType getType() {
		return type;
	}

	/**
	 * @return {@link TypeChangedEvent} object.
	 */
	public TypeChangedEvent getTypeChanged() {
		return typeChangedEvent;
	}

	/**
	 * @return <code>true</code> if player can walk on this cell.
	 */
	public boolean isWalkable() {
		return type.isWalkable();
	}

	/**
	 * @return <code>true</code> if player can walk on this cell or this cell contains bomb.
	 */
	public boolean isWalkableOrBomb() {
		if (type.isWalkable() || type.equals(CellType.CELL_BOMB)) {
			return true;
		} else {
			return false;
		}
	}

	private void setTypeChangedEvent(final CellType newType) {
		if (newType == CellType.CELL_EMPTY) {
			if (type == CellType.CELL_BONUS_BOMB) {
				typeChangedEvent = TypeChangedEvent.BOMB_BONUS_TAKEN;
				return;
			} else if (type == CellType.CELL_BONUS_RANGE) {
				typeChangedEvent = TypeChangedEvent.RANGE_BONUS_TAKEN;
				return;
			}
		} else if (newType == CellType.CELL_BOMB) {
			if (type == CellType.CELL_EMPTY) {
				typeChangedEvent = TypeChangedEvent.BOMB_SET;
				return;
			}
		}
		typeChangedEvent = TypeChangedEvent.NONE;
	}

}
