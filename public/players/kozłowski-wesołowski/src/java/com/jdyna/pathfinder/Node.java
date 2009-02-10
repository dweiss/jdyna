package com.jdyna.pathfinder;

import com.dawidweiss.dyna.IPlayerController.Direction;
import com.jdyna.emulator.gamestate.PointCoord;

/**
 * Class representing a path node. It is used by the path finder.
 * 
 * @author Bartosz Wesołowski
 */
@SuppressWarnings("serial")
final class Node extends PointWithStopList implements Comparable<Node> {
	public enum MoveType {
		NONE, IN_CELL, EXITING_CELL
	};
	private Node parent;
	private int costFromStart;
	private final int costToGoal;
	private int elapsedFrames;
	private Direction direction;
	private MoveType moveType;

	/** Constructor for creating the first node- without any parent or direction. Cost from start is zero. */
	Node(final PointCoord point, final int firstFrame, final int costToGoal) {
		super(point);
		this.costFromStart = firstFrame;
		this.elapsedFrames = firstFrame;
		this.costToGoal = costToGoal;
	}

	/** Constructor for creating all nodes except the first one. */
	Node(final Neighbor neighbor, final Node parent, final int costFromStart, final int costToGoal,
			final int elapsedFrames) {
		super(neighbor, parent.stopPoints);
		this.costFromStart = costFromStart;
		this.costToGoal = costToGoal;
		this.direction = neighbor.direction;
		this.parent = parent;
		this.elapsedFrames = elapsedFrames;
		if (neighbor.exitsCell) {
			moveType = MoveType.EXITING_CELL;
		} else {
			moveType = MoveType.IN_CELL;
		}
		if (elapsedFrames > parent.elapsedFrames + neighbor.frames) {
			this.stopPoints.add(parent);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Node) {
			final Node other = (Node) obj;
			if (super.equals(other)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int compareTo(Node other) {
		if (getCost() > other.getCost()) {
			return 1;
		}
		if (getCost() < other.getCost()) {
			return -1;
		}
		return 0;
	}

	/** Returns the estimated trip cost. */
	public int getCost() {
		return costFromStart + costToGoal;
	}

	@Override
	public String toString() {
		return super.toString() + "=" + getCost();
	}

	/** Updates the PointNode's state using information from given node. */
	public void update(final Node node) {
		costFromStart = node.costFromStart;
		elapsedFrames = node.elapsedFrames;
		direction = node.direction;
		parent = node.parent;
		moveType = node.moveType;
	}

	public Node getParent() {
		return parent;
	}

	public Direction getDirection() {
		return direction;
	}

	public int getCostFromStart() {
		return costFromStart;
	}

	public int getElapsedFrames() {
		return elapsedFrames;
	}

	public MoveType getMoveType() {
		return moveType;
	}

}
