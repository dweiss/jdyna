package AIplayer.model;

import java.awt.Point;

import org.jdyna.Globals;


/**
 * Class used to calculate distance to adjacent cells on board, and moving point
 * to new position.
 * 
 * @author Lukasz Witkowski
 * 
 */
public final class CellDistance {

	/* Size of cell on board */
	private static int cellSize = Globals.DEFAULT_CELL_SIZE;

	/* Distance player is moving between two frames */
	private static int moveFramePixel = 2;

	/**
	 * Return distance measure in Manhattan metric to upper cell.
	 * 
	 * @param position
	 *            position in pixel on board
	 * @return distance to upper cell
	 */
	public static int distanceUP(Point position) {
		int distance = position.y % cellSize + moveFramePixel;
		if (distance % moveFramePixel != 0)
			distance = distance - distance % moveFramePixel + moveFramePixel;
		if (position.x % cellSize > 8) {
			distance += position.x % (cellSize) - (cellSize / 2);
			if (distance % moveFramePixel != 0)
				distance = distance - distance % moveFramePixel
						+ moveFramePixel;
		} else {
			distance += (cellSize / 2) - position.x % (cellSize);
			if (distance % moveFramePixel != 0)
				distance = distance - distance % moveFramePixel
						+ moveFramePixel;
		}
		return Math.abs(distance / moveFramePixel);
	}

	/**
	 * Return the coordinates of point when he reach upper cell.
	 * 
	 * @param position
	 *            position in pixel on board
	 * @return the coordinates of point when he reach upper cell
	 */
	public static Point movePointUP(final Point position) {
		int distance = position.y % cellSize + moveFramePixel;
		if (distance % moveFramePixel != 0)
			distance = distance - distance % moveFramePixel + moveFramePixel;

		Point result = new Point(position);
		result.y -= distance;

		if (position.x % cellSize > 8) {
			distance = position.x % (cellSize) - (cellSize / 2);
			if (distance % moveFramePixel != 0)
				distance = distance - distance % moveFramePixel
						+ moveFramePixel;
			result.x -= distance;
		} else {
			distance = (cellSize / 2) - position.x % (cellSize);
			if (distance % moveFramePixel != 0)
				distance = distance - distance % moveFramePixel
						+ moveFramePixel;
			result.x += distance;
		}

		return result;
	}

	/**
	 * Return distance measure in Manhattan metric to lower cell.
	 * 
	 * @param position
	 *            position in pixel on board
	 * @return distance to lower cell
	 */
	public static int distanceDOWN(Point position) {
		int distance = (cellSize - position.y % cellSize);
		if (distance % moveFramePixel != 0)
			distance = distance - distance % moveFramePixel + moveFramePixel;

		if (position.x % cellSize > 8) {
			distance += position.x % (cellSize) - (cellSize / 2);
			if (distance % moveFramePixel != 0)
				distance = distance - distance % moveFramePixel
						+ moveFramePixel;
		} else {
			distance += (cellSize / 2) - position.x % (cellSize);
			if (distance % moveFramePixel != 0)
				distance = distance - distance % moveFramePixel
						+ moveFramePixel;
		}
		return Math.abs(distance / moveFramePixel);
	}

	/**
	 * Return distance measure in Manhattan metric to cell on the left.
	 * 
	 * @param position
	 *            position in pixel on board
	 * @return distance to cell on the left
	 */
	public static int distanceLEFT(Point position) {
		int distance = (position.x % cellSize + moveFramePixel);
		if (distance % moveFramePixel != 0)
			distance = distance - distance % moveFramePixel + moveFramePixel;

		if (position.y % cellSize > 8) {
			distance += position.y % (cellSize) - (cellSize / 2);
			if (distance % moveFramePixel != 0)
				distance = distance - distance % moveFramePixel
						+ moveFramePixel;
		} else {
			distance += (cellSize / 2) - position.y % (cellSize);
			if (distance % moveFramePixel != 0)
				distance = distance - distance % moveFramePixel
						+ moveFramePixel;
		}
		return Math.abs(distance / moveFramePixel);
	}

