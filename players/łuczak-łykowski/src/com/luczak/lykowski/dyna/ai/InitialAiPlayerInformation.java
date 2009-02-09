package com.luczak.lykowski.dyna.ai;

import java.awt.Point;

/**
 * Initial information for our AI about the playerr
 * 
 * @author Konrad Łykowski
 * 
 */
public class InitialAiPlayerInformation {

	/*
	 * Name of the player
	 */
	private String name;
	/*
	 * Player position
	 */
	private Point position;
	/*
	 * Is he alive?
	 */
	private boolean dead;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param position
	 * @param dead
	 */
	public InitialAiPlayerInformation(String name, Point position, boolean dead) {
		this.name = name;
		this.position = position;
		this.dead = dead;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the position
	 */
	public Point getPosition() {
		return position;
	}

	/**
	 * @return the dead
	 */
	public boolean isDead() {
		return dead;
	}

}
