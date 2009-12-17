package org.jdyna.view.jme;

import org.jdyna.IPlayerController;

import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;

public class JMEPlayerController implements IPlayerController {

	private static JMEPlayerController controllers[] = new JMEPlayerController[] {
		new JMEPlayerController(1),
		new JMEPlayerController(2)
	};
	
	private static final KeyBindingManager km = KeyBindingManager.getKeyBindingManager();
	
	/**
	 * Current set of controls for players using the 3d view.
	 */
	static {
		km.set("up_1",KeyInput.KEY_UP);		
		km.set("down_1",KeyInput.KEY_DOWN);		
		km.set("left_1",KeyInput.KEY_LEFT);		
		km.set("right_1",KeyInput.KEY_RIGHT);		
		km.set("bomb_1",KeyInput.KEY_RCONTROL);
		
		km.set("up_2",KeyInput.KEY_W);		
		km.set("down_2",KeyInput.KEY_S);		
		km.set("left_2",KeyInput.KEY_A);		
		km.set("right_2",KeyInput.KEY_D);		
		km.set("bomb_2",KeyInput.KEY_LCONTROL);
	}

	private int playerNumber;
	
	
	public JMEPlayerController(int playerNumber) {
		this.playerNumber = playerNumber;
	}

	static public JMEPlayerController instance(int iPlayer) {
		
		return controllers[iPlayer-1];
	}
	
	@Override
	public boolean dropsBomb() {
		
		return km.isValidCommand("bomb_" + playerNumber);
	}

	@Override
	public Direction getCurrent() {
		
		Direction dir = null;
		
		if (km.isValidCommand("up_" + playerNumber))
			dir = Direction.UP;
		else if (km.isValidCommand("down_" + playerNumber))
			dir = Direction.DOWN;
		else if (km.isValidCommand("left_" + playerNumber))
			dir = Direction.LEFT;
		else if (km.isValidCommand("right_" + playerNumber))
			dir = Direction.RIGHT;
			
		return dir;
	}

}
