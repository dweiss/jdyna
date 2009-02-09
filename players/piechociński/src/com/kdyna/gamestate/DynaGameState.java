package com.kdyna.gamestate;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IPlayerSprite;

/**
 * 
 * class containing all information about ongoing game
 * 
 * @author Krzysztof P
 *
 */

public class DynaGameState {
	
	/**
	 * Interface created for passing criteria, wchich cells have to meet to be taken into account
	 * in certain algorithms (mostly route finding)
	 *  
	 */
	public interface CellPredicate {
	    public boolean test(CellInfo ci);
	}
		
	private DynaBoard board;
	private BombsManager bombsManager;
	private BonusesInfo bonusesInfo;
	private PlayersManager playersManager;

	private int frameCounter;

	public DynaGameState(final int frame, final GameStateEvent event) {
		frameCounter = frame;
		
		CellInfo[][] cellInfos = new CellInfo[event.getCells().length][event.getCells()[0].length];

		board = new DynaBoard(cellInfos);
		bombsManager = new BombsManager(cellInfos);		
		bonusesInfo = new BonusesInfo();
		playersManager = new PlayersManager();
		
		for( IPlayerSprite ps : event.getPlayers()) {
			playersManager.addPlayer(ps);
		}

		for (int i = 0; i < cellInfos.length; i++) {
			for (int j = 0; j < cellInfos[i].length; j++) {
				cellInfos[i][j] = new CellInfo(i, j);
				cellInfos[i][j].setType(event.getCells()[i][j].type);
			}
		}
		
		for (int i = 0; i < cellInfos.length; i++) {
			for (int j = 0; j < cellInfos[i].length; j++) {
				if (cellInfos[i][j].getType() == CellType.CELL_BOMB) {
					bombsManager.addBomb(new Bomb(null,i,j));
				}		
			}
		}
		update(frame, event);
	}

	public void update(final int frame, final GameStateEvent event) {
		
		int skippedFrames = frame - frameCounter - 1;
		bombsManager.reduceTimers(skippedFrames + 1);
		playersManager.update(event.getPlayers());
		
		Cell[][] newCells = event.getCells();
		for (int i = 0; i < newCells.length; i++) {
			for (int j = 0; j < newCells[i].length; j++) {
				if (board.getCell(i, j).getType() != newCells[i][j].type) {
					updateInfos(i, j, board.getCell(i, j).getType(), newCells[i][j].type);
				}
				board.getCell(i, j).setType(event.getCells()[i][j].type);
			}
		}
		frameCounter = frame;
	}
	
	/**
	 * updating information about cell[x,y] (and/or player on this filed) basis on diffrence between
	 * earlier and current type of this cell
	 * 
	 */
	
	private void updateInfos(final int x, final int y, final CellType oldCell, final CellType newCell) {
		switch (oldCell) {
			case CELL_EMPTY:
				if (newCell == CellType.CELL_BOMB) {			  // Bomb placed
					PlayerInfo bomber = playersManager.getPlayerAt(x, y);
					Bomb bomb = new Bomb(bomber, x, y);
					bombsManager.addBomb(bomb);
					if (bomber != null) {
						bomber.removeBomb();
					} 
				} else if (newCell == CellType.CELL_BONUS_BOMB) {  // Bonus appeared
					bonusesInfo.addBonus(board.getCell(x, y));
				} else if (newCell == CellType.CELL_BONUS_RANGE) { // Bonues appeared
					bonusesInfo.addBonus(board.getCell(x, y));
				}
				break;
			case CELL_BOMB:
				if (newCell == CellType.CELL_EMPTY) {				//explosion
					Bomb bomb = bombsManager.getBombAt(x, y);
					bombsManager.detonationAt(x, y);
					if (bomb.getOwner() != null) {
						bomb.getOwner().addBomb();
					}					
				}
				break;
			case CELL_BONUS_BOMB:							
				if (newCell.isExplosion()) {							// bonus bomb destroyed
					bonusesInfo.removeBonus(board.getCell(x, y));					
				} else if (newCell == CellType.CELL_EMPTY) {			// bonus bomb captured
					playersManager.getPlayerAt(x,y).increaseCapacity(); 
					bonusesInfo.removeBonus(board.getCell(x, y));					
				}
				break;
			case CELL_BONUS_RANGE:
				if (newCell.isExplosion()) {							// bonus firepower destroyed
					bonusesInfo.removeBonus(board.getCell(x, y));				
				} else if (newCell == CellType.CELL_EMPTY) {			// bonus firepower captured
					playersManager.getPlayerAt(x,y).increaseBombRange();
					bonusesInfo.removeBonus(board.getCell(x, y));				
				}
				break;
		}

	}
	
	/**
	 *  Return list of alive players
	 */
	public List<PlayerInfo> getPlayers() {
		final List<PlayerInfo> result = new ArrayList<PlayerInfo>();
		for (PlayerInfo pi : playersManager.getPlayers()) {
			if (pi.isActive()) result.add(pi);
		}
		return result;
	}

	public PlayerInfo getPlayer(String name) {
		return playersManager.getPlayerByName(name);
	}
	public BonusesInfo getBonusesInfo() {
		return bonusesInfo;
	}
	
	public DynaBoard getBoard() {
		return board;
	}

	public Point exactToGridCoords(final Point exact) {
		return new Point(exact.x / Globals.DEFAULT_CELL_SIZE, exact.y / Globals.DEFAULT_CELL_SIZE);
	}
}
