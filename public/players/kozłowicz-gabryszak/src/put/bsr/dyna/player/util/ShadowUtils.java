package put.bsr.dyna.player.util;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import put.bsr.dyna.player.extcell.ExtBombCell;
import put.bsr.dyna.player.extcell.ExtCell;
import put.bsr.dyna.player.extcell.ExtPreExplosionCell;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.BoardUtilities;
import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IPlayerSprite;
import com.dawidweiss.dyna.IPlayerController.Direction;

/**
 * Utility class for Shadow player.
 * 
 * @author marcin
 */
public class ShadowUtils {

	private static int SINGLE_CELL_TRAVEL_TIME = Globals.DEFAULT_CELL_SIZE / 2;
	private static int BOMB_EXPLOSION_THRESHOLD = 5;
	private static int CELL_IN_DANGER_COST = 10;

	/**
	 * Inner class used to find best path to target.
	 * 
	 * @author marcin 
	 */
	@SuppressWarnings("serial")
	private static class Node extends Point {
		// cost from source point
		public int gCost;
		// cost to destination point
		public int hCost;
		// previous point in path
		public Node parent;

		public Node(int x, int y) {
			super(x, y);
		}

		public Node(Point p) {
			super(p);
		}

		public Node(Point p, int gCost, int hCos) {
			this(p.x, p.y, gCost, hCos);
		}

		public Node(int x, int y, int gCost) {
			super(x, y);
			this.gCost = gCost;
		}

		public Node(int x, int y, int gCost, int hCost) {
			super(x, y);
			this.gCost = gCost;
			this.hCost = hCost;
		}

		@Override
		public boolean equals(Object obj) {
			return super.equals(obj);
		}
	}

	/**
	 * Determine given player in the player list.
	 */
	public static IPlayerSprite identifyPlayer(
			List<? extends IPlayerSprite> players, String name) {
		for (IPlayerSprite ps : players) {
			if (name.equals(ps.getName())) {
				return ps;
			}
		}
		throw new RuntimeException("Player not on the list of players: " + name);
	}

	private static boolean isWalkable(Cell[][] cells, Point pointXY) {
		return cells[pointXY.x][pointXY.y].type.isWalkable();
	}

	/**
	 * Checks if you can go to the direction.
	 * 
	 * @param sourcePixels
	 * @param direction
	 * @return
	 */
	public static boolean isWalkable(BoardInfo boardInfo, Cell[][] cells,
			Point sourcePixels, Direction direction) {
		Point sourceGridPos = boardInfo.pixelToGrid(sourcePixels);
		Point targetGridPos = null;

		switch (direction) {
		case LEFT:
			targetGridPos = new Point(sourceGridPos.x - 1, sourceGridPos.y);
			break;
		case RIGHT:
			targetGridPos = new Point(sourceGridPos.x + 1, sourceGridPos.y);
			break;
		case UP:
			targetGridPos = new Point(sourceGridPos.x, sourceGridPos.y - 1);
			break;
		case DOWN:
			targetGridPos = new Point(sourceGridPos.x, sourceGridPos.y + 1);
			break;
		default:
			break;
		}

		if (targetGridPos != null && targetGridPos.x >= 0
				&& targetGridPos.x < boardInfo.gridSize.width
				&& targetGridPos.y >= 0
				&& targetGridPos.y < boardInfo.gridSize.height) {

			return cells[targetGridPos.x][targetGridPos.y].type.isWalkable();
		}

		return false;
	}

	public static boolean isLethal(Cell[][] cells, Point pointXY) {
		boolean result = cells[pointXY.x][pointXY.y].type.isLethal();
		return result;
	}

	/**
	 * Direction from sourceXY to nextXY.
	 * 
	 * @param sourceXY
	 *            source Point
	 * @param nextXY
	 *            target Point
	 * @return Direction direction which should be chosen when going from source
	 *         Point to target, <code>null</code> when sourcePoint and target
	 *         have the same localisation.
	 */
	public static Direction getDirection(Point sourceXY, Point nextXY) {
		int xDiff = nextXY.x - sourceXY.x;
		int yDiff = nextXY.y - sourceXY.y;

		if (xDiff != 0) {
			return xDiff < 0 ? Direction.LEFT : Direction.RIGHT;
		}

		if (yDiff != 0) {
			return yDiff < 0 ? Direction.UP : Direction.DOWN;
		}

		return null;
	}

