package ai.navigation;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Iterator;

/**
 * Interface for representing traversal graph.
 */
public interface Graph {
	/**
	 * Retrieves node at given point.
	 * @param point
	 * @return node
	 */
	public Node getNodeAt(Point point);
	
	/**
	 * Retrieves not at given (x, y) address.
	 * @param x 
	 * @param y
	 * @return node
	 */
	public Node getNodeAt(int x, int y);
	
	/**
	 * Returns number of nodes in traversal graph.
	 * @return number of nodes
	 */
	public int getNodeNum();
	
	/**
	 * Returns dimension of this graph.
	 * @return dimension of graph
	 */
	public Dimension getDimension();
	
	/**
	 * Retrieves iterator that iterates over all nodes in graph.
	 * @return nodes iterator
	 */
	public Iterator<Node> getNodesIterator();
	
	/**
	 * Retrieves iterator that iterates over adjacent nodes to given node.
	 * @param node node whose adjacent nodes we want to iterate over
	 * @return
	 */
	public Iterator<Node> getNeighbouringNodesIterator(Node node);
}
