package com.kdyna.gamestate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.dawidweiss.dyna.IPlayerSprite;

/**
 * Contains info about all players in game
 * 
 * @author Krzysztof P
 *
 */
public class PlayersManager {

	final private HashMap<String, PlayerInfo> players;

	public PlayersManager() {
		players = new HashMap<String, PlayerInfo>();
	}

	public void addPlayer(IPlayerSprite ps) {
		players.put(ps.getName(), new PlayerInfo(ps));		
	}

	public void update(List<? extends IPlayerSprite> sprites) {
		for (IPlayerSprite ps : sprites) {
			players.get(ps.getName()).update(ps);
		}
	}

	public Collection<PlayerInfo> getPlayers() {
		return players.values();
	}
	
	
	public PlayerInfo getPlayerAt(int x, int y) {
		for (PlayerInfo pi : players.values()) {
			if (pi.getCoords().x == x && pi.getCoords().y == y && pi.isActive())
				return pi;
		}
		return null;
	}

	public PlayerInfo getPlayerByName(String name) {
		return players.get(name);
	}
	
	
}
