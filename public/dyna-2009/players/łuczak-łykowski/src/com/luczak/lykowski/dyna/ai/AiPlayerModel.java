package com.luczak.lykowski.dyna.ai;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;

import org.jdyna.*;

/**
 * Model of intelligent player which implements IPlayerController interface
 * 
 * @author Konrad Łykowski
 * @author Ewa Łuczak
 * 
 */
public class AiPlayerModel implements IPlayerController {

	/*
	 * If the player should put up a bomb
	 */
	private volatile boolean dropBomb;
	/*
	 * Best move to make
	 */
	private volatile Direction bestMove;
	/*
	 * The Name of our player
	 */
	private final String playerName;
	/*
	 * Information about how far we want to see, this variable is used in
	 * modifiedDFS function
	 */
	private final int DFSDepth = 10;
	/*
	 * Table with actual players positions
	 */
	private HashMap<Point, String> playersPositions = new HashMap<Point, String>();
	/*
	 * Actual information about players
	 */
	private HashMap<String, AiPlayerInformation> playerInfo = new HashMap<String, AiPlayerInformation>();
	/*
	 * Player last position in cell indexes
	 */
	private HashMap<String, Point> playerPrevPosition = new HashMap<String, Point>();
	/*
	 * Previous game state information
	 */
	private OnFrameInformation prevGameState;
	/*
	 * Actual board cells
	 */
	private Cell[][] cell;
	/*
	 * Previous board cells
	 */
	private Cell[][] prevCell;
	/*
	 * Player position on the board
	 */
	private Point myPosition;
	/*
	 * Weigh of the best move, it is set by the modyfiedDFS
	 */
	private int bestWeight;
	/*
	 * Player previous direction
	 */
	private Direction myprevPosition = Direction.RIGHT;
	/*
	 * Information about dangerous(bomb) places on the board
	 */
	private HashMap<Point, Point> hotBoardCells;
	/*
	 * Board information
	 */
	private final int cellSize = Globals.DEFAULT_CELL_SIZE;

	/**
	 * AiPlayerModel class construction
	 * 
	 * @param name
	 *            of player
	 */
	public AiPlayerModel(String name) {
		playerName = name;
	}

	/**
	 * Convert pixel coordinates to grid cell coordinates.
	 */
	public Point pixelToGrid(Point location) {
		return new Point(location.x / cellSize, location.y / cellSize);
	}

	/**
	 * Main method which invokes a groups of another methods, to gathers the
	 * board information and controls the move of the player
	 * 
	 * @param gameState
	 */
	public void play(OnFrameInformation gameState) {
		{
			int playerNumber = 0;
			List<InitialAiPlayerInformation> playerstList = gameState
					.getPlayers();
			for (int index = 0; index < playerstList.size(); index++) {
				if (playerstList.get(index).getName().equals(playerName)) {
					playerNumber = index;
					break;
				}
			}
			if (playerInfo.size() == 0) {
				for (InitialAiPlayerInformation p : playerstList) {
					playerInfo.put(p.getName(), new AiPlayerInformation(p
							.getName(), Globals.DEFAULT_BOMB_COUNT,
							Globals.DEFAULT_BOMB_RANGE));
				}
			}
			cell = gameState.getCells();
			if (prevGameState != null) {
				prevCell = prevGameState.getCells();
				for (InitialAiPlayerInformation ips : playerstList) {
					if (ips.isDead()) {
						playerInfo.get(ips.getName()).resetBombCount();
						playerInfo.get(ips.getName()).resetBombRange();
						continue;
					}
					Point gridPos = pixelToGrid(new Point(ips.getPosition().x,
							ips.getPosition().y));
					int x = gridPos.x;
					int y = gridPos.y;
					if (x != myPosition.x || y != myPosition.y) {
						playersPositions.put(new Point(x, y), ips.getName());
					}
					if (prevCell[x][y].type == CellType.CELL_BONUS_BOMB) {
						playerInfo.get(ips.getName()).setBombCount();
					} else if (prevCell[x][y].type == CellType.CELL_BONUS_RANGE) {
						playerInfo.get(ips.getName()).setBombRange();
					}
				}
			}
			myPosition = pixelToGrid(new Point(playerstList.get(playerNumber)
					.getPosition().x, playerstList.get(playerNumber)
					.getPosition().y));
			dropBomb = false;
			bestWeight = Integer.MIN_VALUE;
			bestMove = Direction.LEFT;
			setHotPoints();
			modifiedDFS(myPosition, bestMove, 0, DFSDepth, null);
			checkMoveSave();
			if (bestMove != null) {
				checkDropBombsSafe();
				myprevPosition = bestMove;
			}

			playersPositions.clear();
			Cell[][] clonec = new Cell[cell.length][cell[0].length];
			for (int i = 0; i < cell.length; i++)
				for (int j = 0; j < cell[i].length; j++)
					clonec[i][j] = cell[i][j];
			prevGameState = new OnFrameInformation(clonec, playerstList);
			playerPrevPosition.clear();
			for (InitialAiPlayerInformation ips : playerstList) {
				Point p = ips.getPosition();
				playerPrevPosition.put(ips.getName(), new Point(p.x, p.y));
			}
		}
	}

