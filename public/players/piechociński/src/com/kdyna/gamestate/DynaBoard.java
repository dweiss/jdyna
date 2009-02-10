package com.kdyna.gamestate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import com.dawidweiss.dyna.IPlayerController.Direction;
import com.kdyna.gamestate.DynaGameState.CellPredicate;

/**
 *  
 * @author Krzysztof P
 *
 */

public class DynaBoard {

	final private CellInfo[][] cells;
	
	public DynaBoard(CellInfo[][] cells) {
		this.cells = cells;
	}
	
	
	/**
	 * Method add all cells wchich meets criteria of predicate p and can be reach walking only
	 * on those cell in at most maxRange steps from point (x,y) to destination list
	 * 
	 */
	
	public void addConnectedCells(final List<CellInfo> dest, int x, int y, int maxRange, final CellPredicate p) {
		if (x >= 0 && x < cells.length && y >= 0 && y < cells[0].length) {
			if ( p.test(cells[x][y])) {
				if (!dest.contains(cells[x][y])){
					dest.add(cells[x][y]);
				}
				if (maxRange > 0) {
					addConnectedCells(dest, x, y - 1, maxRange - 1, p );
					addConnectedCells(dest, x + 1, y, maxRange - 1, p );
					addConnectedCells(dest, x - 1, y, maxRange - 1, p );
					addConnectedCells(dest, x, y + 1, maxRange - 1, p );		
				}
			}
		}
	}
	
	

	
	/**
	 * 		 
	 *  Finding shortest route(use Breadth-first search) - allowed cells are determined by passed predicate
	 *  Result contains start and end point;
	 *  
	 * @return Route from start to destination or null if it doesn't exist
	 */
	public Stack<CellInfo> findRoute(final CellInfo from, final CellInfo to, final CellPredicate predicate) {
		
		final ArrayDeque<CellInfo> queue = new ArrayDeque<CellInfo>();
		final Stack<CellInfo> route = new Stack<CellInfo>();
	
		//map (visited cell -> predecessor)
		final HashMap<CellInfo, CellInfo> visited = new HashMap<CellInfo, CellInfo>();

		queue.add(from);

		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[j].length; j++) {
				visited.put(cells[i][j], null);
			}
		}
		
		final ArrayList<CellInfo> neighbours = new ArrayList<CellInfo>();
		
		visited.put(from, from);
		while (!queue.isEmpty()) {
			CellInfo ci = queue.pollFirst();
			if (ci == to) break;
			neighbours.clear();
			addConnectedCells(neighbours, ci.x, ci.y, 1, predicate);
			for (CellInfo nci : neighbours) {
				if (ci == to) break;
				if (visited.get(nci) == null) {
					visited.put(nci, ci);
					queue.add(nci);
				}
			}
			
		}
		
		route.push(to);
		CellInfo prev = visited.get(to);
		while (prev != from) {
			route.add(prev);
			prev = visited.get(prev);
			if (prev == null) return null; // starting point cannot be reached -> route doesn't exist
		}
		route.push(from);
		return route;
	}
	
	
	/**
	 * Standard finding shortest route algorithm - all walkable and only walkable cells are allowed
	 * 
	 * @return Route from start to destination or null if it doesn't exist
	 */
	public Stack<CellInfo> findRoute(final CellInfo from, final CellInfo to) {
		
		CellPredicate pred = new CellPredicate() {
			public boolean test(CellInfo ci) {
				return (ci.getType().isWalkable() || ci == from);
			}			
		};
		return findRoute(from, to, pred);
	}
	
	/**
	 * Method returns Direction object required to walk from coordinates (x1, y1) to (x2, y2)
	 *  
	 */
	
	public Direction getDirection(int x1, int y1, int x2, int y2) {
		if (x1 == x2 && y1 < y2) {
			return Direction.DOWN;
		} else if (x1 == x2 && y1 > y2) {
			return Direction.UP;
		} else if (x1 > x2 && y1 == y2) {
			return Direction.LEFT;
		} else if (x1 < x2 && y1 == y2) {
			return Direction.RIGHT;
		}
		return null;
	}
	
	/**
	 * Method calculates distance between two cells
	 *  
	 */
	
	public int getDistance(CellInfo c1, CellInfo c2) {
		CellPredicate pred = new CellPredicate() {
			public boolean test(CellInfo ci) {
				return (ci.getType().isWalkable());
			}			
		};
		Stack<CellInfo> route = findRoute(c1, c2, pred);
		if (route == null) {
			return -1;
		} else {
			return route.size() - 1;
		}
	}
	
	public CellInfo getCell(int x, int y) {
		return cells[x][y];
	}
	
	public int getWidth() {
		return cells.length;
	}
	
	public int getHeight() {
		return cells[0].length;
	}
}
