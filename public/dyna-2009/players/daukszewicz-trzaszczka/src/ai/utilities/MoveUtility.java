package ai.utilities;

import java.awt.Point;

import org.jdyna.IPlayerController.Direction;

import ai.board.EditableCell;


/**
 * 
 * simple utility for user moving on board
 * 
 * @author Asia
 * 
 */
public class MoveUtility {

	/**
	 * 
	 * move user by given nr of pixels in given direction
	 * 
	 * @param startPoint
	 * @param pixelDistance
	 * @param direction
	 * @return
	 */
	public static Point moveUser(Point startPoint, int pixelDistance,
			Direction direction) {
		Point copy = (Point) startPoint.clone();

		if (direction == null) {
			return copy;
		}

		if (direction.equals(Direction.LEFT)) {
			copy.x = copy.x - pixelDistance;
		}

		if (direction.equals(Direction.RIGHT)) {
			copy.x = copy.x + pixelDistance;
		}

		if (direction.equals(Direction.UP)) {
			copy.y = copy.y - pixelDistance;
		}

		if (direction.equals(Direction.DOWN)) {
			copy.y = copy.y + pixelDistance;
		}

		return copy;
	}

	/**
	 * 
	 * move to new position by given pixels in given direction
	 * 
	 * @param pointPosition
	 * @param direction
	 * @param cells
	 * @param pixelStep
	 * @return
	 */
	public static int[] moveUser(Point pointPosition, Direction direction,
			EditableCell[][] cells, int pixelStep) {
		int clone[];
		int[] beforeMove = MathUtility.getPosition(pointPosition);
		if (direction.equals(Direction.LEFT)) {
			pointPosition.x = pointPosition.x - pixelStep;
		}
		if (direction.equals(Direction.RIGHT)) {
			pointPosition.x = pointPosition.x + pixelStep;
		}
		if (direction.equals(Direction.UP)) {
			pointPosition.y = pointPosition.y - pixelStep;
		}
		if (direction.equals(Direction.DOWN)) {
			pointPosition.y = pointPosition.y + pixelStep;
		}

		clone = MathUtility.getPosition(pointPosition);

		if ((cells[clone[0]][clone[1]]).type.isWalkable()) {
			return clone;
		} else {
			if (clone[0] == beforeMove[0] && clone[1] == beforeMove[1]) {
				return clone;
			} else {
				return null;
			}
		}
	}

	/**
	 * 
	 * move to new postion by one cell in given direction
	 * 
	 * @param position
	 * @param direction
	 * @return
	 */
	public static int[] moveUser(int[] position, Direction direction) {
		if (direction.equals(Direction.LEFT)) {
			position[0] = position[0] - 1;
		}
		if (direction.equals(Direction.RIGHT)) {
			position[0] = position[0] + 1;
		}
		if (direction.equals(Direction.UP)) {
			position[1] = position[1] - 1;
		}
		if (direction.equals(Direction.DOWN)) {
			position[1] = position[1] + 1;
		}
		return position;
	}
}
