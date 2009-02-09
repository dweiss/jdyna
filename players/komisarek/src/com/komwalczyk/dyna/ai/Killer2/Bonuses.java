package com.komwalczyk.dyna.ai.Killer2;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.CellType;

/**
 * Contains info about bonuses (new range and bombs).
 *
 */
public class Bonuses
{
    private final static Logger logger = LoggerFactory.getLogger(Bonuses.class);
	/**
	 * Set containing position of new bombs. 
	 */
	private Set<Point> bombBonuses = new HashSet<Point>();
	/**
	 * Set containing position of range bonuses.
	 */
	private Set<Point> rangeBonuses = new HashSet<Point>();
	
	public Set<Point> getBombBonuses()
	{
		return bombBonuses;
	}

	public Set<Point> getRangeBonuses()
	{
		return rangeBonuses;
	}

	/**
	 * Adds specified point to proper bonus set.
	 * @param point point with bonus (grid).
	 * @param type bonus type
	 */
	public void addBonus(Point point, CellType type) 
	{
		if (type == CellType.CELL_BONUS_BOMB)
		{
			bombBonuses.add(point);
		}
		else if(type == CellType.CELL_BONUS_RANGE)
		{
			rangeBonuses.add(point);
		}
		else
		{
			logger.error("Wrong type bassed to bonus. Shouldn't never happen");
		}
	}
	
	/**
	 * Get all bonuses.
	 * @return new set containing info of both new bombs and ranges.
	 */
	public Set<Point> getBonuses()
	{
		Set<Point> temp = new HashSet<Point>();
		temp.addAll(bombBonuses);
		temp.addAll(rangeBonuses);
		return temp;
	}
}
