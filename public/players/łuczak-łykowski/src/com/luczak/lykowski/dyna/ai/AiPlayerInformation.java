package com.luczak.lykowski.dyna.ai;

import java.awt.Point;
import java.util.ArrayList;

import org.jdyna.Globals;

/**
 * Basic information about the players. Players positions are stored in other
 * class.
 * 
 * @author Ewa Łuczak
 */
public class AiPlayerInformation {
	/*
	 * How many bombs player can set
	 */
	private int bombCount;
	/*
	 * Range of these bombs
	 */
	private int bombRange;
	/*
	 * Player name
	 */
	private String playerName;
	/*
	 * Bombs positions
	 */
	private ArrayList<Point> bombs = new ArrayList<Point>();

	/**
	 * Constructor for this class
	 * 
	 * @param playerName
	 * @param bombCount
	 * @param bombRange
	 */
	public AiPlayerInformation(String playerName, int bombCount, int bombRange) {
		this.playerName = playerName;
		this.bombCount = bombCount;
		this.bombRange = bombRange;
	}

	public String getPlayerName() {
		return playerName;
	}

	public ArrayList<Point> getBombsPositions() {
		return bombs;
	}

	/*
	 * This method is invoked when player gets a bonus called the bonus bomb
	 */
	public void setBombCount() {
		this.bombCount += 1;
	}

	public int getBombCount() {
		return bombCount;
	}

	public void resetBombCount() {
		this.bombCount = Globals.DEFAULT_BOMB_COUNT;
	}

	/*
	 * This method is invoked when player gets a bonus called the range bonus
	 */
	public void setBombRange() {
		this.bombRange += 1;
	}

	public int getBombRange() {
		return bombRange;
	}

	public void resetBombRange() {
		this.bombRange = Globals.DEFAULT_BOMB_RANGE;
	}
}
