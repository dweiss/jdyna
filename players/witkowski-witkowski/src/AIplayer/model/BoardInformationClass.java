package AIplayer.model;

import java.util.ArrayList;

/**
 * Class collecting information about whole board and players.
 * 
 * @author Łukasz Witkowski
 * 
 */
public class BoardInformationClass {

	/** Information about number of frame that board represent */
	private int frameNumber;

	/** information about board, arrayList if board isn't a rectangle */
	public ArrayList<ArrayList<SuperCell>> cells;

	/** information about players */
	public ArrayList<PlayerInfo> playersOnBoard;

	public BoardInformationClass() {
		cells = new ArrayList<ArrayList<SuperCell>>();
		playersOnBoard = new ArrayList<PlayerInfo>();
	}

	public BoardInformationClass clone() {
		BoardInformationClass bIC = new BoardInformationClass();
		bIC.setFrameNumber(this.getFrameNumber());
		for (int t = 0; t < this.cells.size(); t++) {
			ArrayList<SuperCell> superCellList = new ArrayList<SuperCell>();
			for (int tt = 0; tt < this.cells.get(t).size(); tt++) {
				superCellList.add(cells.get(t).get(tt).clone());
			}
			bIC.cells.add(superCellList);
		}
		for (int t = 0; t < this.playersOnBoard.size(); t++) {
			PlayerInfo pI = null;
			if (playersOnBoard.get(t) != null) {
				pI = new PlayerInfo(playersOnBoard.get(t).getName(),
						playersOnBoard.get(t).getBombsNumber(), playersOnBoard
								.get(t).getRange(), playersOnBoard.get(t)
								.getPosition_x(), playersOnBoard.get(t)
								.getPosition_y());
				pI.setDead(playersOnBoard.get(t).isDead());
				pI.setImmortal(playersOnBoard.get(t).isImmortal());
			}
			bIC.playersOnBoard.add(pI);
		}
		return bIC;
	}

	public void setFrameNumber(int frameNumber) {
		this.frameNumber = frameNumber;
	}

	public int getFrameNumber() {
		return frameNumber;
	}
}
