package AIplayer.model;

import java.util.ArrayList;

import com.dawidweiss.dyna.Cell;

/**
 * Collection of information pass through the Corba from Game controller.
 * 
 * @author Lukasz Witkowski
 * 
 */
public class GameStateInformation {

	/** Number of received frame */
	private final int frameNumber;
	/** Information about board cells */
	private Cell[][] cells;

	/** Information about players */
	public ArrayList<PlayerInfo> players;

	/**
	 * 
	 * @param cells
	 *            two dimensional table of board cells
	 * @param frameNumber
	 *            number of frame
	 */
	public GameStateInformation(Cell[][] cells, int frameNumber) {
		this.cells = new Cell[cells.length][cells[0].length];
		for (int i = 0; i < cells.length; i++)
			System.arraycopy(cells[i], 0, this.cells[i], 0, cells[i].length);
		this.frameNumber = frameNumber;
		players = new ArrayList<PlayerInfo>();
	}

	public int getFrameNumber() {
		return frameNumber;
	}

	public Cell[][] getCells() {
		return cells;
	}
}
