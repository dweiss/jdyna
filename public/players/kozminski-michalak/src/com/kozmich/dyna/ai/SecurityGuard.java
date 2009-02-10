package com.kozmich.dyna.ai;

import java.awt.Point;
import java.util.Iterator;
import java.util.List;

import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.GameEvent.Type;

/**
 * This class is used to estimate threatening by bombs.
 * 
 * @author Lukasz Kozminski
 * @author Tomasz Michalak
 *
 */
public class SecurityGuard implements IGameEventListener {

	private final static int CELL_SAFE = 0;
	private final static int CELL_DANGEROUS = 1;
	public static final int EMPTY_CELL = 0;
	
	private State actualState;
	private int[][] explosionBoard;
	private int[][] bombRange;

	private boolean initialization = true;
		
	/**
	 * Keep information about players - theirs bomb range and bomb count.
	 */
	private PlayerManager playerManager;

	public SecurityGuard(PlayerManager playerManager) {
		this.playerManager = playerManager;
	}
	
	@Override
	public void onFrame(int frame, List<? extends GameEvent> events) {
		GameStateEvent gse = null;
		for (Iterator<? extends GameEvent> iterator = events.iterator(); iterator.hasNext();) {
			GameEvent gameEvent = iterator.next();
			if (gameEvent.type == Type.GAME_STATE) {
				gse = (GameStateEvent) gameEvent;
				break;
			}
		}
		if (gse != null) {
			actualState = new State(gse.getCells());
			if (initialization) {
				initiateData();
				initialization = false;
			}
			calculateState();
		}
	}

	/**
	 * Give advance if this point is save and player could stay at it.
	 * 
	 * @param point
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	public boolean isSafePlace(Point point) throws IndexOutOfBoundsException {
		return explosionBoard[point.x][point.y] == CELL_SAFE ? true : false;
	}

	private void initiateData() {
		explosionBoard = new int[actualState.getCells().length][];
		bombRange = new int[actualState.getCells().length][];
		for (int i = 0; i < actualState.getCells().length; i++) {
			explosionBoard[i] = new int[actualState.getCells()[i].length];
			bombRange[i] = new int[actualState.getCells()[i].length];
		}
	}

	private void clearState() {
		for (int i = 0; i < explosionBoard.length; i++) {
			for (int j = 0; j < explosionBoard[i].length; j++) {
				explosionBoard[i][j] = CELL_SAFE;
			}
		}
	}

	private void calculateState() {
		clearState();
		for (int i = 0; i < actualState.getCells().length; i++) {
			for (int j = 0; j < actualState.getCells()[i].length; j++) {
				if (actualState.getCells()[i][j].type == CellType.CELL_BOMB) {
					markDangerousPlace(i, j);
				} else if (actualState.getCells()[i][j].type == CellType.CELL_BOOM_BY
						|| actualState.getCells()[i][j].type == CellType.CELL_BOOM_LX
						|| actualState.getCells()[i][j].type == CellType.CELL_BOOM_RX
						|| actualState.getCells()[i][j].type == CellType.CELL_BOOM_TY
						|| actualState.getCells()[i][j].type == CellType.CELL_BOOM_X
						|| actualState.getCells()[i][j].type == CellType.CELL_BOOM_XY
						|| actualState.getCells()[i][j].type == CellType.CELL_BOOM_Y) {
					markAsBomb(i, j);
				}
			}
		}
	}

	/**
	 * Give information about bomb's range.
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	private int getBombRangeInfo(int i, int j) {
		if (bombRange[i][j] != EMPTY_CELL) {
			return bombRange[i][j];
		} else {
			return playerManager.getPlayerRange(i, j);
		}
	}

	private void markDangerousPlace(int i, int j) {
		boolean leftPossible = true;
		boolean rightPossible = true;
		boolean topPossible = true;
		boolean downPossible = true;
		int range = getBombRangeInfo(i, j);
		markAsBomb(i, j);
		for (int k = 1; k <= range; k++) {
			if (leftPossible)
				leftPossible = markAsBomb(i - k, j);
			if (rightPossible)
				rightPossible = markAsBomb(i + k, j);
			if (downPossible)
				downPossible = markAsBomb(i, j - k);
			if (topPossible)
				topPossible = markAsBomb(i, j + k);
		}
	}

	/**
	 * 
	 * @param i
	 * @param j
	 * @return false if point(i, j) is a wall
	 */
	private boolean markAsBomb(int i, int j) {
		if (i >= 0 && i < actualState.getCells().length) {
			if (j > 0 && j < actualState.getCells()[i].length) {
				if (actualState.getCells()[i][j].type != CellType.CELL_WALL) {
					explosionBoard[i][j] = CELL_DANGEROUS;
				} else {
					return false;
				}
			}
		}
		return true;
	}

}