package com.luczak.lykowski.dyna.ai;

import java.util.List;

import org.jdyna.Cell;


/**
 * Class which is put in to the queue and delivered to AI thread. Class contains
 * information coped from frame game
 * 
 * @author Konrad Łykowski
 * 
 */
public class OnFrameInformation {

	/*
	 * Cells game
	 */
	private Cell[][] clonedCells;
	/*
	 * Player List
	 */
	private List<InitialAiPlayerInformation> clonedPlayerList;

	/**
	 * Constructor for this class
	 * 
	 * @param clonedCells
	 * @param clonedPlayerList
	 */
	public OnFrameInformation(Cell[][] clonedCells,
			List<InitialAiPlayerInformation> clonedPlayerList) {
		this.clonedCells = clonedCells;
		this.clonedPlayerList = clonedPlayerList;
	}

	/**
	 * Getter for:
	 * 
	 * @return Cell[][]
	 */
	public Cell[][] getCells() {
		return clonedCells;
	}

	/**
	 * Getter for:
	 * 
	 * @return List<InitialAiPlayerInformation>
	 */
	public List<InitialAiPlayerInformation> getPlayers() {
		return clonedPlayerList;
	}

}
