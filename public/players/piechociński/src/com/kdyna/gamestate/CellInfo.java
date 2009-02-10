package com.kdyna.gamestate;


import com.dawidweiss.dyna.CellType;


/**
 * Info about single cell on board
 * @author Krzysztof P
 *
 */
public class CellInfo {

	private CellType type;
	private int explosionCounter;
	private int timeToExplode;
	
	public final int x;
	public final int y;
	
	public CellInfo(int x,  int y){
		this.x = x;
		this.y = y;
	}
	
	public void setType(CellType type) {
		this.type = type;
	}
	
	public CellType getType() {
		return type;
	}
	
	public void setExplosionCounter(int value) {
		explosionCounter = value;
	}
	
	public int getExplosionCounter() {
		return explosionCounter;
	}
	
	public void setTimeToExplode(int value) {
		timeToExplode = value;
	}
	
	public boolean willExplode() {
		return timeToExplode != 0;
	}
	
	public int getTimeToExplode() {
		return timeToExplode;
	}
	
	
	
}
	

