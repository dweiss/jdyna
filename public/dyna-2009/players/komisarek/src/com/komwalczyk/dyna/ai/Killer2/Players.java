package com.komwalczyk.dyna.ai.Killer2;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdyna.BoardInfo;
import org.jdyna.IPlayerSprite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Logic for players. Updating collectin bonuses. Positions.
 *
 */
public class Players
{
    private final static Logger logger = LoggerFactory.getLogger(Players.class);
	
    /**
     * Maps grid points to list of players. 
     */
    private final Map<Point, List<Player>> players = new HashMap<Point, List<Player>>();
	/**
	 * Maps name of player to player.
	 */
	private final Map<String, Player> namesPlayers = new HashMap<String, Player>();
	
	
	//if 2 players on one point first from the list is returned...
	public Player getPlayer(Point p)
	{
		if (players.get(p) == null)
		{
			return null;
		}
		return players.get(p).get(0);
	}

	public Player getPlayer(String name)
	{
		return namesPlayers.get(name);
	}
	
	/**
	 * Updates players (and adds new players to namesPlayers should happen very rarely).
	 * @param players2 list containing player sprite.
	 * @param boardInfo board info about current game
	 */
	public void updatePlayers(List<? extends IPlayerSprite> players2, BoardInfo boardInfo)
	{
		players.clear();
		for (IPlayerSprite playerSprite : players2)
		{
			String currentName = playerSprite.getName();
			if (boardInfo == null) //null because we skipped start game event - its game over for us
			{
				logger.warn("null in update players..");
				return;
			}
			Point pos = boardInfo.pixelToGrid(playerSprite.getPosition());
			if (!namesPlayers.containsKey(playerSprite.getName()))
			{
				logger.debug("No player named: " + playerSprite.getName() + " adding.");
				Player player = new Player(currentName, pos);
				player.setPickBonuses(playerSprite.isDead() || playerSprite.isImmortal());
				List<Player> list = new ArrayList<Player>();
				list.add(player);
				
				players.put(pos, list);
				namesPlayers.put(currentName, player);
			}
			else
			{	
				List<Player> playersOnCurrentPoint = players.get(pos);
				Player currentPlayer = namesPlayers.get(currentName);
				currentPlayer.setPickBonuses(!(playerSprite.isDead() || playerSprite.isImmortal()));
				currentPlayer.setPosition(playerSprite.getPosition());
				if (playersOnCurrentPoint != null)
				{
					logger.debug("We have second player in same point...");
				}
				else
				{
				}
				playersOnCurrentPoint = new ArrayList<Player>();
				playersOnCurrentPoint.add(currentPlayer);
				players.put(pos, playersOnCurrentPoint);
			}
		}
	}

	/**
	 * Checks if players take certain bonus.
	 * @param bonuses
	 */
	public void checkForBonuses(Bonuses bonuses)
	{
		Set<Point> newBombs = bonuses.getBombBonuses();
		Set<Point> ranges = bonuses.getRangeBonuses();
		for (Point point : players.keySet())//we will check only first player - only one can pick bonus (if player immortal then we check another one)
		{
			List<Player> list = players.get(point);
			int size = list.size();
			Player player = null;
			for (int i = 0; i < size; i++)
			{
				if (list.get(i).isPickBonuses())
				{
					player = list.get(i);
					break;
				}
			}
			if (player == null)
				continue;
			if (newBombs.contains(point))
			{
				player.increaseBombs();
				newBombs.remove(point);
				logger.debug("Player: " + player + " took new bomb!!");
			}
			
			if (ranges.contains(point))
			{
				player.increaseRange();
				ranges.remove(point);
				logger.debug("Player: " + player + " took new range!!" + "His range: " + player.getRange());
			}
		}
	}
	/**
	 * Get closest player to given player. 
	 * @param myName
	 * @return
	 */
	public Player getClosest(String myName) //TODO really closest now only one 
	{
		for (Player player : namesPlayers.values())
		{
			if (player.getName().equals(myName) || !player.isPickBonuses())
			{
				continue;
			}
			return player;
		}
		
		return null;
	}
}
