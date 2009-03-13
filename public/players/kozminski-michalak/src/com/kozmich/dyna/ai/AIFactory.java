package com.kozmich.dyna.ai;

import org.jdyna.IPlayerController;
import org.jdyna.IPlayerFactory;

/**
 * Factory class for AI player.
 * 
 * @author Lukasz Kozminski
 * @author Tomasz Michalak
 * 
 */
public class AIFactory implements IPlayerFactory {

	@Override
	public IPlayerController getController(String playerName) {
		return new AiPlayer("Katun");
	}

	@Override
	public String getDefaultPlayerName() {
		return "Katun";
	}

	@Override
	public String getVendorName() {
		return "Lukasz Kozminski, Tomasz Michalak";
	}

}
