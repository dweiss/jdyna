package ai.utilities;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.jdyna.IPlayerController.Direction;

import ai.board.BombCell;
import ai.board.EditableCell;


/**
 * 
 * produces path to some place with avoiding dangerous place
 * 
 * @author Slawek
 * 
 */
public class PathUtility {

	private BombUtility bombUtility;
	private GameUtility gameUtility;
	private BorderFactory borderFactory;

	private final int PIXEL_STEP;
	/**
	 * global value represents nr of results in recursive search path
	 */
	int resultCounter = 0;

	/**
	 * number of results received from recursive search path to point
	 */
	private final static int NUMBER_OF_RESULTS = 1;

	/**
	 * global the bests value of the recursive search path
	 */
	int globalMinResult = Integer.MAX_VALUE;

	public PathUtility(BombUtility bombUtility, GameUtility gameUtility,
			BorderFactory borderFactory) {
		this.bombUtility = bombUtility;
		this.gameUtility = gameUtility;
		this.borderFactory = borderFactory;
		PIXEL_STEP = 2;
	}

	public void setResultCounter(int resultCounter) {
		this.resultCounter = resultCounter;
	}

	public void setGlobalMinResult(int globalMinResult) {
		this.globalMinResult = globalMinResult;
	}

	/**
	 * 
	 * 
	 * returns bombed Directions which are dangerous
	 * 
	 * 
	 * @param userPos
	 * @param userPointPos
	 * @param availableDirections
	 * @param cells
	 * @param frame
	 * @return
	 */
	public List<Direction> getBombedDirections(int[] userPos,
			Point userPointPos, List<Direction> availableDirections,
			EditableCell[][] cells, int frame) {
		List<Direction> result = new ArrayList<Direction>();
		List<Direction> copyAvailableDirections = new ArrayList<Direction>(
				availableDirections);
		Direction dir;
		BombCell bomb;
		for (int i = copyAvailableDirections.size() - 1; i >= 0; i--) {
			dir = copyAvailableDirections.get(i);
			bomb = bombUtility.getBombCellInLine(userPos, dir, cells);
			if (bomb == null) {
				bomb = bombUtility.getBombCellInLine(userPos, MathUtility
						.getOpositeDirection(dir), cells);
			}
			if (bomb != null) {
				List<Integer> crossesInPixels = borderFactory
						.getPixelDistanceToCrossesInLine(userPos, cells,
								userPointPos, dir);
				List<int[]> crossesInCells = borderFactory
						.getCellDistanceToCrossesInLine(userPos, cells, dir);
				for (int j = crossesInPixels.size() - 1; j >= 0; j--) {
					if (MathUtility.getLineDistance(crossesInCells.get(j),
							new int[] { bomb.x, bomb.y }) <= bomb.range
							&& crossesInPixels.get(j).intValue() + frame > bomb.explosionFrame) {
						crossesInPixels.remove(j);
						crossesInCells.remove(j);
					}
				}
				if (crossesInPixels.isEmpty()) {
					copyAvailableDirections.remove(dir);
					result.add(dir);
				}
			}
		}
		return result;
	}

	public Direction getShortestDirection(List<Direction> aD,
			Point myPointPosition, int[] myCellPosition,
			int[] oponentCellPosition, EditableCell[][] myCells, int id,
			int frame) {

		Direction direction = null;

		int min = Integer.MAX_VALUE;
		int length;
		id = frame + 1;
		myCells[myCellPosition[0]][myCellPosition[1]].id = id;

		globalMinResult = Integer.MAX_VALUE;

		for (Direction dir : aD) {
			resultCounter = 0;
			length = getPath(myCells, MoveUtility.moveUser(myCellPosition.clone(),
					dir), myPointPosition, oponentCellPosition, dir, frame, id,
					0);

			if (length >= 0 && length < min) {
				direction = dir;
				min = length;
				globalMinResult = min;
			}
		}

		return direction;
	}

	/**
	 * 
	 * returns directions to neighbouring cells which are in lethal or explosion
	 * state
	 * 
	 * @param pos
	 * @param cells
	 * @param availableDirections
	 * @return
	 */
	public List<Direction> getBlockedDirections(int pos[],
			EditableCell[][] cells, List<Direction> availableDirections) {
		List<Direction> directions = new ArrayList<Direction>();
		EditableCell cell;
		int[] newPosition;
		for (Direction direction : availableDirections) {
			newPosition = MoveUtility.moveUser(pos.clone(), direction);
			cell = cells[newPosition[0]][newPosition[1]];
			if (cell.type.isLethal() || cell.type.isExplosion()) {
				directions.add(direction);
			}
		}
		return directions;
	}

