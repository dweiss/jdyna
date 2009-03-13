package com.komwalczyk.dyna.ai.Killer2;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
  * This class stores info about players bomb.
  */
public class PlayersBomb
{
	private Map<Point, Bomb> bombs;
	
	public PlayersBomb()
	{
		bombs = new HashMap<Point, Bomb>();
	}

	public Bomb getBomb(Point p)
	{
		return bombs.get(p);
	}
	
	public void putBomb(Point point, Bomb bomb)
	{
		bombs.put(point, bomb);
	}
	
	public void removeBomb(Point point)
	{
		bombs.remove(point);
	}
	
	public Collection<Bomb> getBombs()
	{
		return bombs.values();
	}
}
