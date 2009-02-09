package ai.navigation;

import java.awt.Point;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import ai.utils.UpdateablePriorityQueue;

import com.google.common.collect.Lists;

/**
 * Calculates the shortest path from source to target using A* algorithm.
 */
public class PathFinder {
	/** navigation graph */
	private Graph graph;

	/** keeps calculated path of last query */
	private List<Node> path;
	
	/** priority queue needed by A* algorithm */
	private UpdateablePriorityQueue<Node> priorityQueue;

	/** comparator for comparing traversal cost between two nodes */
	private Comparator<Node> comparator;
	
	/**
	 * Create path finder
	 * @param graph navigation graph
	 */
	public PathFinder(Graph graph) {
		this.graph = graph;
		this.path = Lists.newArrayList();
		this.comparator = new Comparator<Node>() {
			@Override
			public int compare(Node node1, Node node2) {
				return Double.compare(node1.getFCost(), node2.getFCost());
			}
		};
		this.priorityQueue = new UpdateablePriorityQueue<Node>(
				graph.getNodeNum(), comparator);
	}

	/**
	 * Clears calculation data from previous search.
	 */
	private void reset() {
		priorityQueue.clear();
		Iterator<Node> nodesIter = graph.getNodesIterator();
		while (nodesIter.hasNext()) {
			nodesIter.next().updateEstimation(0, 0, null);
		}
	}
	
	/**
	 * Get shortest path from 
	 * @param start start point
	 * @param end end point
	 * @return calculated shortest path
	 */
	public List<Node> getPath(Point start, Point end) {
		reset();
		Node source = graph.getNodeAt(start);
		Node target = graph.getNodeAt(end);
		priorityQueue.offer(source);

		while (!priorityQueue.isEmpty()) {
			Node nextClosedNode = priorityQueue.poll();
			nextClosedNode.setProcessed(true);

			/* check if we reached target */
			if (nextClosedNode.equals(target)) {
				return getReconstructedPath(start, end);
			}

			Iterator<Node> nodeIter = graph
					.getNeighbouringNodesIterator(nextClosedNode);
			while (nodeIter.hasNext()) {
				Node neighbourNode = nodeIter.next();
				if (neighbourNode.isProcessed()) {
					continue;
				}

				double hCost = Metrics
						.manhattanDistance(neighbourNode, target);
				double gCost = nextClosedNode.getGCost()
						+ neighbourNode.getType().getCost();

				/* when node first time spotted */
				if (!priorityQueue.contains(neighbourNode)
						&& !neighbourNode.isProcessed()) {
					neighbourNode.updateEstimation(gCost, hCost, nextClosedNode);
					priorityQueue.offer(neighbourNode);
				/* when this path is shorter than already found */
				} else if (!neighbourNode.isProcessed()
						&& gCost < neighbourNode.getGCost()) {
					neighbourNode.updateEstimation(gCost, hCost, nextClosedNode);
					priorityQueue.update(neighbourNode);
				}
			}
		}
		/* denote that such path does not exist */
		return null;
	}

	/**
	 * Calculates distance of previously found path.
	 * @return distance of previous path query
	 */
	public double getDistanceCost() {
		if (path != null && !path.isEmpty()) {
			return path.get(path.size() - 1).hCost;
		} else {
			return 0;
		}
	}

	/**
	 * Reconstructs path backtracking from end node. 
	 * @param start starting node of path query
	 * @param end end node of path query
	 * @return list of nodes 
	 */
	private List<Node> getReconstructedPath(Point start, Point end) {
		path = Lists.newLinkedList();
		Node node = graph.getNodeAt(end);
		do {
			path.add(node);
			node = node.getParent();
			
		} while (node != null && node.getParent() != null);

		Collections.reverse(path);
		return Collections.unmodifiableList(path);
	}

	/**
	 * Prints path plan. Used for testing purposes.
	 * @param start
	 * @param end
	 */
	public void print(Point start, Point end) {
		Node source = graph.getNodeAt(start);
		Node target = graph.getNodeAt(end);
		System.out.print(' ');
		for (int i = 0; i < graph.getDimension().width; i++) {
			System.out.print(i % 10);
		}
		System.out.println();
		for (int j = 0; j < graph.getDimension().height; j++) {
			System.out.print(j % 10);
			for (int i = 0; i < graph.getDimension().width; i++) {
				Node node = graph.getNodeAt(i, j);
				if (source != null && node.equals(source)) {
					System.out.print('+');
				} else if (target != null && node.equals(target)) {
					System.out.print('-');
				} else if (path.contains(node)) {
					System.out.print(path.indexOf(node) % 10);
				} else {
					System.out.print(node.getType().getCode());
				}
			}
			System.out.println();
		}
		System.out.println("done");
	}
}