	static private List<Node> getNeighbourPoints(BoardInfo info, Cell[][] cells,
			Node point) {
		Node[] points = new Node[4];
		points[0] = new Node(point.x - 1, point.y, point.gCost + 1);
		points[1] = new Node(point.x + 1, point.y, point.gCost + 1);
		points[2] = new Node(point.x, point.y + 1, point.gCost + 1);
		points[3] = new Node(point.x, point.y - 1, point.gCost + 1);
		List<Node> result = new ArrayList<Node>(4);
		for (final Node p : points) {
			if (info.isOnBoard(p) && !isLethal(cells, p)
					&& isWalkable(cells, p)) {
				result.add(p);
			}
		}
		return result;
	}

	private static List<Node> getNeighbourPoints(BoardInfo info,
			Cell[][] cells, Node point, Set<Node> visitedNodes) {
		Node[] points = new Node[4];
		points[0] = new Node(point.x - 1, point.y, point.gCost + 1);
		points[1] = new Node(point.x + 1, point.y, point.gCost + 1);
		points[2] = new Node(point.x, point.y + 1, point.gCost + 1);
		points[3] = new Node(point.x, point.y - 1, point.gCost + 1);
		List<Node> result = new ArrayList<Node>(4);
		for (final Node p : points) {
			if (info.isOnBoard(p) && !isLethal(cells, p)
					&& isWalkable(cells, p) && !visitedNodes.contains(p)) {
				result.add(p);
			}
		}
		return result;
	}

	static private boolean writen = false;

	static private int getHeuristic(ExtCell[][] cells, Point from, Point target) {
		int cost = 0;
		int distance = BoardUtilities.manhattanDistance(from, target);
		if (cells[from.x][from.y] instanceof ExtPreExplosionCell) {
			ExtPreExplosionCell cell = (ExtPreExplosionCell) cells[from.x][from.y];
			if (Math.abs(cell.fuseCounter
					- (distance * SINGLE_CELL_TRAVEL_TIME)) < BOMB_EXPLOSION_THRESHOLD) {
				cost = CELL_IN_DANGER_COST;
			}
			// if (isInDanger(from, target, cells)) {
			// cost = 10;
			// }
			//			
			// if (!writen) {
			// DebugUtils.printBoard(cells);
			// writen = true;
			// }
		}
		return distance + cost;
	}

	static private int getHeuristicCost(Cell[][] cells, Point sourceGridPoint,
			Point stopGridPoint) {
		return BoardUtilities.manhattanDistance(sourceGridPoint, stopGridPoint);
	}

	static public List<Point> getShortest(BoardInfo info, ExtCell[][] cells,
			Point startGridPoint, Point targetGridPoint) {
		Node tmpPoint = new Node(startGridPoint, 0, getHeuristicCost(cells,
				startGridPoint, targetGridPoint));
		int cost;

		// list of node to explore
		Set<Node> openList = new LinkedHashSet<Node>();
		openList.add(tmpPoint);
		// list of node fully explored - they won't be checked again
		Set<Node> closedList = new LinkedHashSet<Node>();
		writen = false;
		while (openList.size() > 0) {
			cost = Integer.MAX_VALUE;
			// from list of nodes to explore find one
			// which sum of distance from source and target is lowest
			for (Node node : openList) {
				if (node.gCost + node.hCost < cost) {
					cost = node.gCost + node.hCost;
					tmpPoint = node;
				}
			}
			// checking node is our destination - path is found
			if (tmpPoint.equals(targetGridPoint)) {
				break;
			}
			// mark current node as 'fully' explored
			openList.remove(tmpPoint);
			closedList.add(tmpPoint);
			boolean isBetter = false;
			// find every 'movable' neighbour
			for (Node node : getNeighbourPoints(info, cells, tmpPoint)) {
				if (closedList.contains(node)) {
					// node already fully explored
					continue;
				}

				isBetter = false;
				if (!openList.contains(node)) {
					// first check - add to open list to explore it's neighbour
					// later
					openList.add(node);
					node.hCost = getHeuristic(cells, node, targetGridPoint);
					isBetter = true;
				} else if (node.gCost > tmpPoint.gCost + 1) {
					// has been already explored from other path - if this path
					// is
					// shorter chose it
					isBetter = true;
				}

				if (isBetter) {
					node.parent = tmpPoint;
					node.gCost = tmpPoint.gCost + 1;
				}
			}
		}
		// rewrite result to List of Points
		LinkedList<Point> result = new LinkedList<Point>();
		while (tmpPoint.parent != null) {
			result.addFirst(tmpPoint);
			tmpPoint = tmpPoint.parent;
		}
		if (writen) {
			for (Point p : result) {
				System.out.println("[" + p.x + "," + p.y + "]");

			}
		}

		return result;
	}

