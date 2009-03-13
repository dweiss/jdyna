package com.kdyna.gamestate;

import java.awt.Point;

import org.jdyna.Globals;

/**
 * Class represents single bomb
 * @author Krzysztof P
 *
 */
public class Bomb {

	final private Point position;
	private PlayerInfo owner;
	private int range;
	private int counter; 
	
	public Bomb(PlayerInfo owner, int x, int y) {
		this.owner = owner;
		range = Globals.DEFAULT_BOMB_RANGE;	
		counter = Globals.DEFAULT_FUSE_FRAMES + 1;
		if (owner != null) {
			range = owner.getBombRange();
		} 
		position = new Point(x, y);

	}
	
	public int getRange() {
		return range;
	}
	
	public void decreaseCounter(int value) {
		counter -=  value;
	}
	
	public void setCounter(int value) {
		counter = value;
	}
	
	public int getCounter() {
		return counter;
	}
	
	public int getX() {
		return position.x;
	}
	
	public int getY() {
		return position.y;
	}
	
	public PlayerInfo getOwner() {
		 return owner;
	}
}
