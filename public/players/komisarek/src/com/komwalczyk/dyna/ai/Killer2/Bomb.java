package com.komwalczyk.dyna.ai.Killer2;

import static org.jdyna.Globals.DEFAULT_FUSE_FRAMES;

import java.awt.Point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bomb. Has fuse counter and calculated range. 
 *
 */
public class Bomb
{
	//suppressed warning cause we need logger sometime
    @SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory.getLogger(Bomb.class);

	/**
	 * Each bomb has default fuse frame at the begining.
	 */
	private int fuseCounter = DEFAULT_FUSE_FRAMES;
	
	/**
	 * Calculated range. 
	 */
	private int range;
	
	/**
	 * Bomb belongs to that player (we need to increase number of given player bombs)
	 */
	private Player belongsTo;
	
	/**
	 * Bombs position.
	 */
	private Point position;
	
	public void setFuseCounter(int fuseCounter) {
		this.fuseCounter = fuseCounter;
	}

	public Bomb(Point position, Player owner)
	{
		this.position = position;
		this.range = owner.getRange();
		this.belongsTo = owner;
	}
	
	/**
	 * Decrase fuse counter by given number of frames. 
	 * @param frames frames by which we decrease
	 * @return true if the bomb should explode
	 */
	public boolean decreaseFuseCounter(int frames)
	{
//		logger.debug("Range of bomb: " + range);
		fuseCounter -= frames;
//		logger.debug("Decreasing fuse counter by: " + frames + " current fuse: " + fuseCounter + " on point " + this.getPosition());
		return fuseCounter <= 0;
	}

	public int getFuseCounter()
	{
		return fuseCounter;
	}

	public int getRange()
	{
		return range;
	}

	public Point getPosition()
	{
		return position;
	}
	
	/**
	 * Invoked when bomb blows - increases connected player's bomb counter.
	 */
	public void blow()
	{
		belongsTo.increaseBombs();
//		logger.debug("Bomb explodes. Giving " + belongsTo.getName() + " one bomb");
	}
}