	public static Point findClosestSafePoint(BoardInfo info,
			ExtCell[][] extCell, Point gridPosition, boolean findSemiSafe) {
		LinkedHashSet<Node> nodes = new LinkedHashSet<Node>();
		nodes.add(new Node(gridPosition));
		nodes.addAll(new LinkedHashSet<Node>(getNeighbourPoints(info, extCell,
				new Node(gridPosition))));
		Set<Node> visitedNodes = new LinkedHashSet<Node>();
		while (nodes.size() > 0) {
			for (Node n : nodes) {
				if (isSafe(info, extCell, n, gridPosition, findSemiSafe)) {
					return n;
				}
				nodes
						.addAll(getNeighbourPoints(info, extCell, n,
								visitedNodes));
				nodes.remove(n);
				visitedNodes.add(n);
				break;
			}
		}

		// not found
		if (!findSemiSafe) {
			return findClosestSafePoint(info, extCell, gridPosition, true);
		}

		return null;
	}

	private static boolean isSafe(BoardInfo info, ExtCell[][] cells, Node n,
			Point gridPosition, boolean findSemiSafe) {
		ExtCell cell = (ExtCell) cells[n.x][n.y];
		if (cell instanceof ExtPreExplosionCell) {
			if (findSemiSafe
					&& ((ExtPreExplosionCell) cell).fuseCounter > 2 * BoardUtilities
							.manhattanDistance(info.gridToPixel(gridPosition),
									info.gridToPixel(n))) {
				return true;
			}
			return false;
		}
		return isWalkable(cells, n) && !isLethal(cells, n);
	}

	/**
	 * Checks if pixelPosition is the upper-left pixel of grid (object is
	 * exactly in a grid cell).
	 * 
	 * @param pixelPosition
	 * @param boardInfo
	 * @return
	 */
	public static boolean isAligned(Point pixelPosition, BoardInfo boardInfo) {
		final int cellSize = boardInfo.cellSize;
		final int pixelThreshold = 0;
		final Point pixelToGridOffset = boardInfo
				.pixelToGridOffset(pixelPosition);
		return Math.abs(pixelToGridOffset.x - cellSize / 2) <= pixelThreshold
				&& Math.abs(pixelToGridOffset.y - cellSize / 2) <= pixelThreshold;
	}

	private static int evaluate(BoardInfo info, ExtCell[][] cells, Node point,
			Node start) {
		int distance = BoardUtilities.manhattanDistance(point, start);
		if (cells[point.x][point.y] instanceof ExtPreExplosionCell) {
			final int val = 100 - (Globals.DEFAULT_FUSE_FRAMES - distance * 25);
			return val > 0 ? val : distance;
		} else if (cells[point.x][point.y].type == CellType.CELL_BOMB) {
			System.out.println("It can't be here!");
		}
		return distance;
	}