	/**
	 * Return distance measure in Manhattan metric to cell on the right.
	 * 
	 * @param position
	 *            position in pixel on board
	 * @return distance to cell on the right
	 */
	public static int distanceRIGHT(Point position) {
		int distance = (cellSize - position.x % cellSize);
		if (distance % moveFramePixel != 0)
			distance = distance - distance % moveFramePixel + moveFramePixel;

		if (position.y % cellSize > 8) {
			distance += position.y % (cellSize) - (cellSize / 2);
			if (distance % moveFramePixel != 0)
				distance = distance - distance % moveFramePixel
						+ moveFramePixel;
		} else {
			distance += (cellSize / 2) - position.y % (cellSize);
			if (distance % moveFramePixel != 0)
				distance = distance - distance % moveFramePixel
						+ moveFramePixel;
		}
		return Math.abs(distance / moveFramePixel);
	}

	/**
	 * Return the coordinates of point when he reach lower cell.
	 * 
	 * @param position
	 *            position in pixel on board
	 * @return the coordinates of point when he reach lower cell
	 */
	public static Point movePointDOWN(Point position) {

		int distance = (cellSize - position.y % cellSize);
		if (distance % moveFramePixel != 0)
			distance = distance - distance % moveFramePixel + moveFramePixel;

		Point result = new Point(position);
		result.y += distance;

		if (position.x % cellSize > 8) {
			distance = position.x % (cellSize) - (cellSize / 2);
			if (distance % moveFramePixel != 0)
				distance = distance - distance % moveFramePixel
						+ moveFramePixel;
			result.x -= distance;
		} else {
			distance = (cellSize / 2) - position.x % (cellSize);
			if (distance % moveFramePixel != 0)
				distance = distance - distance % moveFramePixel
						+ moveFramePixel;
			result.x += distance;
		}

		return result;
	}

	/**
	 * Return the coordinates of point when he reach cell on the left.
	 * 
	 * @param position
	 *            position in pixel on board
	 * @return the coordinates of point when he reach cell on the left.
	 */
	public static Point movePointLEFT(Point position) {

		int distance = (position.x % cellSize + moveFramePixel);
		if (distance % moveFramePixel != 0)
			distance = distance - distance % moveFramePixel + moveFramePixel;

		Point result = new Point(position);
		result.x -= distance;

		if (position.y % cellSize > 8) {
			distance = position.y % (cellSize) - (cellSize / 2);
			if (distance % moveFramePixel != 0)
				distance = distance - distance % moveFramePixel
						+ moveFramePixel;
			result.y -= distance;
		} else {
			distance = (cellSize / 2) - position.y % (cellSize);
			if (distance % moveFramePixel != 0)
				distance = distance - distance % moveFramePixel
						+ moveFramePixel;
			result.y += distance;
		}
		return result;
	}

	/**
	 * Return the coordinates of point when he reach cell on the right.
	 * 
	 * @param position
	 *            position in pixel on board
	 * @return the coordinates of point when he reach cell on the right.
	 */
	public static Point movePointRIGHT(Point position) {

		int distance = (cellSize - position.x % cellSize);
		if (distance % moveFramePixel != 0)
			distance = distance - distance % moveFramePixel + moveFramePixel;

		Point result = new Point(position);
		result.x += distance;

		if (position.y % cellSize > 8) {
			distance = position.y % (cellSize) - (cellSize / 2);
			if (distance % moveFramePixel != 0)
				distance = distance - distance % moveFramePixel
						+ moveFramePixel;
			result.y -= distance;
		} else {
			distance = (cellSize / 2) - position.y % (cellSize);
			if (distance % moveFramePixel != 0)
				distance = distance - distance % moveFramePixel
						+ moveFramePixel;
			result.y += distance;
		}
		return result;
	}

	public static int distanceSTAY(Point position) {
		return moveFramePixel;
	}
}