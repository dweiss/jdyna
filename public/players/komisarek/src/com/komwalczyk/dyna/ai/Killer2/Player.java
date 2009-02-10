package com.komwalczyk.dyna.ai.Killer2;

import static com.dawidweiss.dyna.Globals.DEFAULT_BOMB_COUNT;
import static com.dawidweiss.dyna.Globals.DEFAULT_BOMB_RANGE;

import java.awt.Point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Player class. Has info about number of bombs, their range and players position. 
 */
public class Player
{
    private final static Logger logger = LoggerFactory.getLogger(Bomb.class);
	private int bombs = DEFAULT_BOMB_COUNT;
	private int range = DEFAULT_BOMB_RANGE;
	private String name;
	private Point position;
	/**
	 * Indicates if player can pick bonuses - player can't pick bonuses if its dead or immortal. This is set during updating players.
	 */
	private boolean pickBonuses;
	
	public Player(String name, Point position)
	{
		this.name = name;
		this.position = position;
	}
	
	public Point getPosition()
	{
		return position;
	}

	public void setPosition(Point position)
	{
		this.position = position;
	}

	public int getBombs()
	{
		return bombs;
	}

	public int getRange()
	{
		return range;
	}

	public String getName()
	{
		return name;
	}

	public void increaseBombs()
	{
		bombs++;
		logger.debug("Increases bombs. " + this + "So we have: " + bombs);
	}
	
	public void increaseRange()
	{
		range++;
		logger.debug("Increases range. " + this);
	}
	
	public String toString()
	{
		return "killer.Player with name: " + name;
	}

	public void decreaseBomb()
	{
		bombs--;
		logger.debug("Decreasing bombs! So we have: " + bombs);
	}

	public boolean isPickBonuses() {
		return pickBonuses;
	}

	public void setPickBonuses(boolean pickBonuses) {
		this.pickBonuses = pickBonuses;
	}
}
