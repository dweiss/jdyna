package ai.navigation;

import java.awt.Point;

/**
 * Represents single node in navigation graph.
 */
public class Node {

	/**
	 * Construct node.
	 * 
	 * @param type
	 *            type of node
	 * @param x
	 *            x-axis address
	 * @param y
	 *            y-axis address
	 */
	public Node(NodeType type, int x, int y) {
		this(type, new Point(x, y));
	}

	/**
	 * Construct node.
	 * 
	 * @param type
	 *            type of node
	 * @param location
	 *            address of this node in graph
	 */
	public Node(NodeType type, Point location) {
		this.type = type;
		this.location = location;
	}
	
	/**
	 * Location of this node in graph.
	 */
	private Point location;

	/**
	 * Type of this node.
	 */
	private NodeType type;

	/** stores total traversal cost to node */
	double fCost;

	/** stores current real traversal cost to node */
	double gCost;

	/** stores heuristic traversal cost to node */
	double hCost;

	/** parent node that has the shortest path to this node */
	Node parent;

	/** denotes whether this node has already been processed */
	boolean processed;
	
	/**
	 * Updates calculation data
	 * @param gCost current observed cost
	 * @param hCost current estimated cost
	 * @param parent current parent on the searched path
	 */
	void updateEstimation(double gCost, double hCost, Node parent) {
		this.gCost = gCost;
		this.hCost = hCost;
		this.fCost = gCost + hCost;
		this.parent = parent;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Node))
			return false;

		Node node = (Node) obj;
		return this.location.equals(node.location);
	}

	@Override
	public int hashCode() {
		return this.location.hashCode();
	}

	public NodeType getType() {
		return type;
	}

	public void setType(NodeType type) {
		this.type = type;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	public double getFCost() {
		return fCost;
	}

	public void setFCost(double cost) {
		fCost = cost;
	}

	public double getGCost() {
		return gCost;
	}

	public void setGCost(double cost) {
		gCost = cost;
	}

	public double getHCost() {
		return hCost;
	}

	public void setHCost(double cost) {
		hCost = cost;
	}

	public String toString() {
		return String.format("[%d,%d, F=%.3f, G=%.3f, H=%.3f]", location.x,
				location.y, fCost, gCost, hCost);
	}

	public Point getLocation() {
		return new Point(location);
	}
}
