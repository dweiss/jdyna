package com.kdyna;

import java.util.HashMap;

import org.jdyna.IPlayerController;
import org.jdyna.IPlayerFactory;





public class KDynaPlayerFactory implements IPlayerFactory {
	private HashMap<String, KPlayer> players = new HashMap<String, KPlayer>();
	
	public IPlayerController getController(String playerName) {
		if (!players.containsKey(playerName)) {
			players.put(playerName, new KPlayer(playerName));
		}
		return players.get(playerName);
	}

	public String getDefaultPlayerName() {
		return "SupaHiro";
	}

	public String getVendorName() {
		return "Krzysztof Piechociński";
	}
}

