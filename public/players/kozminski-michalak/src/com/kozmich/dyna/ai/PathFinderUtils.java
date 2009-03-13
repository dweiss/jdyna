package com.kozmich.dyna.ai;

import java.awt.Point;
import java.util.ArrayList;

import org.jdyna.Cell;


/**
 * Helper for {@link PathFinder}.
 * 
 * @author Lukasz Kozminski
 * @author Tomasz Michalak
 * 
 */
public class PathFinderUtils {

	/**
	 * Punishment on the distance between tops. Used when the player is standing
	 * on the bomb.
	 */
	// private static final int BOMB_PUNISHMENT = 1000;
	public static Node[] convertToGraphNodes(Cell[][] cells) {
		int cellsNumber = 0;
		for (int i = 0; i < cells.length; i++) {
			cellsNumber += cells[i].length;
		}
		Node[] nodes = new Node[cellsNumber];
		int k = 0;
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				nodes[k] = new Node(i, j, k++);
			}
		}
		return nodes;
	}

	public static Edge[] convertToGraphEdges(Cell[][] cells, Node[] nodes, Node player) {
		ArrayList<Edge> edges = new ArrayList<Edge>();
		for (int i = 0; i < nodes.length; i++) {
			for (int j = 0; j < nodes.length; j++) {
				int distance = getDistance(cells, nodes[i], nodes[j], player);
				if (distance != Integer.MAX_VALUE) {
					Edge edge = new Edge(nodes[i], nodes[j], distance);
					edges.add(edge);
				}
			}
		}
		return edges.toArray(new Edge[edges.size()]);
	}

	private static int getDistance(Cell[][] cells, Node n1, Node n2, Node player) {
		if (!canWalkOn(cells, new Point(n1.getRow(), n1.getColumn()))
				|| !canWalkOn(cells, new Point(n2.getRow(), n2.getColumn()))) {
			// the player is standing on the bomb and the n1 or n2 edge isn't a
			// wall
			// if (player != null
			// && ((n1.name == player.name && cells[n2.row][n2.column].type !=
			// CellType.CELL_WALL) || (n2.name == player.name &&
			// cells[n1.row][n1.column].type != CellType.CELL_WALL))) {
			// return BOMB_PUNISHMENT;
			// } else {
			return Integer.MAX_VALUE;
			// }
		}
		if (n1.getColumn() == n2.getColumn() && n1.getRow() == n2.getRow()) {
			return 0;
		}
		if (n1.getColumn() == n2.getColumn() && (n1.getRow() == n2.getRow() - 1 || n1.getRow() == n2.getRow() + 1)) {
			return 1;
		}
		if (n1.getRow() == n2.getRow()
				&& (n1.getColumn() == n2.getColumn() - 1 || n1.getColumn() == n2.getColumn() + 1)) {
			return 1;
		}

		return Integer.MAX_VALUE;
	}

	private static boolean canWalkOn(Cell[][] cells, Point point) {
		return cells[point.x][point.y].type.isWalkable();
	}

}
