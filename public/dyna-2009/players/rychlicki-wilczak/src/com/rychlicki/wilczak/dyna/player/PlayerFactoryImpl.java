package com.rychlicki.wilczak.dyna.player;

import org.jdyna.IPlayerController;
import org.jdyna.IPlayerFactory;

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
