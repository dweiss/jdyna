package AIplayer.model;

import org.jdyna.IPlayerController;

/**
 * Pure controller that is send by corba to Game controller and change move
 * direction or put up a bomb.
 * 
 * @author Lukasz Witkowski
 * 
 */
public class PureController implements IPlayerController {
	/** If player wants to put up a bomb. */
	private final boolean dropBomb;
	/** Direction player wants to move. */
	private final Direction current;

	public PureController(boolean dropBomb, Direction current) {
		this.dropBomb = dropBomb;
		this.current = current;
	}

	public boolean dropsBomb() {
		return dropBomb;
	}

	public Direction getCurrent() {
		return current;
	}

}
