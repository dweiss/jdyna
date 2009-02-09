package ai.utilities;

import java.awt.Point;

import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IPlayerController.Direction;

/**
 * 
 * contains math/logic operation linked with board,points
 * 
 * @author Slawek
 * 
 */
public class MathUtility {
	/**
	 * 
	 * converts Point which represents position to int[]
	 * 
	 * @param point
	 * @return
	 */
	public static int[] getPosition(Point point) {
		int tab[] = new int[2];
		tab[0] = (int) (point.x / Globals.DEFAULT_CELL_SIZE);
		tab[1] = (int) (point.y / Globals.DEFAULT_CELL_SIZE);
		return tab;
	}

	/**
	 * returns vector from one point to other that represents direction to this
	 * other point
	 * 
	 * 
	 * @param tab1
	 * @param tab2
	 * @return
	 */
	public static Direction[] getVectorDirections(int[] tab1, int tab2[]) {
		Direction[] result = new Direction[2];
		int horizontalDiff = tab2[0] - tab1[0];
		int verticalDiff = tab2[1] - tab1[1];

		if (horizontalDiff > 0) {
			result[0] = Direction.RIGHT;
		} else if (horizontalDiff < 0) {
			result[0] = Direction.LEFT;
		} else {
			result[0] = null;
		}

		if (verticalDiff > 0) {
			result[1] = Direction.DOWN;
		} else if (verticalDiff < 0) {
			result[1] = Direction.UP;
		} else {
			result[1] = null;
		}

		return result;
	}

	/**
	 * 
	 * simple manhatan distance in cells
	 * 
	 * @param pos1
	 * @param pos2
	 * @return
	 */
	public static int getManhatanDistance(int[] pos1, int[] pos2) {
		return Math.abs(pos1[0] - pos2[0]) + Math.abs(pos1[1] - pos2[1]);
	}

	/**
	 * 
	 * simple manhatan distance in pixels
	 * 
	 * @param pos1
	 * @param pos2
	 * @return
	 */
	public static int getManhatanDistance(Point point1, Point point2) {
		return Math.abs(point1.x - point2.x) + Math.abs(point1.y - point2.y);
	}

	/**
	 * 
	 * returns oposite direction from a given etc.
	 * 
	 * @param direction
	 * @return
	 */
	public static Direction getOpositeDirection(Direction direction) {
		if (direction.equals(Direction.LEFT)) {
			return Direction.RIGHT;
		} else if (direction.equals(Direction.RIGHT)) {
			return Direction.LEFT;
		} else if (direction.equals(Direction.UP)) {
			return Direction.DOWN;
		} else if (direction.equals(Direction.DOWN)) {
			return Direction.UP;
		}
		return null;
	}

	/**
	 * 
	 * returns distance between two points ( if they are in line)
	 * 
	 * @param pos1
	 * @param pos2
	 * @return
	 */
	public static int getLineDistance(int[] pos1, int[] pos2) {

		if (pos1[0] == pos2[0]) {
			return Math.abs(pos1[1] - pos2[1]);
		} else if (pos1[1] == pos2[1]) {
			return Math.abs(pos1[0] - pos2[0]);
		} else {
			return -1;
		}
	}

	/**
	 * 
	 * gets line direction from previous point to current point
	 * 
	 * @param lastPosition
	 * @param currentPosition
	 * @return
	 */
	public static Direction getDirection(Point lastPosition,
			Point currentPosition) {
		int diffX = lastPosition.x - currentPosition.x;
		int diffY = lastPosition.y - currentPosition.y;

		if (diffX > 0) {
			return Direction.LEFT;
		} else if (diffX < 0) {
			return Direction.RIGHT;
		}
		if (diffY > 0) {
			return Direction.UP;
		} else if (diffX < 0) {
			return Direction.DOWN;
		}

		return null;
	}

}
