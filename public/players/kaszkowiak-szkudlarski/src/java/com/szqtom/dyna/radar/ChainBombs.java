package com.szqtom.dyna.radar;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.jdyna.Cell;



public class ChainBombs implements IBomb {

	private List<IBomb> bombs;
	
	
	public void add(int x, int y,int frame) {
		bombs.add(new Bomb(x,y,frame));
	}
	
	public void add(IBomb bomb) {
		bombs.add(bomb);
	}

	public ChainBombs(int x, int y, int frame) {
		bombs = new ArrayList<IBomb>();
		bombs.add(new Bomb(x, y, frame));
	}

	@Override
	public int frameToExpolosion(int actualFrame) {
		int framesLeft = Short.MAX_VALUE;
		for (IBomb bomb : bombs) {
			if (bomb.frameToExpolosion(actualFrame) < framesLeft) {
	 			framesLeft = bomb.frameToExpolosion(actualFrame);
			}
		}
		return framesLeft;
	}

	@Override
	public boolean isInDestructionZone(int x, int y, Cell[][] board) {
		for (IBomb bomb : bombs) {
			if(bomb.isInDestructionZone(x, y, board)) {
				return true;
			} 
		}
		return false;
	}
	
	@Override
	public boolean isInPoint(Point point){
		for (IBomb bomb : bombs) {
			if(bomb.isInPoint(point)) {
				return true;
			} 
		}
		return false;
	}

	@Override
	public boolean equal(IBomb bomb) {
		for (IBomb b : bombs) {
			if(b.equal(bomb))
				return true;
		}		
		return false;
	}

	@Override
	public boolean equal(int x, int y) {
		for (IBomb b : bombs) {
			if(b.equal(x,y))
				return true;
		}		
		return false;
	}

}