	/**
	 * Method tries to put up a bomb in other players neighborhood
	 */
	private void checkDropBombsSafe() {
		if (myprevPosition == Direction.LEFT) {
			if (playersPositions.get(new Point(myPosition.x - 1, myPosition.y)) != null) {
				dropBomb = true;
				bestMove = Direction.RIGHT;
			}
		} else if (myprevPosition == Direction.RIGHT) {
			if (playersPositions.get(new Point(myPosition.x + 1, myPosition.y)) != null) {
				dropBomb = true;
				bestMove = Direction.LEFT;
			}
		} else if (myprevPosition == Direction.UP) {
			if (playersPositions.get(new Point(myPosition.x, myPosition.y - 1)) != null) {
				dropBomb = true;
				bestMove = Direction.DOWN;
			}
		} else if (myprevPosition == Direction.DOWN) {
			if (playersPositions.get(new Point(myPosition.x, myPosition.y + 1)) != null) {
				dropBomb = true;
				bestMove = Direction.UP;
			}
		}
	}

	/**
	 * Our player always try to move. It is harder to kill moving player, but
	 * sometimes when we surrounded by bombs or dangerous cells it is better to
	 * stay in place. So this method checks that we should stay in place or move
	 * according to modified DFS
	 */
	private void checkMoveSave() {
		if (hotBoardCells.get(new Point(myPosition.x, myPosition.y)) == null
				&& cell[myPosition.x][myPosition.y].type.isWalkable()) {
			if (bestMove == Direction.LEFT) {
				if (cell[myPosition.x - 1][myPosition.y].type.isLethal()
						|| hotBoardCells.get(new Point(myPosition.x - 1,
								myPosition.y)) != null) {
					bestMove = null;
					return;
				}
			} else if (bestMove == Direction.RIGHT) {
				if (cell[myPosition.x + 1][myPosition.y].type.isLethal()
						|| hotBoardCells.get(new Point(myPosition.x + 1,
								myPosition.y)) != null) {
					bestMove = null;
					return;
				}
			} else if (bestMove == Direction.UP) {
				if (cell[myPosition.x][myPosition.y - 1].type.isLethal()
						|| hotBoardCells.get(new Point(myPosition.x,
								myPosition.y - 1)) != null) {
					bestMove = null;
					return;
				}
			} else if (bestMove == Direction.DOWN) {
				if (cell[myPosition.x][myPosition.y + 1].type.isLethal()
						|| hotBoardCells.get(new Point(myPosition.x,
								myPosition.y + 1)) != null) {
					bestMove = null;
					return;
				}
			}
		}
	}

