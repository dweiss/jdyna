package ai.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdyna.CellType;
import org.jdyna.IPlayerController.Direction;

import ai.board.BombCell;
import ai.board.EditableCell;
import ai.player.PlayerInfo;

/**
 * 
 * @author Slawek
 */
public class BombUtility {

	/**
	 * 
	 * returns all bombs in line ( vertical,horizontal) that can explode and
	 * kill us
	 * 
	 * @param position
	 * @param cells
	 * @return
	 */
	public List<BombCell> getBombCellInLine(int[] position,
			EditableCell[][] cells) {

		int[] cellPosition;
		int distance;
		List<Direction> availableDirectionList;
		List<BombCell> bombCellList = new ArrayList<BombCell>();
		Direction[] directions = Direction.values();
		for (Direction dir : directions) {
			cellPosition = position.clone();
			while (true) {
				availableDirectionList = GameUtility.getAvailableDirections(
						cellPosition, cells);
				if (availableDirectionList.contains(dir)) {
					cellPosition = MoveUtility.moveUser(cellPosition, dir);
				} else {
					// jakas sciana/bomba..
					cellPosition = MoveUtility.moveUser(cellPosition, dir);
					if (cells[cellPosition[0]][cellPosition[1]].type
							.equals(CellType.CELL_BOMB)) {
						BombCell bombCell = (BombCell) cells[cellPosition[0]][cellPosition[1]];
						distance = MathUtility.getLineDistance(position,
								cellPosition);
						if (bombCell.lazy) {
							break;
						} else {

							if (distance > bombCell.range) {

							} else {
								bombCellList.add(bombCell);
							}
							break;
						}

					} else {
						break;
					}
				}
			}
		}
		return bombCellList;
	}

	/**
	 * gets bomb owner
	 * 
	 * 
	 * 
	 * @param position -
	 *            bomb position
	 * @param players
	 * @param myPlayer -
	 *            our Player
	 * @return
	 */
	public PlayerInfo getBombOwner(int[] position,
			Map<String, PlayerInfo> players, PlayerInfo myPlayer) {

		if (myPlayer.position[0] == position[0]
				&& myPlayer.position[1] == position[1]) {
			return myPlayer;
		}

		for (PlayerInfo playerInfo : players.values()) {
			if (playerInfo.position[0] == position[0]
					&& playerInfo.position[1] == position[1]) {
				return playerInfo;
			}
		}

		return null;
	}

	/**
	 * 
	 * returns BombCells in line
	 * 
	 * @param position
	 * @param direction
	 * @param cells
	 * @return
	 */
	public BombCell getBombCellInLine(int[] position, Direction direction,
			EditableCell[][] cells) {
		int[] cellPosition = position.clone();
		List<Direction> availableDirectionList;
		if (direction == null) {
			return null;
		}
		while (true) {
			availableDirectionList = GameUtility.getAvailableDirections(
					cellPosition, cells);
			if (availableDirectionList.contains(direction)) {
				cellPosition = MoveUtility.moveUser(cellPosition, direction);
			} else {
				// a wall/bomb
				cellPosition = MoveUtility.moveUser(cellPosition, direction);
				if (cells[cellPosition[0]][cellPosition[1]].type
						.equals(CellType.CELL_BOMB)) {
					BombCell bombCell = (BombCell) cells[cellPosition[0]][cellPosition[1]];
					if (bombCell.lazy) {
						return null;
					} else {
						return bombCell;
					}

				} else {
					return null;
				}
			}
		}
	}
}