	public static boolean getWalkableNeigtbours(BoardInfo info,
			ExtCell[][] cells, LinkedList<Node> path, Set<Node> visited,
			Node point, Node start) {
		Node[] points = new Node[4];
		points[0] = new Node(point.x - 1, point.y, point.gCost + 1);
		points[1] = new Node(point.x + 1, point.y, point.gCost + 1);
		points[2] = new Node(point.x, point.y + 1, point.gCost + 1);
		points[3] = new Node(point.x, point.y - 1, point.gCost + 1);

		boolean bResult = false;
		for (final Node p : points) {
			if (info.isOnBoard(p) && isWalkable(cells, p)
					&& !visited.contains(p)) {
				p.hCost = evaluate(info, cells, point, start);
				path.add(p);
				visited.add(p);
				if (cells[p.x][p.y].type == CellType.CELL_EMPTY) {
					bResult = true;
				}
			}
		}
		return bResult;
	}

	/**
	 * Checks if following the path will end up with death.
	 * 
	 * @param movePath
	 *            path to move on
	 * @param gridPosition
	 * @param extCell
	 * 
	 * @return true - path is dangerous, false - in other case
	 */
	public static boolean isInDanger(List<Point> movePath, Point gridPosition,
			ExtCell[][] extCells) {
		for (Point p : movePath) {
			if (isInDanger(gridPosition, p, extCells)) {
				return true;
			}
		}
		ExtCell cell = extCells[gridPosition.x][gridPosition.y];
		if (cell instanceof ExtBombCell) {
			return true;
		}

		return false;
	}

	public static boolean isInDanger(Point gridPointStart, Point gridPointEnd,
			ExtCell[][] extCells) {

		final int speed = 2;

		int pixelDistance = BoardUtilities.manhattanDistance(gridPointEnd,
				gridPointStart)
				* Globals.DEFAULT_CELL_SIZE;
		int travelFrames = pixelDistance / speed;

		ExtCell cell = extCells[gridPointEnd.x][gridPointEnd.y];
		if (cell instanceof ExtPreExplosionCell) {
			int fuseCounter = ((ExtPreExplosionCell) cell).fuseCounter;
			if (travelFrames >= fuseCounter - 5
					&& travelFrames <= fuseCounter + 10) {
				return true;
			}
		}

		return false;
	}

	public static IPlayerSprite getClosestPlayer(
			List<? extends IPlayerSprite> players, Point pixelPosition,
			String myName, BoardInfo boardInfo) {

		double minLength = Double.MAX_VALUE;// in grid
		IPlayerSprite closestPlayer = null;
		for (IPlayerSprite player : players) {
			if (player.getName().equals(myName)) {
				continue;
			}
			double len = BoardUtilities.euclidianDistance(player.getPosition(),
					pixelPosition);
			if (len < minLength) {
				minLength = len;
				closestPlayer = player;
			}
		}
		// gridTarget = boardInfo.pixelToGrid(closestPlayer.getPosition());
		return closestPlayer;
	}

	public static boolean inSraightPath(Point a, Point b, int fuzziness) {
		return (Math.abs(a.x - b.x) <= fuzziness && a.y == b.y)
				|| (Math.abs(a.y - b.y) <= fuzziness && a.x == b.x);
	}

	// public static Point evacuation(BoardInfo info, ExtCell[][] cells,
	// Point startGridPoint) {
	//
	// boolean not_safe = false;
	// LinkedList<Node> path = new LinkedList<Node>();
	// Set<Node> alreadyVisited = new LinkedHashSet<Node>();
	// Node startPoint = new Node(startGridPoint);
	// Node currentPoint = new Node(startGridPoint);
	// int size = -1;
	// do {
	// size = path.size();
	// not_safe = getWalkableNeigtbours(info, cells, path, alreadyVisited,
	// currentPoint, startPoint);
	// } while (not_safe && size != path.size());
	//
	// return null;
	// }

