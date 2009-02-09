package com.kozmich.dyna.ai;

import java.awt.Point;

import com.dawidweiss.dyna.Globals;

public class PlayerUtils {

	/**
	 * Calculate manhattan distance between two locations.
	 */
	public static int manhattanDistance(Point a, Point b) {
		return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
	}

	/**
	 * Euclidian distance between two points.
	 */
	public static double euclidianDistance(Point a, Point b) {
		final int x = a.x - b.x;
		final int y = a.y - b.y;
		return Math.sqrt(x * x + y * y);
	}

	/**
	 * Convert pixel coordinates to grid cell coordinates.
	 */
	public static Point pixelToGrid(Point location) {
		return new Point(location.x / Globals.DEFAULT_CELL_SIZE, location.y
				/ Globals.DEFAULT_CELL_SIZE);
	}

}
