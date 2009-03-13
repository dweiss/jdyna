package com.kozmich.dyna.ai;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.jdyna.*;
import org.jdyna.GameEvent.Type;


/**
 * This class is implementation of {@link IGameEventListener} and it is main
 * class for the project.
 * 
 * @author Lukasz Kozminski
 * @author Tomasz Michalak
 * 
 */
public class AiPlayer implements IPlayerController, IGameEventListener {

	/**
	 * Name of player
	 */
	private final String name;

	/**
	 * Determine if bomb should be dropped or not.
	 */
	private boolean bomb;

	/**
	 * Determine direction of moving.
	 */
	private Direction direction = null;

	/**
	 * AI player.
	 */
	private IPlayerSprite player;

	/**
	 * {@link GameStateEvent#getPlayers()}
	 */
	private List<? extends IPlayerSprite> players;

	/**
	 * Player position in grid.
	 */
	private Point position;

	/**
	 * {@link GameStateEvent#getCells()}
	 */
	private Cell[][] cells;

	/**
	 * Determine if it is first {@link GameStateEvent} received or not.
	 */
	boolean startGame = true;

	/**
	 * Used to calculate path for player.
	 */
	private final PathFinder pathFinder;

	/**
	 * List of objects that implements {@link IGameEventListener} and which are
	 * informed about state if it is changed.
	 */
	private List<IGameEventListener> listeners;

	/**
	 * Used to determine safe positions.
	 */
	private SecurityGuard securityGuard;

	/**
	 * Keep information about player bomb count and range.
	 */
	private PlayerManager playerManager;

	/**
	 * Compare two states.
	 */
	private StateManager stateManager;

	public AiPlayer(String name) {
		this.name = name;
		listeners = new ArrayList<IGameEventListener>();
		playerManager = new PlayerManager();
		listeners.add(playerManager);
		stateManager = new StateManager();
		securityGuard = new SecurityGuard(playerManager);
		listeners.add(securityGuard);
		pathFinder = new PathFinder(name);
		listeners.add(pathFinder);
	}

	/**
	 * {@link IPlayerController#dropsBomb()}
	 */
	@Override
	public boolean dropsBomb() {
		return bomb;
	}

	/**
	 * {@link IPlayerController#getCurrent()}
	 */
	@Override
	public Direction getCurrent() {
		return direction;
	}

	/**
	 * {@link IGameEventListener#onFrame(int, List)}
	 */
	@Override
	public void onFrame(int frame, List<? extends GameEvent> events) {
		for (Iterator<? extends GameEvent> iterator = events.iterator(); iterator.hasNext();) {
			GameEvent gameEvent = iterator.next();
			if (gameEvent.type == Type.GAME_STATE) {
				GameStateEvent gse = (GameStateEvent) gameEvent;
				getInterestingData(gse);
				if (startGame) {
					stateManager.updateState(new State(gse.getCells()));
					startGame = false;
					invokeListeners(frame, events);
				}
				makeMove(frame, events);
			}
		}
	}

	/**
	 * Invoke all listeners to update their data.
	 * 
	 * @param frame
	 *            - {@link IGameEventListener#onFrame(int, List)}
	 * @param events
	 *            - {@link IGameEventListener#onFrame(int, List)}
	 */
	private void invokeListeners(int frame, List<? extends GameEvent> events) {
		for (Iterator<IGameEventListener> iterator = listeners.iterator(); iterator.hasNext();) {
			IGameEventListener gameEventListener = iterator.next();
			gameEventListener.onFrame(frame, events);
		}
	}

	/**
	 * Get information from {@link GameStateEvent}.
	 * 
	 * @param gse
	 *            {@link GameStateEvent}
	 */
	private void getInterestingData(GameStateEvent gse) {
		cells = gse.getCells();
		player = getPlayer(gse.getPlayers());
		players = gse.getPlayers();
		position = PlayerUtils.pixelToGrid(player.getPosition());
	}

	/**
	 * Gets current game state.
	 * 
	 * @return {@link State}
	 */
	private State getBoardGameState() {
		return new State(cells);
	}