	/**
	 * 
	 * returns directions which in next step will be Explosion
	 * 
	 * 
	 * @param userPos
	 * @param userPointPos
	 * @param availableDirections
	 * @param cells
	 * @param frame
	 * @return
	 */
	public List<Direction> getLethalDirections(int[] userPos,
			Point userPointPos, List<Direction> availableDirections,
			EditableCell[][] cells, int frame) {
		int nextFrame = frame + 1;
		List<Direction> result = new ArrayList<Direction>();
		List<Direction> copyAvailableDirections = new ArrayList<Direction>(
				availableDirections);

		int distance;
		List<BombCell> bombCells;

		Direction dir;
		for (int i = copyAvailableDirections.size() - 1; i >= 0; i--) {
			Point clone = (Point) userPointPos.clone();
			int[] newPosition = userPos.clone();

			dir = copyAvailableDirections.get(i);
			// System.out.print(dir+"-"+copyAvailableDirections);
			int frameDistance = borderFactory.getFramesCountToNextCell(
					newPosition, clone, dir);
			newPosition = MoveUtility.moveUser(clone, dir, cells, PIXEL_STEP);
			clone = MoveUtility.moveUser(clone, frameDistance, dir);
			if (newPosition[0] == userPos[0] && newPosition[1] == userPos[1]) {
				continue;
			}
			nextFrame += frameDistance;
			bombCells = bombUtility.getBombCellInLine(newPosition, cells);
			for (BombCell bc : bombCells) {
				distance = MathUtility.getLineDistance(newPosition, new int[] {
						bc.x, bc.y });
				if ((bc.explosionFrame == nextFrame)
						|| ((bc.explosionFrame < nextFrame && bc.explosionFrame + 14 > nextFrame))
						&& bc.range + 1 > distance) {
					result.add(dir);
				}
			}
		}

		return result;
	}

	/**
	 * 
	 * returns dangerous directions
	 * 
	 * 
	 * @param userPos
	 * @param userPointPos
	 * @param availableDirections
	 * @param cells
	 * @param frame
	 * @return
	 */
	public List<Direction> getBlockedDirection2(int[] userPos,
			Point userPointPos, List<Direction> availableDirections,
			EditableCell[][] cells, int frame) {
		List<Direction> result = new ArrayList<Direction>();
		int[] clonedPosition = userPos.clone();
		int distance;
		List<BombCell> bombCells;

		bombCells = bombUtility.getBombCellInLine(clonedPosition, cells);

		int timeToExplosion;
		for (BombCell bc : bombCells) {
			distance = MathUtility.getLineDistance(clonedPosition, new int[] {
					bc.x, bc.y });
			if (bc.range + 1 > distance) {
				// jestesmy w zasiegu
				timeToExplosion = bc.explosionFrame - frame;
				for (Direction dir : availableDirections) {
					int framesCountToNextCell = borderFactory
							.getFramesCountToNextCell(clonedPosition.clone(),
									(Point) userPointPos.clone(), dir);
					if (timeToExplosion <= framesCountToNextCell) {
						result.add(dir);
					}
				}
			}
		}
		return result;
	}

	public int getPath(EditableCell[][] cells, int userPos[],
			Point userPointPos, int oponentPos[], Direction direction,
			int frame, int id, int cellDistance) {

		if (resultCounter >= NUMBER_OF_RESULTS) {
			return -13;
		}
		int pixelPosition = frame;
		int pixelDistance;
		List<Direction> availableDirections;
		int distance;
		EditableCell cell;

		while (userPos[0] != oponentPos[0] || userPos[1] != oponentPos[1]) {
			cell = cells[userPos[0]][userPos[1]];
			if (cell.id == id && cell.length < cellDistance) {
				// byl ktos inny
				resultCounter++;
				return -5;
			}

			if (MathUtility.getManhatanDistance(userPos, oponentPos)
					+ cellDistance >= globalMinResult) {
				return -6;
			}

			cell.id = id;
			cell.length = cellDistance;
			availableDirections = GameUtility.getAvailableDirections(userPos,
					cells);
			availableDirections.remove(MathUtility
					.getOpositeDirection(direction));
			List<Direction> bombedDirections = getBombedDirections(userPos,
					userPointPos, availableDirections, cells, pixelPosition);
			availableDirections.removeAll(bombedDirections);

			if (availableDirections.isEmpty()) {
				// slepa
				resultCounter++;
				return -3;
			}

			if (cellDistance >= globalMinResult) {
				resultCounter++;
				return -4;
			}

			if (availableDirections.size() == 1) {
				// korytarz
				direction = availableDirections.get(0);
				pixelDistance = borderFactory.getFramesCountToNextCell(userPos,
						userPointPos, direction);
				userPointPos = MoveUtility.moveUser(userPointPos, pixelDistance
						* PIXEL_STEP, direction);
				userPos = MoveUtility.moveUser(userPos, direction);
				pixelPosition += pixelDistance;
				cellDistance++;
			} else if (availableDirections.size() > 1) {
				// skrzyzowanie

				availableDirections = gameUtility.sortDirections(userPos,
						oponentPos, availableDirections);

				for (int i = 0; i < availableDirections.size(); i++) {
					Direction dir = availableDirections.get(i);
					cellDistance++;
					distance = getPath(cells, MoveUtility.moveUser(userPos
							.clone(), dir), userPointPos, oponentPos, dir,
							pixelPosition + 1, id, cellDistance);

					if ((distance >= 0 && distance < globalMinResult)) {
						resultCounter++;
						globalMinResult = distance;
					}
					if (resultCounter >= NUMBER_OF_RESULTS) {
						return globalMinResult;
					}

				}
				return globalMinResult;
			}
		}
		resultCounter++;
		return cellDistance;
	}