	/**
	 * According to players bonus, methods sets the dangerous cells
	 */
	private void setHotPoints() {
		hotBoardCells = new HashMap<Point, Point>();
		for (String ips : playerPrevPosition.keySet()) {
			Point pTranslatePosition = pixelToGrid(playerPrevPosition.get(ips));
			if (playerInfo.get(ips) != null
					&& cell[pTranslatePosition.x][pTranslatePosition.y].type == CellType.CELL_BOMB
					&& !playerInfo.get(ips).getBombsPositions().contains(
							pTranslatePosition)) {
				playerInfo.get(ips).getBombsPositions().add(pTranslatePosition);
			}
			for (int j = playerInfo.get(ips).getBombsPositions().size() - 1; j >= 0; j--) {
				Point p = playerInfo.get(ips).getBombsPositions().get(j);
				if (playerInfo.get(ips) != null
						&& cell[p.x][p.y].type.isExplosion()) {
					playerInfo.get(ips).getBombsPositions().remove(p);
				} else {
					if (playerInfo.get(ips) != null) {
						int range = playerInfo.get(ips).getBombRange() + 1;
						for (int i = 1; i < range; i++) {
							if (cell[p.x + i][p.y].type == CellType.CELL_WALL
									|| cell[p.x + i][p.y].type == CellType.CELL_BOMB
									|| cell[p.x + i][p.y].type.isExplosion()) {
								break;
							}
							if (cell[p.x + i][p.y].type == CellType.CELL_EMPTY) {
								hotBoardCells.put(new Point(p.x + i, p.y),
										new Point(p.x + i, p.y));
							}
						}
						for (int i = 1; i < range; i++) {
							if (cell[p.x - i][p.y].type == CellType.CELL_WALL
									|| cell[p.x - i][p.y].type == CellType.CELL_BOMB
									|| cell[p.x - i][p.y].type.isExplosion()) {
								break;
							}
							if (cell[p.x - i][p.y].type == CellType.CELL_EMPTY) {
								hotBoardCells.put(new Point(p.x - i, p.y),
										new Point(p.x - i, p.y));
							}
						}
						for (int i = 1; i < range; i++) {
							if (cell[p.x][p.y + i].type == CellType.CELL_WALL
									|| cell[p.x][p.y + i].type == CellType.CELL_BOMB
									|| cell[p.x][p.y + i].type.isExplosion()) {
								break;
							}
							if (cell[p.x][p.y + i].type == CellType.CELL_EMPTY) {
								hotBoardCells.put(new Point(p.x, p.y + i),
										new Point(p.x, p.y + i));
							}
						}
						for (int i = 1; i < range; i++) {
							if (cell[p.x][p.y - i].type == CellType.CELL_WALL
									|| cell[p.x][p.y - i].type == CellType.CELL_BOMB
									|| cell[p.x][p.y - i].type.isExplosion()) {
								break;
							}
							if (cell[p.x][p.y - 1].type == CellType.CELL_EMPTY) {
								hotBoardCells.put(new Point(p.x, p.y - i),
										new Point(p.x, p.y - i));
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 * Main method which choose the best way to move. Method recurrently build
	 * the movement tree and estimate all the weights to every of the leafs.
	 * After that it returns the move with the highest weight
	 * 
	 * @param startPoint
	 * @param prevDirection
	 * @param weight
	 * @param depth
	 * @param firstMove
	 */
	private void modifiedDFS(Point startPoint, Direction prevDirection,
			int weight, int depth, Direction firstMove) {
		if (depth == 0) {
			if (bestWeight < weight) {
				bestWeight = weight;
				bestMove = firstMove;
			}
			return;
		}
		if (hotBoardCells.get(new Point(startPoint.x + 1, startPoint.y)) != null) {
			if (depth == DFSDepth) {
				firstMove = Direction.RIGHT;
			}
			if (myprevPosition.equals(Direction.RIGHT) && depth == DFSDepth) {
				modifiedDFS(new Point(startPoint.x + 1, startPoint.y),
						Direction.RIGHT, weight - 30, depth - 1, firstMove);
			} else {
				modifiedDFS(new Point(startPoint.x + 1, startPoint.y),
						Direction.RIGHT, weight - 40, depth - 1, firstMove);
			}
		} else if (cell[startPoint.x + 1][startPoint.y].type
				.equals(CellType.CELL_BONUS_BOMB)
				|| cell[startPoint.x + 1][startPoint.y].type
						.equals(CellType.CELL_BONUS_RANGE)) {
			if (depth == DFSDepth) {
				firstMove = Direction.RIGHT;
			}
			if (myprevPosition.equals(Direction.RIGHT) && depth == DFSDepth) {
				modifiedDFS(new Point(startPoint.x + 1, startPoint.y),
						Direction.RIGHT, weight + 10, depth - 1, firstMove);
			} else {
				modifiedDFS(new Point(startPoint.x + 1, startPoint.y),
						Direction.RIGHT, weight + 6, depth - 1, firstMove);
			}
		} else if (playersPositions.get(new Point(startPoint.x + 1,
				startPoint.y)) != null) {
			if (depth == DFSDepth) {
				firstMove = Direction.RIGHT;
			}
			if (myprevPosition.equals(Direction.RIGHT) && depth == DFSDepth) {
				modifiedDFS(new Point(startPoint.x + 1, startPoint.y),
						Direction.RIGHT, weight + 12, depth - 1, firstMove);
			} else {
				modifiedDFS(new Point(startPoint.x + 1, startPoint.y),
						Direction.RIGHT, weight + 7, depth - 1, firstMove);
			}

		} else if (cell[startPoint.x + 1][startPoint.y].type
				.equals(CellType.CELL_EMPTY)) {
			if (depth == DFSDepth) {
				firstMove = Direction.RIGHT;
			}
			if (myprevPosition.equals(Direction.RIGHT) && depth == DFSDepth) {
				modifiedDFS(new Point(startPoint.x + 1, startPoint.y),
						Direction.RIGHT, weight + 2, depth - 1, firstMove);
			} else {
				modifiedDFS(new Point(startPoint.x + 1, startPoint.y),
						Direction.RIGHT, weight + 1, depth - 1, firstMove);
			}
		}

		if (hotBoardCells.get(new Point(startPoint.x, startPoint.y - 1)) != null) {
			if (depth == DFSDepth) {
				firstMove = Direction.UP;
			}
			if (myprevPosition.equals(Direction.UP) && depth == DFSDepth) {
				modifiedDFS(new Point(startPoint.x, startPoint.y - 1),
						Direction.UP, weight - 30, depth - 1, firstMove);
			} else {
				modifiedDFS(new Point(startPoint.x, startPoint.y - 1),
						Direction.UP, weight - 40, depth - 1, firstMove);
			}

		} else if (cell[startPoint.x][startPoint.y - 1].type
				.equals(CellType.CELL_BONUS_BOMB)
				|| cell[startPoint.x][startPoint.y - 1].type
						.equals(CellType.CELL_BONUS_RANGE)) {
			if (depth == DFSDepth) {
				firstMove = Direction.UP;
			}
			if (myprevPosition.equals(Direction.UP) && depth == DFSDepth) {
				modifiedDFS(new Point(startPoint.x, startPoint.y - 1),
						Direction.UP, weight + 10, depth - 1, firstMove);
			} else {
				modifiedDFS(new Point(startPoint.x, startPoint.y - 1),
						Direction.UP, weight + 6, depth - 1, firstMove);
			}
		} else if (playersPositions.get(new Point(startPoint.x,
				startPoint.y - 1)) != null) {
			if (depth == DFSDepth) {
				firstMove = Direction.UP;
			}
			if (myprevPosition.equals(Direction.UP) && depth == DFSDepth) {
				modifiedDFS(new Point(startPoint.x, startPoint.y - 1),
						Direction.UP, weight + 12, depth - 1, firstMove);
			} else {
				modifiedDFS(new Point(startPoint.x, startPoint.y - 1),
						Direction.UP, weight + 7, depth - 1, firstMove);
			}
		} else if (cell[startPoint.x][startPoint.y - 1].type
				.equals(CellType.CELL_EMPTY)) {
			if (depth == DFSDepth) {
				firstMove = Direction.UP;
			}
			if (myprevPosition.equals(Direction.UP) && depth == DFSDepth) {
				modifiedDFS(new Point(startPoint.x, startPoint.y - 1),
						Direction.UP, weight + 2, depth - 1, firstMove);
			} else {
				modifiedDFS(new Point(startPoint.x, startPoint.y - 1),
						Direction.UP, weight + 1, depth - 1, firstMove);
			}
		}

		if (hotBoardCells.get(new Point(startPoint.x, startPoint.y + 1)) != null) {
			if (depth == DFSDepth) {
				firstMove = Direction.DOWN;
			}
			if (myprevPosition.equals(Direction.DOWN) && depth == DFSDepth) {
				modifiedDFS(new Point(startPoint.x, startPoint.y + 1),
						Direction.DOWN, weight - 30, depth - 1, firstMove);
			} else {
				modifiedDFS(new Point(startPoint.x, startPoint.y + 1),
						Direction.DOWN, weight - 40, depth - 1, firstMove);
			}
		} else if (cell[startPoint.x][startPoint.y + 1].type
				.equals(CellType.CELL_BONUS_BOMB)
				|| cell[startPoint.x][startPoint.y + 1].type
						.equals(CellType.CELL_BONUS_RANGE)) {
			if (depth == DFSDepth) {
				firstMove = Direction.DOWN;
			}
			if (myprevPosition.equals(Direction.DOWN) && depth == DFSDepth) {
				modifiedDFS(new Point(startPoint.x, startPoint.y + 1),
						Direction.DOWN, weight + 10, depth - 1, firstMove);
			} else {
				modifiedDFS(new Point(startPoint.x, startPoint.y + 1),
						Direction.DOWN, weight + 6, depth - 1, firstMove);
			}
		} else if (playersPositions.get(new Point(startPoint.x,
				startPoint.y + 1)) != null) {
			if (depth == DFSDepth) {
				firstMove = Direction.DOWN;
			}
			if (myprevPosition.equals(Direction.DOWN) && depth == DFSDepth) {
				modifiedDFS(new Point(startPoint.x, startPoint.y + 1),
						Direction.DOWN, weight + 12, depth - 1, firstMove);
			} else {
				modifiedDFS(new Point(startPoint.x, startPoint.y + 1),
						Direction.DOWN, weight + 7, depth - 1, firstMove);
			}
		} else if (cell[startPoint.x][startPoint.y + 1].type
				.equals(CellType.CELL_EMPTY)) {
			if (depth == DFSDepth) {
				firstMove = Direction.DOWN;
			}
			if (myprevPosition.equals(Direction.DOWN) && depth == DFSDepth) {
				modifiedDFS(new Point(startPoint.x, startPoint.y + 1),
						Direction.DOWN, weight + 2, depth - 1, firstMove);
			} else {
				modifiedDFS(new Point(startPoint.x, startPoint.y + 1),
						Direction.DOWN, weight + 1, depth - 1, firstMove);
			}
		}

		if (hotBoardCells.get(new Point(startPoint.x - 1, startPoint.y)) != null) {
			if (depth == DFSDepth) {
				firstMove = Direction.LEFT;
			}
			if (myprevPosition.equals(Direction.LEFT) && depth == DFSDepth) {
				modifiedDFS(new Point(startPoint.x - 1, startPoint.y),
						Direction.LEFT, weight - 30, depth - 1, firstMove);
			} else {
				modifiedDFS(new Point(startPoint.x - 1, startPoint.y),
						Direction.LEFT, weight - 40, depth - 1, firstMove);
			}
		} else if (cell[startPoint.x - 1][startPoint.y].type
				.equals(CellType.CELL_BONUS_BOMB)
				|| cell[startPoint.x - 1][startPoint.y].type
						.equals(CellType.CELL_BONUS_RANGE)) {
			if (depth == DFSDepth) {
				firstMove = Direction.LEFT;
			}
			if (myprevPosition.equals(Direction.LEFT) && depth == DFSDepth) {
				modifiedDFS(new Point(startPoint.x - 1, startPoint.y),
						Direction.LEFT, weight + 10, depth - 1, firstMove);
			} else {
				modifiedDFS(new Point(startPoint.x - 1, startPoint.y),
						Direction.LEFT, weight + 6, depth - 1, firstMove);
			}
		} else if (playersPositions.get(new Point(startPoint.x - 1,
				startPoint.y)) != null) {
			if (depth == DFSDepth) {
				firstMove = Direction.LEFT;
			}
			if (myprevPosition.equals(Direction.LEFT) && depth == DFSDepth) {
				modifiedDFS(new Point(startPoint.x - 1, startPoint.y),
						Direction.LEFT, weight + 12, depth - 1, firstMove);
			} else {
				modifiedDFS(new Point(startPoint.x - 1, startPoint.y),
						Direction.LEFT, weight + 7, depth - 1, firstMove);
			}
		} else if (cell[startPoint.x - 1][startPoint.y].type
				.equals(CellType.CELL_EMPTY)) {
			if (depth == DFSDepth) {
				firstMove = Direction.LEFT;
			}
			if (myprevPosition.equals(Direction.LEFT) && depth == DFSDepth) {
				modifiedDFS(new Point(startPoint.x - 1, startPoint.y),
						Direction.LEFT, weight + 2, depth - 1, firstMove);
			} else {
				modifiedDFS(new Point(startPoint.x - 1, startPoint.y),
						Direction.LEFT, weight + 1, depth - 1, firstMove);
			}
		}
	}

	/**
	 * @return Current direction or <code>null</code> if none.
	 */
	@Override
	public boolean dropsBomb() {
		return dropBomb;
	}

	/**
	 * @return Return <code>true</code> if this player wants to drop a bomb at
	 *         the current location.
	 */
	@Override
	public Direction getCurrent() {
		return bestMove;
	}

}