	// private static List<Node> getWallkableNeighbours(BoardInfo info,
	// Cell[][] cells, Node point, Set<Node> visitedNodes) {
	// Node[] points = new Node[4];
	// points[0] = new Node(point.x - 1, point.y, point.gCost + 1);
	// points[1] = new Node(point.x + 1, point.y, point.gCost + 1);
	// points[2] = new Node(point.x, point.y + 1, point.gCost + 1);
	// points[3] = new Node(point.x, point.y - 1, point.gCost + 1);
	// List<Node> result = new ArrayList<Node>(4);
	// for (final Node p : points) {
	// if (info.isOnBoard(p) && isWalkable(cells, p)) {
	// result.add(p);
	// }
	// }
	// return result;
	// }

	// @Deprecated
	// static public List<Point> getShortestPath(BoardInfo info, Cell[][] cells,
	// Point startGridPoint, Point targetGridPoint) {
	// Node tmpPoint = new Node(startGridPoint, 0, getHeuristicCost(cells,
	// startGridPoint, targetGridPoint));
	// int cost;
	// // list of node to explore
	// Set<Node> openList = new LinkedHashSet<Node>();
	// openList.add(tmpPoint);
	// // list of node fully explored - they won't be checked again
	// Set<Node> closedList = new LinkedHashSet<Node>();
	//
	// while (openList.size() > 0) {
	// cost = Integer.MAX_VALUE;
	// // from list of nodes to explore find one
	// // which sum of distance from source and target is lowest
	// for (Node node : openList) {
	// if (node.gCost + node.hCost < cost) {
	// cost = node.gCost + node.hCost;
	// tmpPoint = node;
	// }
	// }
	// // checking node is our destination - path is found
	// if (tmpPoint.equals(targetGridPoint)) {
	// break;
	// }
	// // mark current node as 'fully' explored
	// openList.remove(tmpPoint);
	// closedList.add(tmpPoint);
	// boolean isBetter = false;
	// // find every 'movable' neighbour
	// for (Node node : getNeighbourPoints(info, cells, tmpPoint)) {
	// if (closedList.contains(node)) {
	// // node already fully explored
	// continue;
	// }
	// isBetter = false;
	// if (!openList.contains(node)) {
	// // first check - add to open list to explore it's neighbour
	// // later
	// openList.add(node);
	// node.hCost = getHeuristicCost(cells, node, targetGridPoint);
	// isBetter = true;
	// } else if (node.gCost > tmpPoint.gCost + 1) {
	// // has been already explored from other path - if this path
	// // is
	// // shorter chose it
	// isBetter = true;
	// }
	//
	// if (isBetter) {
	// node.parent = tmpPoint;
	// node.gCost = tmpPoint.gCost + 1;
	// }
	// }
	// }
	// // rewrite result to List of Points
	// LinkedList<Point> result = new LinkedList<Point>();
	// while (tmpPoint.parent != null) {
	// result.addFirst(tmpPoint);
	// tmpPoint = tmpPoint.parent;
	// }
	//
	// return result;
	// }

	// /**
	// * Points are in grid.
	// *
	// * @param startXY
	// * @param targetXY
	// * @return
	// */
	// public static List<Point> getPointsXYTowardsTarget(Point startXY,
	// Point targetXY) {
	//
	// LinkedList<Point> points = new LinkedList<Point>();
	// if (startXY.x < targetXY.x) {
	// points.addFirst(new Point(startXY.x + 1, startXY.y));
	// } else {
	// points.addLast(new Point(startXY.x + 1, startXY.y));
	// }
	//
	// if (startXY.x > targetXY.x) {
	// points.addFirst(new Point(startXY.x - 1, startXY.y));
	// } else {
	// points.addLast(new Point(startXY.x - 1, startXY.y));
	// }
	//
	// if (startXY.y < targetXY.y) {
	// points.addFirst(new Point(startXY.x, startXY.y + 1));
	// } else {
	// points.addLast(new Point(startXY.x, startXY.y + 1));
	// }
	//
	// if (startXY.y > targetXY.y) {
	// points.addFirst(new Point(startXY.x, startXY.y - 1));
	// } else {
	// points.addLast(new Point(startXY.x, startXY.y - 1));
	// }
	// return points;
	// }
}
