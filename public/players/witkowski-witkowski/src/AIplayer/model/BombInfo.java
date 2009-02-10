package AIplayer.model;

/**
 * Class remember information about bomb put on some cell on board.
 * 
 * @author Lukasz Witkowski
 * 
 */
public class BombInfo {

	/** Name of player who put a bomb */
	private String ownerName;

	/** Number of frame when the bomb will blow up */
	private int timeToBlow;

	/** Range of bombs flames */
	private int range = 0;

	/** x-coordinate where bomb stand */
	private int position_x;

	/** y-coordinate where bomb stand */
	private int position_y;

	public BombInfo(String ownerName, int timeToBlow, int range,
			int position_x, int position_y) {
		this.setOwnerName(ownerName);
		this.setTimeToBlow(timeToBlow);
		this.setRange(range);
		this.setPosition_x(position_x);
		this.setPosition_y(position_y);
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setTimeToBlow(int timeToBlow) {
		this.timeToBlow = timeToBlow;
	}

	public int getTimeToBlow() {
		return timeToBlow;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public int getRange() {
		return range;
	}

	public void setPosition_x(int position_x) {
		this.position_x = position_x;
	}

	public int getPosition_x() {
		return position_x;
	}

	public void setPosition_y(int position_y) {
		this.position_y = position_y;
	}

	public int getPosition_y() {
		return position_y;
	}

}
