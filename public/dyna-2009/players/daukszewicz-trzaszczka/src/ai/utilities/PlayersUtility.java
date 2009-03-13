package ai.utilities;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdyna.IPlayerSprite;

import ai.player.PlayerInfo;

/**
 * 
 * @author Asia
 */
public class PlayersUtility {

	private Map<String, PlayerInfo> players;

	public PlayersUtility() {
		this.players = new HashMap<String, PlayerInfo>();
	}

	public Map<String, PlayerInfo> getPlayers() {
		return players;
	}

	public void setPlayers(Map<String, PlayerInfo> players) {
		this.players = players;
	}

	/**
	 * 
	 * returns our position(in pixel)
	 * 
	 * @param players
	 * @return
	 */
	public Point getMyPosition(List<? extends IPlayerSprite> players,
			String name) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getName().equals(name)) {
				return players.get(i).getPosition();
			}
		}
		return null;
	}

	/**
	 * 
	 * updates oponents position
	 * 
	 * @param playersIN
	 * @param frame
	 */
	public void updateOponentPositions(List<? extends IPlayerSprite> playersIN,
			int frame, PlayerInfo myPlayer) {
		int[] playerPosition;
		for (int i = 0; i < playersIN.size(); i++) {
			if (!playersIN.get(i).getName().equals(myPlayer.name)) {
				playerPosition = MathUtility.getPosition(playersIN.get(i)
						.getPosition());

				players.get(playersIN.get(i).getName()).position = playerPosition;
				players.get(playersIN.get(i).getName()).frame = frame;
			}
		}
	}

	/**
	 * return coordinates ( in pixels) to nearest oponent
	 * 
	 * @param players
	 * @return
	 */
	public Point getNearestOponentPointPosition(
			List<? extends IPlayerSprite> playersIN, Point fromPoint,
			PlayerInfo myPlayer) {
		IPlayerSprite playerSprite = getNearestOponent(playersIN, fromPoint, myPlayer);
		if (playerSprite == null) {
			return null;
		} else {
			return playerSprite.getPosition();
		}
	}

	/**
	 * 
	 * init oponents map
	 * 
	 * @param playersIN
	 * @param frame
	 */
	public Map<String, PlayerInfo> initPlayers(
			List<? extends IPlayerSprite> playersIN, String name, int frame) {
		PlayerInfo playerInfo;
		for (int i = 0; i < playersIN.size(); i++) {
			if (!playersIN.get(i).getName().equals(name)) {
				playerInfo = new PlayerInfo(playersIN.get(i).getName(), frame);

				players.put(playersIN.get(i).getName(), playerInfo);
			}
		}

		return players;
	}

	/**
	 * return nearest oponent
	 * 
	 * @param players
	 * @return
	 */
	private IPlayerSprite getNearestOponent(
			List<? extends IPlayerSprite> players, Point fromPoint,
			PlayerInfo myPlayer) {
		IPlayerSprite player, nearestPlayer = null;

		int minLength = Integer.MAX_VALUE;
		int distance;
		for (int i = 0; i < players.size(); i++) {
			player = players.get(i);
			if (player.getName().equals(myPlayer.name)) {
				continue;
			}
			distance = MathUtility.getManhatanDistance(fromPoint, player
					.getPosition());
			if (distance < minLength) {
				minLength = distance;
				nearestPlayer = player;
			}
		}
		return nearestPlayer;
	}
}
