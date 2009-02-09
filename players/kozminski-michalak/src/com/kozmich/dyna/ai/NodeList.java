package com.kozmich.dyna.ai;

import java.util.List;

/**
 * Wrapper for {@link List<Node>}.
 * 
 * @author Lukasz Kozminski
 * @author Tomasz Michalak
 * 
 */
public class NodeList {

	private final int TARGET_INDEX = 2;
	/**
	 * 0 - start player position, 1 - destination position
	 */
	private final int START_INDEX = 1;

	/**
	 * Index of node where player is going to.
	 */
	private int actualNodeIndex = START_INDEX;

	private List<Node> nodeList;

	public NodeList(List<Node> nodeList) {
		this.nodeList = nodeList;
	}

	public NodeList() {
		this.nodeList = null;
	}

	public boolean isNodeListSet() {
		if (nodeList == null || actualNodeIndex >= nodeList.size()) {
			return false;
		} else {
			return true;
		}
	}

	public Node nextNode() {
		Node node = null;
		actualNodeIndex++;
		if (isNodeListSet() && actualNodeIndex < nodeList.size()) {
			node = nodeList.get(actualNodeIndex);
		}
		return node;
	}

	public Node getPreviousNode() {
		Node node = null;
		if (isNodeListSet() && actualNodeIndex - 1 >= 0) {
			node = nodeList.get(actualNodeIndex - 1);
		}
		return node;
	}

	public Node getNode() {
		Node node = null;
		if (isNodeListSet() && actualNodeIndex < nodeList.size()) {
			node = nodeList.get(actualNodeIndex);
		}
		return node;
	}

	public void catNodeListToNextNode() {
		nodeList = nodeList.subList(0, TARGET_INDEX);
		actualNodeIndex = 1;
	}

}
