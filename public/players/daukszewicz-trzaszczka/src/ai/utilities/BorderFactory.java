package ai.utilities;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdyna.*;
import org.jdyna.IPlayerController.Direction;

import ai.board.BombCell;
import ai.board.EditableCell;
import ai.player.PlayerInfo;


/**
 * 
 * 
 * handles sound events (bombs,bonuses) and initialize border, convert
 * uneditable Border to special editable order which is used in path search.
 * 
 * @author Asia
 * 
 */
public class BorderFactory {

	private BombUtility bombUtility;

	/**
	 * quantity of pixels per one frame
	 */
	private final static int PIXEL_STEP = 2;

	/**
	 * maximum opponent bombRange
	 */
	private int maxBombRange;

	public BorderFactory(BombUtility bu) {
		this.bombUtility = bu;
	}

	public int getMaxBombRange() {
		return maxBombRange;
	}

	/**
	 * 
	 * convert snapsho built with Cell[][] to EditableCell[][]
	 * 
	 * 
	 * @param cells
	 * @param frame
	 * @return
	 */
	public EditableCell[][] convert(Cell[][] cells, int frame) {
		EditableCell[][] result = new EditableCell[cells.length][cells[0].length];
		EditableCell editableCell;
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {

				editableCell = new EditableCell(cells[i][j].type);
				editableCell.id = -1;
				editableCell.length = Integer.MAX_VALUE;
				if (cells[i][j].type.equals(CellType.CELL_BOMB)) {
					BombCell bombCell = new BombCell(CellType.CELL_BOMB);
					if (cells[i][j].type.code == 'B') {
						// lazy
						bombCell.lazy = true;
					} else {
						// tickin' bombed
						bombCell.lazy = false;
					}

					bombCell.x = i;
					bombCell.y = j;
					bombCell.range = Globals.DEFAULT_BOMB_RANGE;
					bombCell.explosionFrame = Globals.DEFAULT_FUSE_FRAMES
							+ frame;
					result[i][j] = bombCell;
				} else {
					result[i][j] = editableCell;
				}
			}

		}
		return result;
	}

	/**
	 * 
	 * handle sound effects like bonus, death
	 * 
	 * @param cells
	 * @param myCells
	 * @param players
	 * @param se
	 * @param count
	 * @param frame
	 */
	public void handleSoundEffects(Cell[][] cells, EditableCell[][] myCells,
			Map<String, PlayerInfo> players, SoundEffect soundEffect,
			PlayerInfo myPlayer, int count, int frame) {
		if (soundEffect.equals(SoundEffect.BONUS)) {
			for (int i = 0; i < myCells.length; i++) {
				for (int j = 0; j < myCells[i].length; j++) {
					// oponents
					if (myCells[i][j].type.equals(CellType.CELL_BONUS_BOMB)) {
						for (PlayerInfo playerInfo : players.values()) {
							if (playerInfo.position[0] == i
									&& playerInfo.position[1] == j) {
								playerInfo.bombCount++;
							}
						}
					} else if (myCells[i][j].type
							.equals(CellType.CELL_BONUS_RANGE)) {
						for (PlayerInfo playerInfo : players.values()) {
							if (playerInfo.position[0] == i
									&& playerInfo.position[1] == j) {
								playerInfo.bombRange++;
								if (maxBombRange < playerInfo.bombRange) {
									maxBombRange = playerInfo.bombRange;
								}
							}
						}
					}
				}
			}
			// my user
			if (myCells[myPlayer.position[0]][myPlayer.position[1]].type
					.equals(CellType.CELL_BONUS_BOMB)) {
				myPlayer.bombCount++;
			} else if (myCells[myPlayer.position[0]][myPlayer.position[1]].type
					.equals(CellType.CELL_BONUS_RANGE)) {
				myPlayer.bombRange++;
			}
		} else if (soundEffect.equals(SoundEffect.DYING)) {
			for (String key : players.keySet()) {
				PlayerInfo playerInfo = players.get(key);
				if (playerInfo.frame != frame) {
					players.remove(key);
				}
			}
		}
	}

	/**
	 * 
	 * updates snapshot state
	 * 
	 * @param cells
	 * @param myCells
	 * @param players
	 * @param frame
	 */
	public void update(Cell[][] cells, EditableCell[][] myCells,
			Map<String, PlayerInfo> players, PlayerInfo myPlayer, int frame) {
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				if (cells[i][j].type.equals(CellType.CELL_BOMB)) {
					if (!myCells[i][j].type.equals(CellType.CELL_BOMB)) {
						PlayerInfo bombOwner = bombUtility.getBombOwner(
								new int[] { i, j }, players, myPlayer);
						BombCell newBombCell = new BombCell(CellType.CELL_BOMB);
						newBombCell.x = i;
						newBombCell.y = j;
						if (bombOwner == null) {
							newBombCell.range = maxBombRange;
						} else {
							newBombCell.range = bombOwner.bombRange;
						}
						newBombCell.explosionFrame = Globals.DEFAULT_FUSE_FRAMES + frame;
						myCells[i][j] = newBombCell;

						int[] position = new int[] { i, j };
						List<BombCell> bombCells = bombUtility.getBombCellInLine(
								position, myCells);
						int minFrame = newBombCell.explosionFrame;
						int[] bcPosition;
						int distance;
						for (int e = bombCells.size() - 1; e >= 0; e--) {
							BombCell bombCell = bombCells.get(e);
							bcPosition = new int[] { bombCell.x, bombCell.y };
							distance = MathUtility.getLineDistance(bcPosition,
									position);
							if (distance <= bombCell.range
									|| distance <= newBombCell.range) {
								if (bombCell.explosionFrame < minFrame) {
									minFrame = bombCell.explosionFrame;
								}
							} else {
								bombCells.remove(e);
							}
						}

						for (BombCell bombCell : bombCells) {
							bombCell.explosionFrame = minFrame;
						}

						newBombCell.explosionFrame = minFrame;

					}
				} else {
					myCells[i][j] = new EditableCell(cells[i][j].type);
				}
			}
		}
	}

	/**
	 * 
	 * checks if the two points are in 'walkable' line
	 * 
	 * 
	 * @param cells
	 * @param pos1
	 * @param pos2
	 * @return
	 */
	public boolean areInLine(EditableCell[][] cells, int[] pos1, int[] pos2) {
		int start;
		int end;

		if (pos1[0] == pos2[0]) {
			// vertical
			if (pos1[1] > pos2[1]) {
				start = pos2[1];
				end = pos1[1];
			} else {
				start = pos1[1];
				end = pos2[1];
			}

			for (int i = start; i <= end; i++) {
				if (!cells[pos1[0]][i].type.isWalkable()) {
					return false;
				}
			}
			return true;

		} else if (pos1[1] == pos2[1]) {
			// horizontal
			if (pos1[0] > pos2[0]) {
				start = pos2[0];
				end = pos1[0];
			} else {
				start = pos1[0];
				end = pos2[0];
			}

			for (int i = start; i <= end; i++) {
				if (!cells[i][pos1[1]].type.isWalkable()) {
					return false;
				}
			}
			return true;
		}

		return false;
	}

	public boolean isOnBomb(EditableCell[][] cells, int position[]) {

		if (cells[position[0]][position[1]].type.equals(CellType.CELL_BOMB)) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * calculate nr of frames to next cell from startCellPosition in given
	 * direction
	 * 
	 * 
	 * @param startCellPosition -
	 *            start position( in pixels)
	 * @param startPoint -
	 *            start position( in cells)
	 * @param direction
	 * @return
	 */
	public int getFramesCountToNextCell(int[] startCellPosition,
			Point startPoint, Direction direction) {

		int borderCell = 0;
		int cellNr;
		int pixel;
		if (direction == null) {
			return -1;
		}
		if (direction.equals(Direction.RIGHT)
				|| direction.equals(Direction.LEFT)) {
			pixel = startPoint.x;
			cellNr = startCellPosition[0];
		} else {
			pixel = startPoint.y;
			cellNr = startCellPosition[1];
		}

		if (direction.equals(Direction.RIGHT)
				|| direction.equals(Direction.DOWN)) {
			borderCell = (cellNr + 1) * Globals.DEFAULT_CELL_SIZE;
		} else {
			borderCell = ((cellNr) * Globals.DEFAULT_CELL_SIZE) - 1;
		}
		return Math.abs(borderCell - pixel) / PIXEL_STEP;
	}

	/**
	 * 
	 * returns pixels to the next curve in line cause avoid the bomb !
	 * 
	 * 
	 * @param startCell
	 * @param cells
	 * @param startPoint
	 * @param direction
	 * @return
	 */
	public List<Integer> getPixelDistanceToCrossesInLine(int[] startCell,
			EditableCell[][] cells, Point startPoint, Direction direction) {
		if (direction == null) {
			return null;
		}
		int result[] = MoveUtility.moveUser(startCell.clone(), direction);
		Point pointCopy = (Point) startPoint.clone();
		int pixelDistance = getFramesCountToNextCell(startCell, pointCopy,
				direction)
				* PIXEL_STEP;
		pointCopy = MoveUtility.moveUser(pointCopy, pixelDistance, direction);
		List<Direction> availDirections;
		List<Integer> resultList = new ArrayList<Integer>();
		while (true) {
			availDirections = GameUtility.getAvailableDirections(result, cells);
			availDirections.remove(MathUtility.getOpositeDirection(direction));

			if (availDirections.size() >= 1) {
				for (Direction dir : availDirections) {
					if (!dir.equals(direction)) {
						int temp = getFramesCountToNextCell(result.clone(),
								pointCopy, dir)
								* PIXEL_STEP;
						resultList.add(pixelDistance + temp);
					}
				}
			}

			if (availDirections.contains(direction)) {
				pixelDistance += (getFramesCountToNextCell(result, pointCopy,
						direction) * PIXEL_STEP);
				result = MoveUtility.moveUser(result.clone(), direction);
				pointCopy = MoveUtility.moveUser((Point) startPoint.clone(),
						pixelDistance, direction);
			} else {
				break;
			}
		}
		return resultList;
	}

	/**
	 * 
	 * just as method getPixelDistanceToCrossesInLine calculate distance by in
	 * cells unit
	 * 
	 * 
	 * @param startCell
	 * @param cells
	 * @param direction
	 * @return
	 */
	public List<int[]> getCellDistanceToCrossesInLine(int[] startCell,
			EditableCell[][] cells, Direction direction) {
		if (direction == null) {
			return null;
		}
		int cellPosition[] = MoveUtility.moveUser(startCell.clone(), direction);
		List<Direction> availDirections;
		List<int[]> resultList = new ArrayList<int[]>();
		while (true) {
			availDirections = GameUtility.getAvailableDirections(cellPosition,
					cells);
			availDirections.remove(MathUtility.getOpositeDirection(direction));

			if (availDirections.size() > 1
					|| (availDirections.size() == 1 && !availDirections.get(0)
							.equals(direction))) {

				for (Direction dir : availDirections) {
					if (!dir.equals(direction)) {
						resultList.add(cellPosition);
					}
				}

			}

			if (availDirections.contains(direction)) {
				cellPosition = MoveUtility.moveUser(cellPosition.clone(),
						direction);
			} else {
				break;
			}
		}
		return resultList;
	}
}
