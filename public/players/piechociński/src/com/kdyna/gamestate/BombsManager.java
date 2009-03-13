package com.kdyna.gamestate;

import java.util.ArrayList;
import java.util.List;

import org.jdyna.CellType;


/**
 * Represents and manages informations about all bombs currently on board
 * 
 * @author Krzysztof P
 *
 */
public class BombsManager {

	private List<Bomb> bombs;
	private CellInfo[][] board;
	
	public BombsManager(final CellInfo[][] board) {
		bombs = new ArrayList<Bomb>();
		this.board = board;
	}
		
	public void addBomb(final Bomb bomb) {
		bombs.add(bomb);
		if (board[bomb.getX()][bomb.getY()].willExplode()) {
			bomb.setCounter(board[bomb.getX()][bomb.getY()].getExplosionCounter());
		} 
		calculateBombEffectArea(bomb);		
	}
	
	/**
	 * recalculate time to explosion for all cells affected by placed bomb
	 */
	private void calculateBombEffectArea(final Bomb bomb) {
		int x = bomb.getX();
		int y = bomb.getY();	
		board[x][y].setTimeToExplode(bomb.getCounter());
		//right
		for (int i = 1; i <= bomb.getRange() && x + i < board.length; i++) {
			CellInfo cell = board[x+i][y];
			if (cell.willExplode()) {
				cell.setTimeToExplode(Math.min(bomb.getCounter(), cell.getTimeToExplode()));
			} else {
				cell.setTimeToExplode(bomb.getCounter());
			}
			if (cell.getType() == CellType.CELL_WALL || cell.getType() == CellType.CELL_CRATE) break;
			if (getBombAt(cell.x, cell.y) != null ) {
				if (getBombAt(cell.x, cell.y).getCounter() > bomb.getCounter()){
					getBombAt(cell.x, cell.y).setCounter(bomb.getCounter());
					calculateBombEffectArea(getBombAt(cell.x, cell.y));
				}
			}
		}
		//left
		for (int i = 1; i <= bomb.getRange() && x - i >= 0; i++) {
			CellInfo cell = board[x-i][y];
			if (cell.willExplode()) {
				cell.setTimeToExplode(Math.min(bomb.getCounter(), cell.getTimeToExplode()));
			} else {
				cell.setTimeToExplode(bomb.getCounter());
			}
			if (cell.getType() == CellType.CELL_WALL || cell.getType() == CellType.CELL_CRATE) break;
			if (getBombAt(cell.x, cell.y) != null ) {
				if (getBombAt(cell.x, cell.y).getCounter() > bomb.getCounter()){
					getBombAt(cell.x, cell.y).setCounter(bomb.getCounter());
					calculateBombEffectArea(getBombAt(cell.x, cell.y));
				}
			}
		}
		//up
		for (int i = 1; i <= bomb.getRange() && y - i >= 0; i++) {
			CellInfo cell = board[x][y-i];
			if (cell.willExplode()) {
				cell.setTimeToExplode(Math.min(bomb.getCounter(), cell.getTimeToExplode()));
			} else {
				cell.setTimeToExplode(bomb.getCounter());
			}
			if (cell.getType() == CellType.CELL_WALL || cell.getType() == CellType.CELL_CRATE) break;
			if (getBombAt(cell.x, cell.y) != null ) {
				if (getBombAt(cell.x, cell.y).getCounter() > bomb.getCounter()){
					getBombAt(cell.x, cell.y).setCounter(bomb.getCounter());
					calculateBombEffectArea(getBombAt(cell.x, cell.y));
				}
			}
		}
		//down
		for (int i = 1; i <= bomb.getRange() && y + i < board[x].length; i++) {
			CellInfo cell = board[x][y+i];
			if (cell.willExplode()) {
				cell.setTimeToExplode(Math.min(bomb.getCounter(), cell.getTimeToExplode()));
			} else {
				cell.setTimeToExplode(bomb.getCounter());
			}
			if (cell.getType() == CellType.CELL_WALL || cell.getType() == CellType.CELL_CRATE) break;
			if (getBombAt(cell.x, cell.y) != null ) {
				if (getBombAt(cell.x, cell.y).getCounter() > bomb.getCounter()){
					getBombAt(cell.x, cell.y).setCounter(bomb.getCounter());
					calculateBombEffectArea(getBombAt(cell.x, cell.y));
				}
			}
		}
	}		
	
	
	public void reduceTimers(final int frameDiff) {
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board[x].length; y++) {
				if (board[x][y].willExplode() ) {
					board[x][y].setTimeToExplode(board[x][y].getTimeToExplode() - frameDiff);
				}				
			}
		}
	}
	

	
	public List<Bomb> getBombs() {
		return bombs;
	}

	public void detonationAt(final int x,  final int y) {
		bombs.remove(getBombAt(x, y));
	}
	
	public Bomb getBombAt(final int x, final int y) {
		for (Bomb bomb : bombs) {
			if (bomb.getX() == x && bomb.getY() == y) {
				return bomb;
			}
		}
		return null;
	}
}
