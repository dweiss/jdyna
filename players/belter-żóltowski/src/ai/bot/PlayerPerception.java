package ai.bot;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ai.navigation.Metrics;
import ai.navigation.NavGraph;
import ai.navigation.Node;
import ai.navigation.NodeType;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.IPlayerSprite;
import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Stores computer knowledge about the game state and other players
 */
public class PlayerPerception {
	/** previous game state */
	private Cell[][] previousState;
	
	/** current game state */
	private Cell[][] nextState;
 	
	/** game navigation graph */
	private NavGraph graph;
	
	/** list of players attributes deduced from game play */
	private List<PlayerAttributes> players;
	
	/** information about map dimension and other useful methods */
	private BoardInfo boardInfo;
	
	/** stores current ticking bombs on the map given map location */
	private BiMap<Point, BombAttributes> tickingBombs;
	
	/**
	 * Constructs player perception
	 * @param boardInfo information about 
	 * @param graph navigation graph
	 */
	public PlayerPerception(BoardInfo boardInfo, NavGraph graph) {
		Dimension dim = boardInfo.gridSize;
		this.previousState = new Cell[dim.width][dim.height];
		this.nextState = new Cell[dim.width][dim.height];
		this.boardInfo = boardInfo;
		this.graph = graph;
		this.tickingBombs = Maps.newHashBiMap();
		
		for (int i = 0; i < dim.width; i++) {
			for (int j = 0; j < dim.height; j++) {
				previousState[i][j] = Cell.getInstance(CellType.CELL_EMPTY);
				nextState[i][j] = Cell.getInstance(CellType.CELL_EMPTY);
			}
		}
	}
	
	/**
	 * Class for comparing distance players' distance to given point on the map
	 */
	class PlayerDistanceToTargetComparator implements Comparator<PlayerAttributes> {
		/** target point we want to reach given in pixel coordinates */
		private Point target;
		
		/**
		 * Constructs comparator 
		 * @param target point we want to reach given in pixel coordinates
		 */
		public PlayerDistanceToTargetComparator(Point target) {
			this.target = target;
		}
		
		/** {@inheritDoc} */
		public int compare(PlayerAttributes p1, PlayerAttributes p2) {
			 double playerDistance1 = p1.isDead ? Double.MAX_VALUE : Metrics.manhattanDistance(p1.position, target);
			 double playerDistance2 = p2.isDead ? Double.MAX_VALUE : Metrics.manhattanDistance(p2.position, target);
			 
			 if (playerDistance1 < playerDistance2) return -1;
			 else if (playerDistance1 == playerDistance2) return 0;
			 else return 1;
		 };
	}
	
	/**
	 * Returns sorted list of players that are closest to given point on the map
	 * @param x x-axis pixel coordinates
	 * @param y y-axis pixel coordinates
	 * @return list for players sorted by distance to target
	 */
	public List<PlayerAttributes> getNearestPlayersToTarget(int x, int y) {
		Collections.sort(players, new PlayerDistanceToTargetComparator(new Point(x, y)));
		return Collections.unmodifiableList(players);
	}
	
	/**
	 * Returns nearest alive player to given grid cell
	 * @param x x-axis grid coordinates
	 * @param y y-axis grid coordinates
	 * @return closest player to given grid cell
	 */
	public PlayerAttributes getNearestPlayerToCell(int x, int y) {
		Point cellPixelCoord = boardInfo.gridToPixel(new Point(x, y));
		PlayerAttributes nearestPlayer = null;
		double minDistance = Double.MAX_VALUE;

		for (PlayerAttributes player : players) {
			if (player.isDead) continue;
			
			double currDistance = Metrics.manhattanDistance(player.position, cellPixelCoord);
			if (currDistance < minDistance) {
				minDistance = currDistance;
				nearestPlayer = player;
			}
		}
		return nearestPlayer;
	}
	
	/**
	 * Creates deep copy of <code>Cells</code>' array 
	 * @param array two-dimensional array we want to clone
	 * @return cloned array
	 */
	private Cell[][] cloneCells(Cell[][] array) {
		Cell[][] copy = new Cell[boardInfo.gridSize.width][boardInfo.gridSize.height];
		for (int i = 0; i < array.length; i++) {
			copy[i] = Arrays.copyOf(array[i], array[i].length);
		}
		return copy;
	}
	
	/**
	 * Adapts <code>IPlayerSprite</code> list to list of <code>PlayerAttributes</code>
	 * @param playerSprites array of players given from server
	 * @return adapted list of player attributes
	 */
	private List<PlayerAttributes> adaptPlayers(List<? extends IPlayerSprite> playerSprites) {
		List<PlayerAttributes> players = Lists.newArrayListWithExpectedSize(playerSprites.size());
		for (IPlayerSprite playerSprite : playerSprites) {
			players.add(new PlayerAttributes(playerSprite));
		}
		return players;
	}
	
	/**
	 * Check if given cell type appeared at given location
	 * @param location cell location 
	 * @param type cell type whose appearance we want to test
	 * @return true if given type appeared at given location, false otherwise
	 */
	private boolean hasTypeAppeared(Point location, CellType type) {
		return previousState[location.x][location.y].type != type &&
			nextState[location.x][location.y].type == type;
	}
	
	/**
	 * Check if given cell type disappeared at given location
	 * @param location cell location 
	 * @param type cell type whose disappearance we want to test
	 * @return true if given type disappeared at given location, false otherwise
	 */
	private boolean hasTypeDisappeared(Point location, CellType type) {
		return previousState[location.x][location.y].type == type &&
			nextState[location.x][location.y].type != type;
	}
	
