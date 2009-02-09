package com.szqtom.dyna.radar;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.Globals;
import com.szqtom.dyna.generator.Point;

public class BombsDetector {
	
	private List<ChainBombs> bombs = new ArrayList<ChainBombs>();
	
	private Logger log = Logger.getLogger(BombsDetector.class.getName());
	
	public BombsDetector(){
		
	}
	
	public boolean pathInExplodeZone(Point curr_point, LinkedList<Point> path, Cell[][] board,int actualFrame){
		int i = 0;
		for (Point point : path) {

			if(isBombInPath(path, actualFrame) || isBombInPoint(curr_point)){
				return true;
			}
			
			if(willExplode(point, board, i, actualFrame)){
				return true;
			}		
			i++;
		}
		return false;
	}
	
	private boolean isBombInPoint(Point point){
		for (IBomb bomb : bombs) {			
			if(bomb.isInPoint(point)){
				return true;
			}	
		}
		return false;
	}
	
	private boolean isBombInPath(LinkedList<Point> path, int actualFrame){
		for (Point point : path) {
			for (IBomb bomb : bombs) {			
				if(bomb.isInPoint(point)){
					return true;
				}
			}

		}
		return false;
	}
	
	public boolean willExplode(Point position, Cell[][] board, int steps, int actualFrame) {
		for (IBomb bomb : bombs) {
			if (bomb.isInDestructionZone(position.x, position.y, board)) {				
				if(bomb.frameToExpolosion(actualFrame) > ((steps)* Globals.DEFAULT_CELL_SIZE/8 - Globals.DEFAULT_CELL_SIZE)  && bomb.frameToExpolosion(actualFrame) < ((steps) * Globals.DEFAULT_CELL_SIZE/8 + Globals.DEFAULT_CELL_SIZE)  ){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean willExplode(Point position, Cell[][] board) {
		for (IBomb bomb : bombs) {
			if (bomb.isInDestructionZone(position.x, position.y, board)) {
				return true;
			}
		}
		return false;
	}

	synchronized public void updateBombsList(Cell[][] board, int frame) {
		Iterator<ChainBombs> iter = bombs.iterator();
		while (iter.hasNext()) {
			IBomb bomb = iter.next();
			if (bomb.frameToExpolosion(frame) < 0) {
				iter.remove();
			}
		}

		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				if (board[i][j].type == CellType.CELL_BOMB) {
					boolean newBomb = true;
					for (ChainBombs chain : bombs) {
						if (chain.equal(i, j)) {
							newBomb = false;
						}
					}
					if (newBomb) {
						addNewBomb(i, j, board, frame);
					}
				}
			}
		}

	}

	private void addNewBomb(int x, int y, Cell[][] board,int frame) {
		ChainBombs lastChain = null;
		Iterator<ChainBombs> iterBombs = bombs.iterator();
		while (iterBombs.hasNext()) {
			ChainBombs chain = iterBombs.next();
			if (chain.isInDestructionZone(x, y, board)) {				
					if (lastChain == null) {
						chain.add(x, y, frame);
						lastChain = chain;
						iterBombs.remove();
					} else {
						chain.add(lastChain);
						lastChain = chain;
						iterBombs.remove();
					}
				}
			}		
		if (lastChain == null) {
			bombs.add(new ChainBombs(x, y, frame));
		} else {
			bombs.add(lastChain);
		}

	}
}
