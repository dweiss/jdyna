package ai.navigation;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import com.google.common.collect.Lists;

/**
 * Navigation graph used by <code>PathFinder</code> class and <i>A*</i>
 * algorithm.
 */
public class NavGraph implements Graph {
	/** two-dimensional array of nodes */
	private Node[][] nodes;

	/** dimension of graph grids*/
	private Dimension dimension;

	/**
	 * 
	 */
	private Random rnd = new Random();
	
	/**
	 * Class for comparing to nodes according to their distance to specified target
	 */
	class NodeDistanceToTargetComparator implements Comparator<Node> {
		/** node we want to reach */
		private Node target;
		
		/**
		 * Constructs comparator
		 * @param target target point coordinates given in in node coordinates
		 */
		public NodeDistanceToTargetComparator(Point target) {
			this.target = getNodeAt(target);
		}
		
		/** {@inheritDoc} */
		public int compare(Node node1, Node node2) {
			 double nodeDistance1 = Metrics.manhattanDistance(node1, target);
			 double nodeDistance2 = Metrics.manhattanDistance(node2, target);
			 
			 if (nodeDistance1 < nodeDistance2) return -1;
			 else if (nodeDistance1 == nodeDistance2) return 0;
			 else return 1;
		 };
	}

	/**
	 * Creates navigation graph.
	 * @param dimension dimension of graph crisscross
	 */
	public NavGraph(Dimension dimension) {
		this.nodes = new Node[dimension.width][dimension.height];
		this.dimension = dimension;
	}
	
	/**
	 * Updates navigation graph.
	 * @param nodes nodes of graph 
	 */
	public void updateNodes(Node[][] nodes) {
		this.nodes = nodes;
	}

	/**
	 * Check if given point matches graph dimension constraints.
	 * @param x coordinates in x-axis
	 * @param y coordinates in y-axis
	 * @return true if given node coordinates is valid, false otherwise
	 */
	private boolean isValidNode(int x, int y) {
		return x >= 0 && y >= 0 && x < dimension.width && y < dimension.height;
	}

	/** {@inheritDoc} */
	public Node getNodeAt(Point point) {
		if (!isValidNode(point.x, point.y)) {
			throw new RuntimeException("Invalid argument");
		}
		return nodes[point.x][point.y];
	}

	/** {@inheritDoc} */
	public Node getNodeAt(int x, int y) {
		return getNodeAt(new Point(x, y));
	}

	/**
	 * Returns random node.
	 * @return random node
	 */
	public Node getRandomNode() {
		int index = rnd.nextInt(getNodeNum());
		return nodes[index / dimension.width][index % dimension.height];
	}

	/**
	 * Returns list of nodes that match specified type.
	 * @param type type of nodes to be returned.
	 * @return list of nodes
	 */
	public List<Node> getNodesByType(NodeType type) {
		List<Node> list = Lists.newArrayList();
		Iterator<Node> iter = getNodesIterator();
		while (iter.hasNext()) {
			Node node = iter.next();
			if (node.getType() == type) {
				list.add(node);
			}
		}
		return list;
	}

	/**
	 * Returns list of nodes that match specified type and are 
	 * within given rage from starting point. 
	 * @param type type of nodes to be returned.
	 * @param seed starting point
	 * @param range range/scope within which to search
	 * @return list of nodes
	 */
	public List<Node> getNodesInRangeByType(NodeType type, Point seed, int range) {
		List<Node> result = Lists.newArrayList();
		Node tmp = getNodeAt(seed);
		fetchNodes(type, tmp, range, 0, result);
		Collections.sort(result, new NodeDistanceToTargetComparator(seed));
		return result;
	}

	/**
	 * Supplementary method that recursively fetches specified type of nodes
	 * @param type type of nodes to be returned.
	 * @param seed starting point
	 * @param ranger range within which to search
	 * @param depth current depth in recursive tree
	 * @param result list of resulting nodes
	 */
	private void fetchNodes(NodeType type, Node seed, int range, int depth,
			List<Node> result) {
		if (depth >= range)
			return;
		
		if (seed.getType() == type) {
			result.add(seed);
		}

		Iterator<Node> iter = getNeighbouringNodesIterator(seed);
		while (iter.hasNext()) {
			Node node = iter.next();
			if (node.getType() == type) {
				result.add(node);
			}
			fetchNodes(type, seed, range, depth + 1, result);
		}
	}

	/**
	 * Returns number of nodes in graph.
	 * @return number of nodes
	 */
	public int getNodeNum() {
		return dimension.width * dimension.height;
	}

	/**
	 * Returns dimension of graph.
	 * @return dimension of graph
	 */
	public Dimension getDimension() {
		return dimension;
	}

	@Override
	public Iterator<Node> getNodesIterator() {
		return this.new NodesIterator();
	}

	@Override
	public Iterator<Node> getNeighbouringNodesIterator(Node node) {
		return this.new NeighbouringNodesIterator(node);
	}

	/**
	 * Iterator that iterates over adjacent nodes of a given node.
	 */
	private class NeighbouringNodesIterator implements Iterator<Node> {
		private Queue<Node> neighbouringNodes;

		/**
		 * Constructs iterator over neighbouring nodes 
		 * @param node node whose neighbourhood we want to get
		 */
		public NeighbouringNodesIterator(Node node) {
			neighbouringNodes = new LinkedList<Node>();

			int x = node.getLocation().x;
			int y = node.getLocation().y;

			if (isValidNode(x - 1, y)) {
				neighbouringNodes.add(nodes[x - 1][y]);
			}
			if (isValidNode(x + 1, y)) {
				neighbouringNodes.add(nodes[x + 1][y]);
			}
			if (isValidNode(x, y - 1)) {
				neighbouringNodes.add(nodes[x][y - 1]);
			}
			if (isValidNode(x, y + 1)) {
				neighbouringNodes.add(nodes[x][y + 1]);
			}
		}

		@Override
		public boolean hasNext() {
			return neighbouringNodes.size() > 0 ? true : false;
		}

		@Override
		public Node next() {
			return neighbouringNodes.poll();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Iterator that iterates over all nodes in graph.
	 */
	private class NodesIterator implements Iterator<Node> {
		private int currIndex;

		@Override
		public boolean hasNext() {
			return currIndex < getNodeNum();
		}

		@Override
		public Node next() {
			Node node = nodes[currIndex % dimension.width][currIndex
					/ dimension.width];
			currIndex++;
			return node;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	};
}