	/**
	 * 
	 * checks if we are in bomb range and we are safe
	 * 
	 * @param cells
	 * @param userPosition
	 * @param userPointPosition
	 * @param frame
	 * @return
	 */
	public boolean isSafePosition(EditableCell[][] cells, int[] userPosition,
			Point userPointPosition, int frame) {
		List<BombCell> bombCells = bombUtility.getBombCellInLine(userPosition,
				cells);
		for (BombCell bombCell : bombCells) {
			if (bombCell.range >= MathUtility.getLineDistance(
					new int[] { bombCell.x, bombCell.y }, userPosition)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * return Direction directed us to safe position
	 * 
	 * @param cells
	 * @param userPos
	 * @param userPointPos
	 * @param directions
	 * @param frame
	 * @param id
	 * @return
	 */
	public List<Direction> getSafeDirections(EditableCell[][] cells,
			int userPos[], Point userPointPos, List<Direction> directions,
			int frame, int id) {

		List<Direction> result = new ArrayList<Direction>();

		int distance;

		for (Direction dir : directions) {
			int[] nextCell = MoveUtility.moveUser(userPos.clone(), dir);
			distance = borderFactory.getFramesCountToNextCell(userPos,
					userPointPos, dir);
			Point nextPoint = MoveUtility.moveUser(
					(Point) userPointPos.clone(), distance * PIXEL_STEP, dir);
			if (isSafeCell(cells, nextCell, nextPoint, dir, frame + distance,
					id)) {
				result.add(dir);
			}
		}
		return result;
	}

	/**
	 * 
	 * checks if given cell is safe- if we can go from it to other safe cell
	 * 
	 * @param cells
	 * @param userPos
	 * @param userPointPos
	 * @param direction
	 * @param frame
	 * @param id
	 * @return
	 */
	public boolean isSafeCell(EditableCell[][] cells, int userPos[],
			Point userPointPos, Direction direction, int frame, int id) {

		int pixelPosition = frame;
		int pixelDistance;
		List<Direction> availableDirections;
		EditableCell cell;

		while (!isSafePosition(cells, userPos, userPointPos, frame)) {
			// System.out.print("["+length+"?"+minResult+"?"+globalMinResult+"]")
			// ;
			cell = cells[userPos[0]][userPos[1]];
			if (cell.id == id) {
				// byl ktos inny
				resultCounter++;
				return false;
			}

			cell.id = id;
			availableDirections = GameUtility.getAvailableDirections(userPos,
					cells);
			availableDirections.remove(MathUtility
					.getOpositeDirection(direction));
			List<Direction> bombedDirections = getBombedDirections(userPos,
					userPointPos, availableDirections, cells, pixelPosition);
			availableDirections.removeAll(bombedDirections);

			if (availableDirections.isEmpty()) {
				// slepa;
				return false;
			}

			if (availableDirections.size() == 1) {
				// korytarz
				direction = availableDirections.get(0);
				pixelDistance = borderFactory.getFramesCountToNextCell(userPos,
						userPointPos, direction);
				userPointPos = MoveUtility.moveUser(userPointPos, pixelDistance
						* PIXEL_STEP, direction);
				userPos = MoveUtility.moveUser(userPos, direction);
				pixelPosition += pixelDistance;
			} else if (availableDirections.size() > 1) {
				// skrzyzowanie

				for (int i = 0; i < availableDirections.size(); i++) {
					Direction dir = availableDirections.get(i);
					return isSafeCell(cells, MoveUtility.moveUser(userPos
							.clone(), dir), userPointPos, dir,
							pixelPosition + 1, id);
				}
				return false;
			}
		}
		return true;
	}

}
