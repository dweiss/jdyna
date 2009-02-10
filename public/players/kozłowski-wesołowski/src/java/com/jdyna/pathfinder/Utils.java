package com.jdyna.pathfinder;

import java.util.List;

import com.dawidweiss.dyna.GameResult;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.Player;
import com.dawidweiss.dyna.PlayerStatus;
import com.dawidweiss.dyna.IPlayerController.Direction;
import com.jdyna.emulator.gamestate.GridCoord;
import com.jdyna.emulator.gamestate.PointCoord;

/** 
 * Class containing some useful static utilities to use in various situations. 
 * 
 * @author Bartosz Wesołowski
 */
public final class Utils {

	/**
	 * @return The direction you have to take to move from point "from" to point "to". This method assumes that the
	 *         points have at least one common coordinate.
	 */
	public static Direction getDirection(final PointCoord from, final PointCoord to) {
		if (from.x == to.x) {
			return to.y > from.y ? Direction.DOWN : Direction.UP;
		} else {
			return to.x > from.x ? Direction.RIGHT : Direction.LEFT;
		}
	}

	/**
	 * @return The direction you have to take to move from point "from" to point "to". This method assumes that the
	 *         cells are neighboring.
	 */
	public static Direction getDirection(final GridCoord from, final GridCoord to) {
		if (from.x == to.x) {
			return to.y > from.y ? Direction.DOWN : Direction.UP;
		} else {
			return to.x > from.x ? Direction.RIGHT : Direction.LEFT;
		}
	}

	/** Returns the opposite direction. */
	public static Direction getOpposite(final Direction direction) {
		if (direction == Direction.UP) {
			return Direction.DOWN;
		}
		if (direction == Direction.RIGHT) {
			return Direction.LEFT;
		}
		if (direction == Direction.DOWN) {
			return Direction.UP;
		}
		if (direction == Direction.LEFT) {
			return Direction.RIGHT;
		}
		return null;
	}

	/** Checks is d2 equals d1 after turning left or right. */
	public static boolean isTurning(final Direction d1, final Direction d2) {
		if (d1 == null || d2 == null) {
			return false;
		}
		if (!getOpposite(d1).equals(d2) && !d1.equals(d2)) {
			return true;
		}
		return false;
	}

	/** @return Point at the left side of the cell. */
	public static PointCoord getLeftPoint(final GridCoord cell) {
		final PointCoord center = Utils.gridToPixel(cell);
		final int x = center.x - Globals.DEFAULT_CELL_SIZE / 2;
		final int y = center.y;
		return new PointCoord(x, y);
	}

	/** @return Point at the right side of the cell. */
	public static PointCoord getRightPoint(final GridCoord cell) {
		final PointCoord center = Utils.gridToPixel(cell);
		final int x = center.x + Globals.DEFAULT_CELL_SIZE / 2 - 1;
		final int y = center.y;
		return new PointCoord(x, y);
	}

	/** @return Point at the top side of the cell. */
	public static PointCoord getUpPoint(final GridCoord cell) {
		final PointCoord center = Utils.gridToPixel(cell);
		final int x = center.x;
		final int y = center.y - Globals.DEFAULT_CELL_SIZE / 2;
		return new PointCoord(x, y);
	}

	/** @return Point at the bottom side of the cell. */
	public static PointCoord getDownPoint(final GridCoord cell) {
		final PointCoord center = Utils.gridToPixel(cell);
		final int x = center.x;
		final int y = center.y + Globals.DEFAULT_CELL_SIZE / 2 - 1;
		return new PointCoord(x, y);
	}

	/** Convert pixel coordinates to grid cell coordinates. Copied from com.jdyna.BoardInfo class and adapted. */
	public static GridCoord pixelToGrid(PointCoord location) {
		return new GridCoord(location.x / Globals.DEFAULT_CELL_SIZE, location.y / Globals.DEFAULT_CELL_SIZE);
	}

	/**
	 * Convert from grid coordinates to pixel data. The result is the centerpoint of the grid's cell. Copied from
	 * com.jdyna.BoardInfo class and adapted.
	 */
	public static PointCoord gridToPixel(GridCoord location) {
		return new PointCoord(location.x * Globals.DEFAULT_CELL_SIZE + Globals.DEFAULT_CELL_SIZE / 2, location.y
				* Globals.DEFAULT_CELL_SIZE + Globals.DEFAULT_CELL_SIZE / 2);
	}

	/** Convert pixel coordinates to grid cell coordinates. Copied from com.jdyna.BoardInfo class and adapted. */
	public static PointCoord pixelToGridOffset(PointCoord location) {
		return new PointCoord(location.x % Globals.DEFAULT_CELL_SIZE, location.y % Globals.DEFAULT_CELL_SIZE);
	}

	/** @return Whether the point is inside the cell or not. */
	public static boolean isPointInsideCell(final PointCoord point, final GridCoord cell) {
		return Utils.pixelToGrid(point).equals(cell);
	}

	/** Estimates the cost of movement between a point and a cell. */
	public static int estimateCost(final PointCoord from, final GridCoord to) {
		final PointCoord toPoint = Utils.gridToPixel(to);
		final int dx = Math.abs(from.x - toPoint.x) / 2 - Globals.DEFAULT_CELL_SIZE / 4;
		final int dy = Math.abs(from.y - toPoint.y) / 2 - Globals.DEFAULT_CELL_SIZE / 4;
		return Math.max(dx, dy);
	}

	/** @return <code>True</code> if the cells are neighboring and <code>false</code> otherwise. */
	public static boolean isNeighbor(final GridCoord cell1, final GridCoord cell2) {
		if (cell1.x == cell2.x) {
			final int dy = Math.abs(cell1.y - cell2.y);
			return dy == 1;
		} else if (cell1.y == cell2.y) {
			final int dx = Math.abs(cell1.x - cell2.x);
			return dx == 1;
		}
		return false;
	}

	/** @return <code>True</code> if the given player has won the game and <code>false</code> otherwise. */
	public static boolean playerWon(final GameResult gr, final Player pl) {
		final List<PlayerStatus> stats = gr.stats;
		final int p1Idx = stats.get(0).getPlayerName().equals(pl.name) ? 0 : 1;
		for (int i = 0; i < stats.size(); i++) {
			if (i != p1Idx) {
				if (stats.get(p1Idx).getLivesLeft() <= stats.get(i).getLivesLeft()) {
					return false;
				}
			}
		}
		return true;
	}

}
