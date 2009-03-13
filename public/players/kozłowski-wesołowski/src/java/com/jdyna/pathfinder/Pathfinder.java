package com.jdyna.pathfinder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.jdyna.IPlayerController.Direction;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jdyna.emulator.gamestate.GameState;
import com.jdyna.emulator.gamestate.GridCoord;
import com.jdyna.emulator.gamestate.PointCoord;

/**
 * Very versatile path finder implementing A* algorithm.
 * 
 * @author Bartosz Wesołowski
 */
public final class Pathfinder {
	// neighborhood generators- some of them taking into account artificial bombs
	private final INeighborhoodGenerator standardNeighborhood = new StandardNeighborhoodGenerator();
	private final INeighborhoodGenerator opponentsNeighborhood = new OpponentsBombsNeighborhoodGenerator();
	private final INeighborhoodGenerator myAndOpponentsNeihgborhood = new MyAndOpponentsBombsNeighborhoodGenerator();
	
	// safety checkers- some of them taking into assount artificial bombs
	private final ISafetyChecker safetyCheckerStandard = new SafetyCheckerStandard();
	private final ISafetyChecker safetyCheckerWithOppBombs = new SafetyCheckerWithOppBombs();
	private final ISafetyChecker safetyCheckerWithMyAndOppBombs = new SafetyCheckerWithMyAndOppBombs();

	/** Finds a shelter regarding opponents as bombs. */
	public List<Direction> findShelter(final GameState gs, final PointCoord start) {
		return findShelter(gs, start, 0, opponentsNeighborhood, safetyCheckerWithOppBombs);
	}

	/** 
	 * <pre>
	 * Finds a shelter regarding opponents as bombs. 
	 * Frame to start the path in is passed as one of the params.
	 * </pre>
	 */
	public List<Direction> findShelter(final GameState gs, final PointCoord start, final int startFrame) {
		return findShelter(gs, start, startFrame, opponentsNeighborhood, safetyCheckerWithOppBombs);
	}

	/** Finds a shelter when walking through opponents is allowed. */
	public List<Direction> findShelterDesperately(final GameState gs, final PointCoord start) {
		return findShelter(gs, start, 0, standardNeighborhood, safetyCheckerStandard);
	}

	/** Finds a shelter after a simulated bomb drop. */
	public List<Direction> findShelterWithMyBomb(final GameState gs, final PointCoord start) {
		return findShelter(gs, start, 1, myAndOpponentsNeihgborhood, safetyCheckerWithMyAndOppBombs);
	}

	/** Finds a path between a point and a cell. */
	public List<Direction> findTrip(final GameState gs, final PointCoord from, final GridCoord to) {
		return findTrip(gs, from, to, Integer.MAX_VALUE);
	}

	/** 
	 * <pre>
	 * Finds a path between a point and a cell.
	 * Returns <code>null</code> if a path no longer than <code>maxFrames</code> can be found.
	 * This method was created for optimization purposes.
	 * </pre>
	 */
	public List<Direction> findTrip(final GameState gs, final PointCoord from, final GridCoord to, final int maxFrames) {
		final RetrievablePriorityQueue<Node> open = new RetrievablePriorityQueue<Node>();
		final Map<PointWithStopList, Integer> closed = new HashMap<PointWithStopList, Integer>();

		final Node startNode = new Node(from, 0, INeighborhoodGenerator.COST_FRAME
				* Utils.estimateCost(from, to));
		open.add(startNode);

		while (!open.isEmpty()) {
			final Node current = open.poll();

			// if the trip was found
			if (Utils.isPointInsideCell(current, to)
					&& findShelter(gs, current, current.getElapsedFrames() + 1) != null) {
				return createPath(current);
			}

			// for each neighbor repeat
			for (Node neighbor : opponentsNeighborhood.getNeighbors(gs, current, to)) {
				// don't accept points that are too far
				if (neighbor.getElapsedFrames() > maxFrames) {
					continue;
				}

				// remove neighbor from closed if it is present there and is worse
				if (closed.containsKey(neighbor)) {
					if (closed.get(neighbor) > neighbor.getCost()) {
						closed.remove(neighbor);
					} else {
						continue;
					}
				}

				// modify neighbor in open if it is present there and is worse
				if (open.contains(neighbor)) {
					final Node oldNode = open.get(neighbor);
					if (oldNode.getCost() > neighbor.getCost()) {
						oldNode.update(neighbor);
					}
				} else {
					open.add(neighbor);
				}
			}

			closed.put(current, current.getCost());
		}

		return null;
	}

	/** Recreates a path starting from <code>node</code> and walking through all his predecessors. */
	private List<Direction> createPath(Node node) {
		final LinkedList<Direction> result = Lists.newLinkedList();
		while (node.getParent() != null) {
			if (node.stoppedAt(node.getParent())) {	// there was a stop in parent point
				result.addFirst(node.getDirection());
				for (int i = 0; i < node.getElapsedFrames() - node.getParent().getElapsedFrames() - 1; i++) {
					result.addFirst(null);
				}
			} else {	// there was no stop in parent point
				for (int i = 0; i < node.getElapsedFrames() - node.getParent().getElapsedFrames(); i++) {
					result.addFirst(node.getDirection());
				}
			}
			node = node.getParent();
		}
		return result;
	}

	/** Finds the closest cell not endangered by any bombs after the bot had entered it. */
	private List<Direction> findShelter(final GameState gs, final PointCoord start, final int startFrame,
			final INeighborhoodGenerator n, final ISafetyChecker s) {
		final Set<PointWithStopList> checked = Sets.newHashSet();
		final PriorityQueue<Node> toCheck = new PriorityQueue<Node>();
		final Node startNode = new Node(start, startFrame, 0);
		final GridCoord dummyDestination = new GridCoord(0, 0);
		toCheck.add(startNode);

		while (!toCheck.isEmpty()) {
			final Node current = toCheck.peek();

			// if the trip was found
			if (s.isUltimatelySafe(gs, current, current.getElapsedFrames())) {
				return createPath(current);
			}

			// add all neighbors to "toCheck" list
			for (Node node : n.getNeighbors(gs, current, dummyDestination)) {
				if (!checked.contains(node) && !toCheck.contains(node)) {
					if (!checked.contains(node)) {
						toCheck.add(node);
					}
				}
			}

			checked.add(current);
			toCheck.poll();
		}

		return null;
	}

}