	/**
	 * Decrements each ticking bomb by given frames numbers
	 * @param framesNum number of frames
	 */
	private void decrementBombTimers(int framesNum) {
		for (BombAttributes bomb : tickingBombs.values()) {
			bomb.fuseCounter -= framesNum;
		}
	}
	
	/**
	 * Updates player perception about the game state taking into account new game state.
	 * @param e
	 */
	public void update(GameStateEvent e) {
		nextState = cloneCells(e.getCells());
		players = adaptPlayers(e.getPlayers());
		Node[][] nodes = new Node[boardInfo.gridSize.width][boardInfo.gridSize.height];
		
		decrementBombTimers(1);
		
		for (int i = 0; i < boardInfo.gridSize.width; i++) {
			for (int j = 0; j < boardInfo.gridSize.height; j++) {
				Point location = new Point(i, j);
				PlayerAttributes playerNearCell = getNearestPlayerToCell(i, j);
				if (hasTypeDisappeared(location, CellType.CELL_BONUS_RANGE)) {
					playerNearCell.maxBombRange++;
				} else if (hasTypeDisappeared(location, CellType.CELL_BONUS_BOMB)) {
					playerNearCell.maxBombNum++;
				} else if (hasTypeDisappeared(location, CellType.CELL_BOMB)) {
					BombAttributes bomb = tickingBombs.remove(location);
					if (bomb != null) bomb.player.bombCounter++;
				} else if (hasTypeAppeared(location, CellType.CELL_BOMB)) {
					playerNearCell.bombCounter--;
					BombAttributes bomb = new BombAttributes(playerNearCell, location);
					tickingBombs.put(location, bomb);
				}
				nodes[i][j] = adaptToNode(nextState[i][j], i, j);
			}
		}
		bombPreignition(nodes);
		graph.updateNodes(nodes);
		
		previousState = nextState;
	}
	
	/**
	 * Prematurely set off bombs so as to computer takes allowances 
	 * for that while path and goal planing.  
	 * @param nodes matrix of nodes that need to updated
	 */
	private void bombPreignition(Node[][] nodes) {
		for (BombAttributes bomb : tickingBombs.values()) {
			if (bomb.fuseCounter <= 0) {
				fireBomb(bomb, nodes);
			}
		}
	}
	
	/**
	 * Fires single bomb. Adapted from corresponding method in <code>BoardUtilities</code> due to
	 * hidden visibility.
	 * @param bomb bomb that it is about to blow
	 * @param nodes
	 */
	private void fireBomb(BombAttributes bomb, Node[][] nodes) {
		final int x = bomb.location.x;
		final int y = bomb.location.y;
		final int range = bomb.range;
        final int xmin = Math.max(0, x - range);
        final int xmax = Math.min(boardInfo.gridSize.width - 1, x + range);
        final int ymin = Math.max(0, y - range);
        final int ymax = Math.min(boardInfo.gridSize.height - 1, y + range);

        // Propagate in all directions from the centerpoint.
        List<Point> processed = Lists.newArrayList();
        processed.add(bomb.location);
        final Node node = nodes[x][y];
        node.setType(NodeType.EXPLOSION);
        explode0(nodes, processed, range, x - 1, xmin, -1, x, y, true);
        explode0(nodes, processed, range, x + 1, xmax, +1, x, y, true);
        explode0(nodes, processed, range, y - 1, ymin, -1, x, y, false);
        explode0(nodes, processed, range, y + 1, ymax, +1, x, y, false);
	}
	
	 /**
     * Helper method for {@link #explode(List, int, int, int)}, propagation
     * of the explosion. Adapted from corresponding method in <code>BoardUtilities</code> due to
	 * hidden visibility.
     */
    private void explode0(
        Node[][] nodes, List<Point> processed, int range, int from, int to, int step,
        final int x, final int y, boolean horizontal)
    {
        for (int i = from; i != to + step; i += step) {
            final int lx = (horizontal ? i : x);
            final int ly = (horizontal ? y : i);

            final Node node = nodes[lx][ly];
            switch (node.getType()) {
            	case CRATE:
            	case WALL:
            		return;
				case BOMB:
					BombAttributes bomb = tickingBombs.get(new Point(x, y));
					if (bomb != null && !processed.contains(bomb.location)) {
						fireBomb(bomb, nodes);
					}
					break;
				case EMPTY:
				case BONUS:
					node.setType(NodeType.EXPLOSION);
					break;
				default:
					break;
			}
        }
    }
	
	/**
	 * Updates current state of graph and its nodes.
	 * @param cells cells from new frame
	 */
	private Node adaptToNode(Cell cell, int i, int j) {
		switch (cell.type) {
		case CELL_BOMB:
			return new Node(NodeType.BOMB, i, j);
		case CELL_CRATE:
		case CELL_CRATE_OUT:
			return new Node(NodeType.CRATE, i, j);
		case CELL_BONUS_BOMB:
		case CELL_BONUS_RANGE:
			return new Node(NodeType.BONUS, i, j);
		case CELL_WALL:
			return new Node(NodeType.WALL, i, j);
		default:
			if (cell.type.isExplosion()) {
				return new Node(NodeType.EXPLOSION, i, j);
			} else {
				return new Node(NodeType.EMPTY, i, j);
			}
		}
	}

	/**
	 * Returns list of players
	 * @return list of players attributes
	 */
	public List<PlayerAttributes> getPlayers() {
		return players;
	}
	
	/**
	 * Returns player by his/her given name
	 * @param name name of player
	 * @return player attributes
	 */
	public PlayerAttributes getPlayerByName(String name) {
		for (PlayerAttributes pl : players) {
            if (name.equals(pl.name)) {
                return pl;
            }
        }
        throw new RuntimeException("Player not on the list of players: " + name);
	}
}
