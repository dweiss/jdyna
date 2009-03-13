package AIplayer.ai.in71391;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdyna.*;
import org.jdyna.IPlayerController.Direction;

import AIplayer.model.BfsInfo;
import AIplayer.model.PlayerInfo;


/**
 * Implementation of behavioral software agent for real time simulation. It is
 * responsible for choosing moves and putting a bomb by the player.
 * 
 * @author Marcin Witkowski
 * 
 */
public class AgentAI {

	/* Game board */
	private Cell[][] board;
	/* Game board with times of estimates blowing times */
	private short[][] bombsTime;
	/* Game board with estimates bombs ranges */
	private short[][] bombsRange;
	/* List of bonuses coordinations */
	private Point[] bonuses;
	/* Game board with times of estimates fire times on cells */
	private short[][] fusesTime;
	/* Details of BFS in and out parameters */
	private BfsInfo bfsInfo;

	/* Time from putting a bomb to blast */
	private final int bombDefault = Globals.DEFAULT_FUSE_FRAMES;
	/* Default cell size */
	private final int cellSize = Globals.DEFAULT_CELL_SIZE;

	/* List of enemies positions */
	private PlayerInfo[] enemiesPositions;
	/* Coordinates of cell I am standing at */
	private Point myPosition;
	/* Pixel coordinates I am standing at */
	private Point myPositionPixel;
	/* My bombs range */
	private short myRange;
	/* if I set an artificial bomb in enemy place */
	private boolean artificialBomb = false;

	/* Object performing BFS */
	private SearchPath search;

	private final static Logger logger = Logger.getLogger("AI.Agent");

	public AgentAI() {
	}
	
	public AgentAI(Cell[][] board, short[][] bombsTime, short[][] bombsRange,
			short[][] fusesTime, PlayerInfo[] enemies, Point myPosition,
			short myRange, Point[] bonuses) {
		setData(board, bombsTime, bombsRange,fusesTime, enemies, myPosition,
			myRange,bonuses);
	}

	/**
	 * Setting data for Agent AI uses to create one object in AIThread and safe some memory. 
	 */
	public void setData(Cell[][] board, short[][] bombsTime, short[][] bombsRange,
			short[][] fusesTime, PlayerInfo[] enemies, Point myPosition,
			short myRange, Point[] bonuses) {
		this.board = board;
		this.bombsTime = bombsTime;
		this.bombsRange = bombsRange;
		this.fusesTime = fusesTime;
		this.enemiesPositions = enemies;
		this.myPositionPixel = myPosition;
		this.myPosition = pixelToGrid(myPosition);
		this.bonuses = bonuses;
		this.search = new SearchPath(board);
		this.bfsInfo = new BfsInfo();
		this.myRange = myRange;
		artificialBomb = false;
	}
	
	
	/**
	 * Method uses by AIThread to get info whether to put a bomb or not
	 */
	public boolean getBomb() {
		return bfsInfo.isBomb();
	}

	/**
	 * Convert pixel coordinates to grid cell coordinates.
	 */
	private Point pixelToGrid(Point location) {
		return new Point(location.x / cellSize, location.y / cellSize);
	}

	/**
	 * Perform move action. Try several kind of behavior
	 * {@link #gatheringPowerUps()}, {@link #breakingBricks()},
	 * {@link #avoidingBombs()}, {@link #attackEnemy()} and choose the most
	 * priority one.
	 * 
	 * @return list of moves to perform
	 */
	public List<Direction> move() {
		List<Direction> path = null;

		attackEnemy();
		gatheringPowerUps();

		// search for crate to destroy
		bfsInfo.setBrick(true);

		// no bonus or enemy so crach some crate
		if (bfsInfo.isEnemy() == false	&& bfsInfo.isBonus() == false)
			breakingBricks();
		
		// set data
		search.searchSetData(board, bombsTime, bombsRange, fusesTime, bonuses,
				myPosition);
		
		// perform BFS
		if (!bfsInfo.isBomb()) {
			search.searchBFS(myPositionPixel, bfsInfo, enemiesPositions);

			if (bfsInfo.getEnemyPath() != null
					&& bfsInfo.getEnemyPath().get(0) != null) {
				path = bfsInfo.getEnemyPath();
				if (path.get(0) != null)
					logger.debug("Attack " + path.get(0));
			} else if (bfsInfo.getBonusPath() != null
					&& bfsInfo.getBonusPath().get(0) != null) {
				path = bfsInfo.getBonusPath();
				if (path.get(0) != null)
					logger.debug("POWER " + path.get(0));
			} else if (bfsInfo.getBrickPath() != null
					&& bfsInfo.getBrickPath().get(0) != null) {
				path = bfsInfo.getBrickPath();
				if (path.get(0) != null)
					logger.debug("BRICK " + path.get(0));
			}
		}
		// I would like to put a bomb
		if (bfsInfo.isBomb()) {
			board[myPosition.x][myPosition.y] = Cell
					.getInstance(CellType.CELL_BOMB);
			bombsTime[myPosition.x][myPosition.y] = bombDefault;
			bombsRange[myPosition.x][myPosition.y] = myRange;

			search.searchSetData(board, bombsTime, bombsRange, fusesTime,
					bonuses, myPosition);

			ArrayList<Direction> pathPr = new ArrayList<Direction>();
			pathPr.add(null);

			path = avoidingBombs(pathPr);
		} else if (path == null) {
			path = avoidingBombs(null);
		} else {
			// check whether chosen path is safe
			if (!bfsInfo.isCheckAvoid()) {
				path = avoidingBombs(path);
			}
		}

		if (path != null) {
			return path;
		}
		// if putting a bomb kill myself don't place bomb and avoid killing
		// yourself
		if (bfsInfo.isBomb()) {
			bfsInfo.setBomb(false);
			board[myPosition.x][myPosition.y] = Cell
					.getInstance(CellType.CELL_EMPTY);
			bombsTime[myPosition.x][myPosition.y] = 0;
			bombsRange[myPosition.x][myPosition.y] = 0;
			fusesTime[myPosition.x][myPosition.y] = 0;
			logger.debug("Refuse put a bomb");
			search.searchSetData(board, bombsTime, bombsRange, fusesTime,
					bonuses, myPosition);
			path = avoidingBombs(null);
		}

		if (path != null) {
			return path;
		}
		
		// take off artificial bombs maybe because of them you can't survive
		if (artificialBomb) {
			for (PlayerInfo pl : enemiesPositions) {
				if (Math.abs(myPosition.x - pl.getPosition_x())
						+ Math.abs(myPosition.y - pl.getPosition_y()) <= 3) {
					board[pl.getPosition_x()][pl.getPosition_y()] = Cell
							.getInstance(CellType.CELL_EMPTY);
					bombsTime[pl.getPosition_x()][pl.getPosition_y()] = 0;
					bombsRange[pl.getPosition_x()][pl.getPosition_y()] = 0;
				}
			}
			search.searchSetData(board, bombsTime, bombsRange, fusesTime,
					bonuses, myPosition);
			path = avoidingBombs(null);
		}
		return path;
	}

