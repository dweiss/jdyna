package com.kozmich.dyna.ai;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerSprite;
import com.dawidweiss.dyna.GameEvent.Type;

/**
 * This class is responsible for keeping information about players. Theirs bomb
 * count and bomb range.
 * 
 * @author Lukasz Kozminski
 * @author Tomasz Michalak
 * 
 */
public class PlayerManager implements IGameEventListener {

	private List<PlayerState> playerState = new ArrayList<PlayerState>();

	/**
	 * {@link IGameEventListener#onFrame(int, List)}
	 */
	@Override
	public void onFrame(int arg0, List<? extends GameEvent> events) {
		for (GameEvent gameEvent : events) {
			if (gameEvent.type == Type.GAME_STATE) {
				GameStateEvent gse = (GameStateEvent) gameEvent;
				updatePlayerList(gse.getPlayers());
			}
		}
	}

	/**
	 * Check if we have information about specidfic player.
	 * 
	 * @param player
	 * @return true if {@link PlayerState} is kept in list.
	 */
	public boolean isPlayerState(IPlayerSprite player) {
		for (Iterator<PlayerState> iterator = playerState.iterator(); iterator.hasNext();) {
			PlayerState playerState = iterator.next();
			if (playerState.equals(player)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets bomb range for player which is in specific position.
	 * 
	 * @param x
	 *            row in grid
	 * @param y
	 *            column in grid
	 * @return specific range for player of {@link Globals#DEFAULT_BOMB_RANGE}
	 */
	public int getPlayerRange(int x, int y) {
		int maxRange = Globals.DEFAULT_BOMB_RANGE;
		for (Iterator<PlayerState> iterator = playerState.iterator(); iterator.hasNext();) {
			PlayerState playerState = iterator.next();
			Point position = playerState.getPlayer().getPosition();
			if (position.x / Globals.DEFAULT_CELL_SIZE == x && position.y / Globals.DEFAULT_CELL_SIZE == y
					&& maxRange < playerState.getBombRange()) {
				maxRange = playerState.getBombRange();
			}
		}
		return maxRange;
	}

	/**
	 * Gets this ai player bomb range
	 * 
	 * @param myName
	 *            name of player
	 * @return
	 */
	public int getMyRange(String myName) {
		for (PlayerState playerStateInner : playerState) {
			if (playerStateInner.getPlayer().getName().equals(myName)) {
				return playerStateInner.getBombRange();
			}
		}
		return Globals.DEFAULT_BOMB_RANGE;
	}

	/**
	 * If new event has come {@link AiPlayer#onFrame(int, List)} this method is
	 * invoke to update players.
	 * 
	 * @param players
	 */
	public void updatePlayerList(List<? extends IPlayerSprite> players) {
		for (IPlayerSprite playerSprite : players) {
			if (!isPlayerState(playerSprite)) {
				playerState.add(new PlayerState(playerSprite));
			} else {
				if (playerSprite.isDead()) {
					resetPlayerState(playerSprite);
				}
			}
		}
	}

	/**
	 * This method reset player information - if player died.
	 * 
	 * @param playerSprite
	 */
	private void resetPlayerState(IPlayerSprite playerSprite) {
		for (PlayerState playerState : this.playerState) {
			if (playerState.equals(playerSprite)) {
				playerState.resetState();
			}
		}

	}

	/**
	 * Compare two states and increments bomb count or range for specific
	 * players.
	 * 
	 * @param previousState
	 * @param actualState
	 */
	public void updatePlayerInfo(State previousState, State actualState) {
		Cell[][] pCells = previousState.getCells();
		Cell[][] aCells = actualState.getCells();
		for (int i = 0; i < pCells.length; i++) {
			for (int j = 0; j < pCells[i].length; j++) {
				if (pCells[i][j].type == CellType.CELL_BONUS_RANGE && pCells[i][j].type != aCells[i][j].type) {
					incrementRange(i, j);
				} else if (pCells[i][j].type == CellType.CELL_BONUS_BOMB && pCells[i][j].type != aCells[i][j].type) {
					incrementBombs(i, j);
				}
			}
		}
	}

	/**
	 * Increment bomb range for player which is in position(i, j).
	 * 
	 * @param i
	 * @param j
	 */
	private void incrementRange(int i, int j) {
		for (Iterator<PlayerState> iterator = playerState.iterator(); iterator.hasNext();) {
			PlayerState playerState = iterator.next();
			Point position = playerState.getPlayer().getPosition();
			if (position.x / Globals.DEFAULT_CELL_SIZE == i && position.y / Globals.DEFAULT_CELL_SIZE == j) {
				playerState.incrementBombRange();
			}
		}
	}
	
	/**
	 * Increment bomb count for player which is in position(i, j).
	 * 
	 * @param i
	 * @param j
	 */
	private void incrementBombs(int i, int j) {
		for (Iterator<PlayerState> iterator = playerState.iterator(); iterator.hasNext();) {
			PlayerState playerState = iterator.next();
			Point position = playerState.getPlayer().getPosition();
			if (position.x / Globals.DEFAULT_CELL_SIZE == i && position.y / Globals.DEFAULT_CELL_SIZE == j) {
				playerState.incrementBombCount();
			}
		}
	}

}
