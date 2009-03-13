package com.krzysztofkazmierczyk.dyna.client.AI;

import com.dawidweiss.dyna.corba.bindings.CControllerState;
import com.krzysztofkazmierczyk.dyna.GameStateEventUpdater;

/**
 * 
 * This interface has methods to play game (choose move).
 * 
 * @author kazik
 *
 */
public interface MoveChooserInterface {

	public CControllerState move(GameStateEventUpdater event);
}