	/**
	 * Check whether there is some brick to destroy.
	 */
	public void breakingBricks() {
		// next to crate so destroy it
		if (board[myPosition.x - 1][myPosition.y].type
				.compareTo(CellType.CELL_CRATE) == 0
				|| board[myPosition.x + 1][myPosition.y].type
						.compareTo(CellType.CELL_CRATE) == 0
				|| board[myPosition.x][myPosition.y - 1].type
						.compareTo(CellType.CELL_CRATE) == 0
				|| board[myPosition.x][myPosition.y + 1].type
						.compareTo(CellType.CELL_CRATE) == 0)
			if (board[myPosition.x][myPosition.y].type
					.compareTo(CellType.CELL_BOMB) != 0) {
				bfsInfo.setBomb(true);
				logger.debug(" DROP BOMB " + bfsInfo.isBomb());
				return;
			}

		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				if (board[i][j].type.compareTo(CellType.CELL_CRATE) == 0) {
					bfsInfo.setBrick(true);
					return;
				}
			}
		}
		return;
	}

	/**
	 * Check whether there is some bonus on the board which i can reach.
	 */
	public void gatheringPowerUps() {
		int min = Integer.MAX_VALUE;
		Point closestBonus = null;
		if (bonuses.length > 0) {

			for (Point en : bonuses) {
				if (Math.abs(en.x - myPosition.x)
						+ Math.abs(en.y - myPosition.y) < min) {
					min = Math.abs(en.x - myPosition.x)
							+ Math.abs(en.y - myPosition.y);
					closestBonus = en;
				}
			}
			// check if enemy is closer to bonus than I am
			for (PlayerInfo pl : enemiesPositions) {
				Point enPt = new Point(pl.getPosition_x(), pl.getPosition_y());
				if (min
						- (Math.abs(closestBonus.x - enPt.x) + Math
								.abs(closestBonus.y - enPt.y)) > 4) {
					return;
				}
			}
			bfsInfo.setBonus(true);
		}
	}

	/**
	 * Check whether I am standing next to enemy.
	 */
	public void attackEnemy() {
		for (PlayerInfo pl : enemiesPositions) {
			// when we are on a bomb do not chase an enemy and get killed
			if (Math.abs(myPosition.x - pl.getPosition_x())
					+ Math.abs(myPosition.y - pl.getPosition_y()) <= 3) {
				if (board[myPosition.x][myPosition.y].type
						.compareTo(CellType.CELL_BOMB) == 0) {
					board[pl.getPosition_x()][pl.getPosition_y()] = Cell
							.getInstance(CellType.CELL_BOMB);
					bombsTime[pl.getPosition_x()][pl.getPosition_y()] = bombDefault;
					bombsRange[pl.getPosition_x()][pl.getPosition_y()] = 4;
					// put a bomb on the board, in place where enemy stand to
					// avoid getting killed stupidly
					artificialBomb = true;
					return;
				}
			}
			if ((Math.abs(myPosition.x - pl.getPosition_x()) <= 2 && Math
					.abs(myPosition.y - pl.getPosition_y()) == 0)
					|| (Math.abs(myPosition.y - pl.getPosition_y()) <= 2 && Math
							.abs(myPosition.x - pl.getPosition_x()) == 0)) {
				if (board[myPosition.x][myPosition.y].type
						.compareTo(CellType.CELL_BOMB) != 0) {
					// put a bomb on the board, to check whether it is a good
					// idea
					bfsInfo.setBomb(true);
					return;
				}
			}
		}
		if (board[myPosition.x][myPosition.y].type
				.compareTo(CellType.CELL_BOMB) != 0) {
			bfsInfo.setEnemy(true);
		}
		return;
	}

	/**
	 * Perform action that help to avoid get killed.
	 * 
	 * @return path to the safe place
	 */
	public List<Direction> avoidingBombs(List<Direction> protPath) {
		// Check whether there is some path propose
		if (protPath == null) {
			List<Direction> path = null;
			path = search.avoid(myPosition, myPositionPixel, null);
			return path;
		} else {
			List<Direction> path = null;
			path = search.avoid(myPosition, myPositionPixel, protPath);
			return path;
		}
	}

}
