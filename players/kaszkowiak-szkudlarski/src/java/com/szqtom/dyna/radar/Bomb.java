package com.szqtom.dyna.radar;


import java.awt.Point;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.Globals;

class Bomb implements IBomb {

	private int frame;
	private int x, y;

	public Bomb(int x, int y, int frame) {
		this.x = x;
		this.y = y;
		this.frame = frame;
	} 

	@Override
	public boolean isInDestructionZone(int x, int y, Cell[][] board) {
		if (this.x != x && this.y != y) {
			return false;
		} else if (this.x == x && this.y == y) {
			return true; 
		} else if ((this.x == x)
				&& (Math.abs(this.x - x) <= Globals.DEFAULT_BOMB_RANGE)) {
			if (this.y > y) {
	 	 		for (int j = y; j < this.y; j++) {
					CellType type = board[x][j].type;
					if (type == CellType.CELL_WALL) {
						return false;
					}
				}
				return true;
			} else {
				for (int j = this.y; j < y; j++) {
					CellType type = board[x][j].type;
					if (type == CellType.CELL_WALL) {
						return false;
					}
				}
				return true;
			}
		} else if ((this.y == y)
				&& (Math.abs(this.x - x) <= Globals.DEFAULT_BOMB_RANGE)) {
			if (this.x > x) {
				for (int i = x; i < this.x; i++) {
					CellType type = board[i][y].type;
					if (type == CellType.CELL_WALL) {
						return false;
					}
				}
				return true;
			} else {
				
				for (int i = this.x; i < x; i++) {
					CellType type = board[i][y].type;
					if (type == CellType.CELL_WALL) {
						return false;
					}
				}
				
				return true;
			}
		}
		return false;
	}  
	
	@Override
	public int frameToExpolosion(int actualFrame) {
		return (frame + Globals.DEFAULT_FUSE_FRAMES - actualFrame);
	}

	@Override
	public boolean equal(IBomb bomb) {
		Bomb b = (Bomb) bomb;
		return ((x == b.getX()) && (y == b.getY()));
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public boolean equal(int x, int y) {
		return ((this.x == x) && (this.y == y));
	}
	
	@Override
	public boolean isInPoint(Point point){
		if(x == point.x && y == point.y){
			return true;
		}
		return false;
	}
}
