package com.kozmich.dyna.ai;

import org.jdyna.Cell;

/**
 * Keep information about state of game.
 * 
 * @author Tomasz Michalak
 * @author Lukasz Kozminski
 * 
 */
public class State {

	private final Cell[][] cells;

	private NodeList path;

	public State(Cell[][] cells) {
		this.cells = new Cell[cells.length][];
		copy(this.cells, cells);
	}

	/**
	 * Instead of clone() function.
	 * 
	 * @param dest
	 * @param src
	 */
	private void copy(Cell[][] dest, Cell[][] src) {
		for (int i = 0; i < src.length; i++) {
			dest[i] = new Cell[src[i].length];
			for (int j = 0; j < src[i].length; j++) {
				dest[i][j] = src[i][j];
			}
		}
	}

	/**
	 * 
	 * 
	 * @return true if there is element in path
	 */
	public boolean isPathSet() {
		if (path != null) {
			return path.isNodeListSet();
		} else {
			return false;
		}
	}

	/**
	 * Get first node from path and remove it from it.
	 * 
	 * @return null if no path node is enabled
	 */
	public Node nextNode() {
		if (path != null) {
			return path.nextNode();
		} else {
			return null;
		}
	}

	/**
	 * Get first node from path.
	 * 
	 * @return null if no path node is enabled
	 */
	public Node getNode() {
		if (path != null) {
			return path.getNode();
		} else {
			return null;
		}
	}

	public NodeList getPath() {
		return path;
	}

	public void setPath(NodeList path) {
		this.path = path;
	}

	public Cell[][] getCells() {
		return cells;
	}

	public Node getPreviousNode() {
		if (path != null) {
			return path.getPreviousNode();
		} else {
			return null;
		}
	}

}
