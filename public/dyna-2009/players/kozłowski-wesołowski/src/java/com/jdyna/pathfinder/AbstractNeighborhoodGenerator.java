package com.jdyna.pathfinder;

import java.util.LinkedList;
import java.util.List;

import org.jdyna.IPlayerController.Direction;

import com.google.common.collect.Lists;
import com.jdyna.emulator.gamestate.Board;
import com.jdyna.emulator.gamestate.GameState;
import com.jdyna.emulator.gamestate.GridCoord;
import com.jdyna.emulator.gamestate.PointCoord;
import com.jdyna.pathfinder.Node.MoveType;

/**
 * This class is used in path finding. It finds all nodes you can move to from the given node. All subclasses find
 * neighbors in a slightly different way. Some of them place artificial bombs to help simulate opponents behavior.
 * 
 * @see StandardNeighborhoodGenerator
 * @see OpponentsBombsNeighborhoodGenerator
 * @see MyAndOpponentsBombsNeighborhoodGenerator
 * 
 * @author Bartosz Wesołowski
 */
abstract class AbstractNeighborhoodGenerator implements INeighborhoodGenerator {

	public List<Node> getNeighbors(final GameState gs, final Node node, final GridCoord destination) {
		final LinkedList<Node> result = Lists.newLinkedList();
		final Direction oppositeDirection = Utils.getOpposite(node.getDirection());

		// add valid neighbors to the result list
		// neighbors that lead to death are forbidden
		// neighbors that change direction to opposite without waiting are forbidden
		for (Neighbor neighbor : getNeighbors(gs, node)) {
			int cost = node.getCostFromStart();
			// there is a different cost of turning and going straight to enable preference between them
			if (Utils.isTurning(node.getDirection(), neighbor.direction)) {
				cost += COST_TURN;
			} else {
				cost += COST_STRAIGHT;
			}
			int neighborCost;
			if (neighbor.exitsCell) {
				// get safe moments in which bot can enter the new cell
				final List<Integer> safeFrames = safeFrames(gs, neighbor, node.getElapsedFrames());
				int lastSafeFrame = node.getElapsedFrames();
				for (Integer safeFrame : safeFrames) { // for each safe moment
					// waiting period has to be spent in a safe cell
					if (!isPeriodSafe(gs, node, lastSafeFrame + 1, safeFrame + MARGIN)) {
						break;
					}
					neighborCost = cost + (COST_FRAME + COST_STAY) * (safeFrame - node.getElapsedFrames()) - COST_STAY;
					final Node newNode = new Node(neighbor, node, neighborCost, COST_FRAME
							* Utils.estimateCost(neighbor, destination), safeFrame);
					result.add(newNode);
					lastSafeFrame = safeFrame;
				}
			} else { // move inside the cell
				// changing direction to opposite is forbiden
				if (neighbor.direction.equals(oppositeDirection)) {
					continue;
				}
				// period bot is staying in the cell has to be safe
				if (!isPeriodSafe(gs, neighbor, node.getElapsedFrames() + 1, node.getElapsedFrames() + neighbor.frames
						+ 1 + MARGIN)) {
					continue;
				}
				neighborCost = cost + neighbor.frames * COST_FRAME;
				final Node newNode = new Node(neighbor, node, neighborCost, COST_FRAME
						* Utils.estimateCost(neighbor, destination), node.getElapsedFrames() + neighbor.frames);
				result.add(newNode);
			}

		}

		return result;
	}

	/**
	 * Returns all neighbors of the given node. It doesn't take into account explosions that can kill the bot as he gets
	 * there.
	 */
	protected List<Neighbor> getNeighbors(final GameState state, final Node node) {
		final List<Neighbor> neighbors = Lists.newLinkedList();
		final PointCoord point = node;
		final GridCoord cell = Utils.pixelToGrid(point);
		final Board board = state.getBoard();

		// LEFT
		final GridCoord leftCell = board.nextCell(cell, Direction.LEFT);
		if (leftCell != null && canWalkOn(state, leftCell, node.getElapsedFrames() + 1)) {
			final PointCoord left = Utils.getLeftPoint(cell);
			final int dx = Math.abs(left.x - point.x);
			final int frames = Math.abs(dx / 2);
			if (!left.equals(point)) { // move the inside cell
				if (!(node.getMoveType() == MoveType.IN_CELL)) {
					neighbors.add(new Neighbor(left, Direction.LEFT, frames, false));
				}
			} else { // move to the next cell
				final PointCoord left2 = new PointCoord(left);
				left2.translate(-1, 0);
				neighbors.add(new Neighbor(left2, Direction.LEFT, frames + 1, true));
			}
		}

		// RIGHT
		final GridCoord rightCell = board.nextCell(cell, Direction.RIGHT);
		if (rightCell != null && canWalkOn(state, rightCell, node.getElapsedFrames() + 1)) {
			final PointCoord right = Utils.getRightPoint(cell);
			final int dx = Math.abs(right.x - point.x);
			final int frames = Math.abs(dx / 2);
			if (!right.equals(point)) { // move inside the cell
				if (!(node.getMoveType() == MoveType.IN_CELL)) {
					neighbors.add(new Neighbor(right, Direction.RIGHT, frames, false));
				}
			} else { // move to the next cell
				final PointCoord right2 = new PointCoord(right);
				right2.translate(1, 0);
				neighbors.add(new Neighbor(right2, Direction.RIGHT, frames + 1, true));
			}
		}

		// UP
		final GridCoord upCell = board.nextCell(cell, Direction.UP);
		if (upCell != null && canWalkOn(state, upCell, node.getElapsedFrames() + 1)) {
			final PointCoord up = Utils.getUpPoint(cell);
			final int dy = Math.abs(up.y - point.y);
			final int frames = Math.abs(dy / 2);
			if (!up.equals(point)) { // move inside the cell
				if (!(node.getMoveType() == MoveType.IN_CELL)) {
					neighbors.add(new Neighbor(up, Direction.UP, frames, false));
				}
			} else { // move to the next cell
				final PointCoord up2 = new PointCoord(up);
				up2.translate(0, -1);
				neighbors.add(new Neighbor(up2, Direction.UP, frames + 1, true));
			}
		}

		// DOWN
		final GridCoord downCell = board.nextCell(cell, Direction.DOWN);
		if (downCell != null && canWalkOn(state, downCell, node.getElapsedFrames() + 1)) {
			final PointCoord down = Utils.getDownPoint(cell);
			final int dy = Math.abs(down.y - point.y);
			final int frames = Math.abs(dy / 2);
			if (!down.equals(point)) { // move inside the cell
				if (!(node.getMoveType() == MoveType.IN_CELL)) {
					neighbors.add(new Neighbor(down, Direction.DOWN, frames, false));
				}
			} else { // move to the next cell
				final PointCoord down2 = new PointCoord(down);
				down2.translate(0, 1);
				neighbors.add(new Neighbor(down2, Direction.DOWN, frames + 1, true));
			}
		}

		return neighbors;
	}

	protected boolean canWalkOn(final GameState gs, final GridCoord cell, final int frameShift) {
		return gs.canWalkOnExcludeOpponents(cell, frameShift);
	}

	protected abstract boolean isPeriodSafe(GameState gs, PointCoord point, int from, int to);

	protected abstract List<Integer> safeFrames(GameState gs, PointCoord point, int frameShift);

}
