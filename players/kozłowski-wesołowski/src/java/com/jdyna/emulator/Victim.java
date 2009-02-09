package com.jdyna.emulator;

import java.util.List;

import com.dawidweiss.dyna.GameEvent;

/**
 * Player that lives just for fun (cannon fodder). Doesn't analyze game state, doesn't move, doesn't drop bombs.
 * 
 * @author Michał Kozłowski
 */
public class Victim extends AbstractPlayerEmulator {

	/** @param name Player's name. */
	public Victim(final String name) {
		super(name);
	}

	@Override
	public void onFrame(int frame, List<? extends GameEvent> events) {
		// nop
	}

	@Override
	public boolean dropsBomb() {
		return false;
	}

	@Override
	public Direction getCurrent() {
		return null;
	}

}
