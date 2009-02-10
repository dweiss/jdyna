package com.rychlicki.wilczak.dyna.player;

import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerFactory;

/**
 * IPlayerFactory implementation.
 */
public class PlayerFactoryImpl implements IPlayerFactory {

	@Override
	public IPlayerController getController(String playerName) {
		return new SmarterRabbit(playerName);
	}

	@Override
	public String getDefaultPlayerName() {
		return "Looser";
	}

	@Override
	public String getVendorName() {
		return "Karol&Darek LLP";
	}

}
