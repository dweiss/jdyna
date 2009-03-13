package com.kozmich.dyna.ai;

/**
 * Node in path.
 * 
 * @author Lukasz Kozminski
 * @author Tomasz Michalak
 * 
 */
public class Node implements Comparable<Node> {
	int row;
	int column;
	int name;

	public Node(int row, int column, int name) {
		this.row = row;
		this.column = column;
		this.name = name;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public int compareTo(Node n) {
		if (n == this)
			return 0;
		return -1;
	}

	@Override
	public String toString() {
		return String.valueOf(name);
	}
}
