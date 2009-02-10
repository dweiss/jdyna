package ai.navigation;

import com.dawidweiss.dyna.Globals;

/**
 * Type of nodes for adjusting strategy.
 */
public enum NodeType {
	CRATE(Globals.DEFAULT_FUSE_FRAMES, 'X'), DORMANT_BOMB(50, 'B'), BOMB(5000, 'b'), WALL(50000, '#'), EXPLOSION(
			50000, 'o'), EMPTY(1, ' '), BONUS(1, '=');

	/**
	 * Default constructor
	 * @param cost unit cost of traversing node of this type.
	 * @param code ASCII code representing this type's node.
	 */
	private NodeType(int cost, char code) {
		this.cost = cost;
		this.code = code;
	}

	/**
	 * Unit cost of traversing all nodes of this type. Used by path finding algorithm.
	 */
	private int cost;
	
	/**
	 * ASCII code assigned to represent this node. 
	 */
	private char code;

	/*
	 * 
	 */
	public int getCost() {
		return cost;
	}
	
	/*
	 * 
	 */
	public char getCode() {
		return code;
	}
}
