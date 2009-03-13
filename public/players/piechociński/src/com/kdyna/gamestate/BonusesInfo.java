package com.kdyna.gamestate;

import java.util.ArrayList;
import java.util.List;

import org.jdyna.CellType;


/**
 * Manages information about all bonuses 
 * @author Krzysztof P
 *
 */
public class BonusesInfo {

	private List<CellInfo> bonusCells;
	
	
	public BonusesInfo() {
		bonusCells = new ArrayList<CellInfo>();
	}
	
	public void update() {
		for (CellInfo cell : bonusCells) {
			if (cell.getType() != CellType.CELL_BONUS_BOMB && 
				cell.getType() != CellType.CELL_BONUS_RANGE) {
				bonusCells.remove(cell);
			}
		}
	}
	
	public void addBonus(CellInfo cell ) {
		bonusCells.add(cell);
	}
	
	public void removeBonus(CellInfo cell) {
		bonusCells.remove(cell);
	}
	
	public List<CellInfo> getBonuses() {
		return bonusCells;
	}

	public List<CellInfo> getCellsWithBonus() {
		return bonusCells;
	}
	
}