	/**
	 * Gets path to safe place if player is in danger.
	 * 
	 * @return {@link NodeList} if is any path, null otherwise
	 */
	private NodeList findSafePlace() {
		int minDistance = Integer.MAX_VALUE;
		int minX = -1;
		int minY = -1;
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Point point = new Point(i, j);
				if (securityGuard.isSafePlace(point) && cells[i][j].type != CellType.CELL_WALL) {
					int shortestDistance = pathFinder.getShortestDistance(pathFinder.getNode(cells, position),
							pathFinder.getNode(i, j));
					if (shortestDistance < minDistance) {
						minDistance = shortestDistance;
						minX = i;
						minY = j;
					}
				}
			}
		}
		if (minX != -1 && minY != -1) {
			return pathFinder.getShortestPath(pathFinder.getNode(cells, position), pathFinder.getNode(minX, minY));
		} else {
			return null;
		}
	}

	/**
	 * This method is invoked when GameStateEvent is received. It is responsible
	 * for choice of direction and the moment of putting the bomb.
	 * 
	 * @param frame
	 *            {@link IGameEventListener#onFrame(int, List)}
	 * @param events
	 *            {@link IGameEventListener#onFrame(int, List)}
	 */
	private void makeMove(int frame, List<? extends GameEvent> events) {
		final Node startNode = pathFinder.getNode(cells, position);
		final State actualState = getBoardGameState();
		boolean recalculateDirection = false;
		// check if bomb count has changed
		if (stateManager.isGameStateChanged(actualState)) {
			playerManager.updatePlayerInfo(stateManager.getSavedState(), actualState);
			invokeListeners(frame, events);
			recalculateDirection = true;
		}
		bomb = false;

		// if no bomb appeared and direction was chosen previously read saved
		// path
		if (!recalculateDirection && stateManager.getSavedState().isPathSet()) {
			Node nextNode = stateManager.getSavedState().getNode();
			if (securityGuard.isSafePlace(convertFromNodeToPoint(nextNode))) {
				if (hasReachedNode(nextNode)) {
					// this node is reached so we remove it from list
					Node destinationNode = stateManager.getSavedState().nextNode();

					// bombInvoker
					bomb = shouldDropBomb(destinationNode);
					if (bomb) {

					} else if (stateManager.getSavedState().isPathSet()) {
						direction = getDirection(position, convertFromNodeToPoint(destinationNode));
					}
				} else {
					direction = getDirection(position, convertFromNodeToPoint(nextNode));
				}
				savePathToActualState(actualState);
			} else {
				if (!securityGuard.isSafePlace(position)) {
					goToSafePlace(actualState);
				} else {
					savePathToActualState(actualState);
					direction = null;
				}
			}
		} else if (stateManager.isBombBonusAppear()) {
			// go to bomb bonus
			goToBonus(startNode, actualState, CellType.CELL_BONUS_BOMB);
		} else if (stateManager.isRangeBonusAppear()) {
			// go to range bonus
			goToBonus(startNode, actualState, CellType.CELL_BONUS_RANGE);
		} else {
			IPlayerSprite clossestEnemy = getClosestEnemy();
			if (clossestEnemy != null) {
				Node targetNode = pathFinder.getNode(cells, PlayerUtils.pixelToGrid(clossestEnemy.getPosition()));
				NodeList shortestPath = pathFinder.getShortestPath(startNode, targetNode);
				if (shortestPath.isNodeListSet()) {
					shortestPath.catNodeListToNextNode();
					actualState.setPath(shortestPath);
				} else {
					if (!securityGuard.isSafePlace(position)) {
						goToSafePlace(actualState);
					} else {
						direction = null;
					}
				}
			}
		}
		stateManager.updateState(actualState);
	}

	/**
	 * Determine if player should drop a bomb.
	 * 
	 * @param destNode
	 *            - node in which direction player is going to
	 * @return true if bomb should be placed
	 */
	private boolean shouldDropBomb(Node destNode) {
		if (destNode != null && !securityGuard.isSafePlace(pathFinder.getPoint(destNode))) {
			return false;
		}
		int closestEnemyDistance = getDistanceToClosestEnemy();
		if (closestEnemyDistance <= playerManager.getMyRange(this.name)) {
			return true;
		} else {
			Random rand = new Random();
			final int bomb_index = 13;
			if (bomb_index == rand.nextInt(100)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets path to bonus with specific type.
	 * 
	 * @param startNode
	 *            player position node
	 * @param actualState
	 *            actual game state
	 * @param type
	 *            {@link CellType#CELL_BONUS_BOMB} or
	 *            {@link CellType#CELL_BONUS_RANGE}
	 */
	private void goToBonus(final Node startNode, final State actualState, CellType type) {
		Node bonusBomb = getBonus(type);
		if (bonusBomb != null) {
			actualState.setPath(pathFinder.getShortestPath(startNode, bonusBomb));
		}
	}

	/**
	 * This is action which find path to safe place and set direction to it.
	 * 
	 * @param actualState
	 *            actual game state
	 */
	private void goToSafePlace(final State actualState) {
		NodeList nodeList = findSafePlace();
		if (nodeList != null && nodeList.isNodeListSet()) {
			Node node = nodeList.getNode();
			Point point = pathFinder.getPoint(node);
			direction = getDirection(position, point);
			actualState.setPath(nodeList);
		}
	}

	/**
	 * Determine if player reached specific node.
	 * 
	 * @param node
	 *            node to reach
	 * @return true if player reached specific node
	 */
	private boolean hasReachedNode(Node node) {
		Node playerNode = pathFinder.getNode(cells, position);
		return node.name == playerNode.name ? true : false;
	}

	/**
	 * Convert node to point.
	 * 
	 * @param node
	 * @return {@link Point}
	 */
	private Point convertFromNodeToPoint(Node node) {
		return pathFinder.getPoint(node);
	}

	/**
	 * Save path from previous state to actual state.
	 * 
	 * @param actualState
	 *            actual game state
	 */
	private void savePathToActualState(State actualState) {
		if (stateManager.getSavedState().isPathSet()) {
			actualState.setPath(stateManager.getSavedState().getPath());
		}
	}

	/**
	 * It is seeking the best bonus - if player can be first from all other
	 * players, it is not having to be the closest bonus.
	 * 
	 * @param bonusType
	 *            {@link CellType}
	 * @return node with bonus.
	 */
	private Node getBonus(CellType bonusType) {
		int shortestDistance = Integer.MAX_VALUE;

		// the closest found bonus
		Node closestBonus = null;
		// the closest found bonus where I will certainly be first
		Node bestBonus = null;

		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				if (cells[i][j].type == bonusType) {
					Node startNode = pathFinder.getNode(cells, position);
					Node targetNode = pathFinder.getNode(i, j);
					int bonusDistance = pathFinder.getShortestDistance(startNode, targetNode);
					if (shortestDistance > bonusDistance) {
						shortestDistance = bonusDistance;
						closestBonus = targetNode;
						if (!canEnemyBeFirst(targetNode, shortestDistance)) {
							bestBonus = targetNode;
						}
					}
				}
			}
		}
		return bestBonus != null ? bestBonus : closestBonus;
	}

	/**
	 * Check if other players could be first to target node.
	 * 
	 * @param targetNode
	 *            - target node
	 * @param shortestDistance
	 *            - my player distance
	 * @return true if other player could be first
	 */
	private boolean canEnemyBeFirst(Node targetNode, int shortestDistance) {
		for (IPlayerSprite enemy : players) {
			if (enemy.getName().equals(player.getName()))
				break;
			Point enemyPosition = PlayerUtils.pixelToGrid(enemy.getPosition());
			int enemyDistance = pathFinder.getShortestDistance(pathFinder.getNode(cells, enemyPosition), targetNode);
			if (shortestDistance > enemyDistance) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets closest other player in game.
	 * 
	 * @return closest player in game, null if no players found
	 */
	private IPlayerSprite getClosestEnemy() {
		int manhattanDistance = Integer.MAX_VALUE;
		IPlayerSprite closestEnemy = null;
		for (Iterator<? extends IPlayerSprite> iterator = players.iterator(); iterator.hasNext();) {
			IPlayerSprite enemy = iterator.next();
			if (!enemy.getName().equals(player.getName())) {
				int tempDistance = PlayerUtils
						.manhattanDistance(position, PlayerUtils.pixelToGrid(enemy.getPosition()));
				if (tempDistance < manhattanDistance) {
					manhattanDistance = tempDistance;
					closestEnemy = enemy;
				}
			}
		}
		return closestEnemy;
	}

	/**
	 * Calculate distance to closest player.
	 * 
	 * @return distance to closest player, {@link Integer#MAX_VALUE} if no
	 *         player found
	 */
	private int getDistanceToClosestEnemy() {
		int manhattanDistance = Integer.MAX_VALUE;
		for (IPlayerSprite enemy : players) {
			if (!enemy.getName().equals(player.getName())) {
				int tempDistance = PlayerUtils
						.manhattanDistance(position, PlayerUtils.pixelToGrid(enemy.getPosition()));
				if (tempDistance < manhattanDistance) {
					manhattanDistance = tempDistance;
				}
			}
		}
		return manhattanDistance;
	}

	/**
	 * Gets reference to this Ai player.
	 * 
	 * @param players
	 *            list of all players
	 * @return reference to this player, null if player is not fount in this
	 *         game state
	 */
	private IPlayerSprite getPlayer(List<? extends IPlayerSprite> players) {
		for (IPlayerSprite playerSprite : players) {
			if (playerSprite.getName().equals(name)) {
				return playerSprite;
			}
		}
		return null;
	}

	/**
	 * Get direction from one point to another. Points must be neighbours.
	 * 
	 * @param currentPoint
	 * @param destinationPoint
	 * @return {@link Direction}
	 */
	private Direction getDirection(Point currentPoint, Point destinationPoint) {
		if (currentPoint.getX() < destinationPoint.getX()) {
			return Direction.RIGHT;
		} else if (currentPoint.getX() > destinationPoint.getX()) {
			return Direction.LEFT;
		}
		if (currentPoint.getY() < destinationPoint.getY()) {
			return Direction.DOWN;
		}
		if (currentPoint.getY() > destinationPoint.getY()) {
			return Direction.UP;
		}
		return null;
	}

	public String getName() {
		return name;
	}

}
