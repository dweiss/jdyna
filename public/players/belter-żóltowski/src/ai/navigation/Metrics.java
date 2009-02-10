package ai.navigation;

import java.awt.Point;

/**
 * Utility class that stores numerous distance metrics.
 */
public class Metrics {
	/**
	 * Calculate not-rooted euclidean distance 
	 * @param source source point
	 * @param target target point
	 * @return calculated distance
	 */
	public static double euclideanDistance(Point source, Point target) {
		double tmp = Math.pow(target.x - source.x, 2)
				+ Math.pow(target.y - source.y, 2);
		return tmp;
	}
	
	/**
	 * Calculate not-rooted euclidean distance 
	 * @param source source node
	 * @param target target node
	 * @return calculated distance
	 */
	public static double euclideanDistance(Node source, Node target) {
		return euclideanDistance(source.getLocation(), target.getLocation());
	}

	/**
	 * Calculate manhattan distance 
	 * @param source source point
	 * @param target target point
	 * @return calculated distance
	 */
	public static double manhattanDistance(Point source, Point target) {
		return Math.abs(target.x - source.x) + Math.abs(target.y - source.y);
	}

	/**
	 * Calculate manhattan distance 
	 * @param source source node
	 * @param target target node
	 * @return calculated distance
	 */
	public static double manhattanDistance(Node source, Node target) {
		return manhattanDistance(source.getLocation(), target.getLocation());
	}
}
