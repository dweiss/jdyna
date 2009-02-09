package AIplayer.model;

/**
 * Class remembers basic information about player.
 * 
 * @author Lukasz Witkowski
 * 
 */
public class PlayerInfo {

	/**
	 * Name of player.
	 */
	private String name;

	/**
	 * Number of bombs current player could put up.
	 */
	private int bombsNumber;

	/**
	 * Range of player bombs.
	 */
	private int range;

	/**
	 * x-coordinate of player on board.
	 */
	private int position_x;

	/**
	 * y-coordinate of player on board.
	 */
	private int position_y;
	
	/**
	 * Is player dead?
	 */
	private boolean isDead;
	
	/**
	 * Is player immortal?
	 */
	private boolean isImmortal;
	
	public PlayerInfo(String name, int bombsNumber, int range, int position_x,
			int position_y) {
		this.name = new String(name);
		this.bombsNumber = bombsNumber;
		this.range = range;
		this.position_x = position_x;
		this.position_y = position_y;
		this.isDead=false;
		this.isImmortal=false;
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

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public int getRange() {
		return range;
	}

	public void setBombsNumber(int bombsNumber) {
		this.bombsNumber = bombsNumber;
	}

	public int getBombsNumber() {
		return bombsNumber;
	}

	public boolean isDead() {
		return isDead;
	}

	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}

	public boolean isImmortal() {
		return isImmortal;
	}

	public void setImmortal(boolean isImmortal) {
		this.isImmortal = isImmortal;
	}
}