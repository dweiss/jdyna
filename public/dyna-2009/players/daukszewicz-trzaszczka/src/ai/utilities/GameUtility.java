package ai.utilities;

import java.util.ArrayList;
import java.util.List;

import org.jdyna.IPlayerController.Direction;

import ai.board.EditableCell;

/**
 * 
 * @author Slawek
 */
public class GameUtility {

	/**
	 * 
	 * returns available distance from current position
	 * 
	 * @param pos
	 * @param cells
	 * @return
	 */
	public static List<Direction> getAvailableDirections(int pos[],
			EditableCell[][] cells) {
		List<Direction> directions = new ArrayList<Direction>();
		EditableCell cell;
		cell = cells[pos[0] - 1][pos[1]];
		if (cell.type.isWalkable()) {
			// LEFT
			directions.add(Direction.LEFT);
		}

		cell = cells[pos[0] + 1][pos[1]];
		if (cell.type.isWalkable()) {
			// RIGHT
			directions.add(Direction.RIGHT);
		}

		cell = cells[pos[0]][pos[1] - 1];
		if (cell.type.isWalkable()) {
			// UP
			directions.add(Direction.UP);
		}

		cell = cells[pos[0]][pos[1] + 1];
		if (cell.type.isWalkable()) {
			// DOWN
			directions.add(Direction.DOWN);
		}

		return directions;
	}

	/**
	 * 
	 * sortuje list of directions
	 * 
	 * @param userPosition
	 * @param oponentPosition
	 * @param availableDirections
	 * @return
	 */
	public List<Direction> sortDirections(int[] userPosition,
			int[] oponentPosition, List<Direction> availableDirections) {
		List<Direction> result = new ArrayList<Direction>();
		Direction[] vector = MathUtility.getVectorDirections(userPosition,
				oponentPosition);
		int diffX = Math.abs(userPosition[0] - oponentPosition[0]);
		int diffY = Math.abs(userPosition[1] - oponentPosition[1]);

		if (diffX > diffY) {
			// get first Dimension
			if (availableDirections.contains(vector[0])) {
				availableDirections.remove(vector[0]);
				result.add(vector[0]);
			}

			if (availableDirections.contains(vector[1])) {
				availableDirections.remove(vector[1]);
				result.add(vector[1]);
			}

		} else if (diffX < diffY) {
			// get second Dimension
			if (availableDirections.contains(vector[1])) {
				availableDirections.remove(vector[1]);
				result.add(vector[1]);
			}

			if (availableDirections.contains(vector[0])) {
				availableDirections.remove(vector[0]);
				result.add(vector[0]);
			}

		} else if (diffX == 0 && diffY == 0) {

		} else if (diffX == diffY) {
			if (availableDirections.contains(vector[0])) {
				availableDirections.remove(vector[0]);
				result.add(vector[0]);
			}

			if (availableDirections.contains(vector[1])) {
				availableDirections.remove(vector[1]);
				result.add(vector[1]);
			}
		} else {
			return result;
		}

		for (Direction dir : availableDirections) {
			result.add(dir);
		}
		return result;
	}

}
