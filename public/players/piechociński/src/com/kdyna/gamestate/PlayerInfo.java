package com.kdyna.gamestate;

import java.awt.Point;

import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IPlayerSprite;
import com.dawidweiss.dyna.IPlayerController.Direction;

/**
 * class representing info about single player
 * @author Krzysztof P
 *
 */
public class PlayerInfo {

	private String playerName;
	private Direction direction;
	private Point coords;
	private Point exactCoords;
	private int bombRange;
	private int bombsCapacity;
	private int bombsLeft;
	private boolean active;
	
	public PlayerInfo(final IPlayerSprite sprite) {
		coords = new Point(sprite.getPosition().x / Globals.DEFAULT_CELL_SIZE, sprite.getPosition().y / Globals.DEFAULT_CELL_SIZE);
		playerName = sprite.getName();	
		bombRange = Globals.DEFAULT_BOMB_RANGE;
		bombsCapacity = Globals.DEFAULT_BOMB_COUNT;
		bombsLeft = bombsCapacity;
		exactCoords = new Point(sprite.getPosition().x, sprite.getPosition().y);
	}
	
	public Point getCoords() {
		return coords;
	}
	
	public void update(final IPlayerSprite sprite) {
		coords.x = sprite.getPosition().x / Globals.DEFAULT_CELL_SIZE;
		coords.y = sprite.getPosition().y / Globals.DEFAULT_CELL_SIZE;
		exactCoords.x = sprite.getPosition().x;
		exactCoords.y = sprite.getPosition().y;
		if (active && sprite.isDead()) {
			bombRange = Globals.DEFAULT_BOMB_RANGE;
			bombsCapacity = Globals.DEFAULT_BOMB_COUNT;
			bombsLeft = bombsCapacity;
		}
		active = !sprite.isDead();
	}
	
	
	public void removeBomb() {
		bombsLeft--;
	}
	
	public void addBomb() {
		bombsLeft++;
	}
	
	public void increaseCapacity() {
		bombsCapacity++;
		bombsLeft++;
	}
	
	public int getCapacity() {
		return bombsCapacity;
	}
	
	public void increaseBombRange() {
		bombRange++;
	}
	
	public int getBombRange() {
		return bombRange;
	}
	
	public String getName() {
		return playerName;
	}
	
	public Direction getDirection() {
		return direction;
	}
	
	
	public Point getExactCoords() {
		return exactCoords;
	}
	
	public void setActive(boolean value) {
		active = value;
	}
	
	public boolean isActive() {
		return active;
	}
}
