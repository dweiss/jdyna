package com.rychlicki.wilczak.dyna.player;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.BoardUtilities;
import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameStartEvent;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerSprite;
import com.google.common.collect.Lists;

/**
 * 
 * (Pseudo) AI player implementation.
 * 
 */
public class SmarterRabbit implements IPlayerController, IGameEventListener {

	/**
	 * Distance measurement fuzziness.
	 */
	private static final int FUZZINESS = Globals.DEFAULT_CELL_SIZE / 3;

	/** Trail history length. */
	private static final int TRAIL_SIZE = 5;

	/**
	 * Bomb frequency.
	 */
	private static final int BOMB_FREQ = 20;
	
	/**
	 * How often we accept frame - whole rest is skipped.
	 */
	private static final int ACC_FRAME_FREQ = 4;

	/**
	 * Wall value in prediction table.
	 */
	private static final int WALL_VALUE = 800;

	/** This player's name. */
	private String name;

	/** Target position we're aiming at, in pixels. */
	private Point target;

	private Random rnd;

	/**
	 * Short history of recently visited cells (trail). Positions in grid
	 * coords.
	 */
	private ArrayList<Point> trail = new ArrayList<Point>(TRAIL_SIZE);

	/**
	 * Prediction table with information about bombs.
	 */
	private int[][] prediction;

	/**
	 * Prediction table form previous frame.
	 */
	private int[][] previousPrediction;

	/**
	 * Frame counter.
	 */
	private int countFrame;

	/**
	 * Actual bomb range.
	 */
	private int bombRange = Globals.DEFAULT_BOMB_RANGE;

	/**
	 * Cached board info.
	 */
	private BoardInfo boardInfo;

	/**
	 * Controller information.
	 */
	private volatile Direction direction;

	/**
	 * Controller information.
	 */
	private volatile boolean dropsBomb;

	public SmarterRabbit(String name) {
		this.name = name;
		rnd = new Random();
	}

	@Override
	public boolean dropsBomb() {
		return dropsBomb;
	}

	@Override
	public Direction getCurrent() {
		return direction;
	}

	@Override
	public void onFrame(int frame, List<? extends GameEvent> events) {
		for (GameEvent event : events) {
			if (event.type == GameEvent.Type.GAME_START) {
				this.boardInfo = ((GameStartEvent) event).getBoardInfo();
				this.prediction = new int[boardInfo.gridSize.width][boardInfo.gridSize.height];
				this.previousPrediction = new int[boardInfo.gridSize.width][boardInfo.gridSize.height];
				this.target = null;
				this.trail.clear();
				this.direction = null;
			}

			if (event.type == GameEvent.Type.GAME_STATE) {
				countFrame++;
				// we don't need update our position on every frame!
				if (countFrame % ACC_FRAME_FREQ != 0) {
					continue;
				}
				final GameStateEvent gse = (GameStateEvent) event;

				final IPlayerSprite myself = identifyMyself(gse.getPlayers());
				if (myself.isDead()) {
					target = null;
				} else {
					final Point pixelPosition = myself.getPosition();
					final Point gridPosition = boardInfo
							.pixelToGrid(pixelPosition);

					makePrediction(gse.getCells());
					updateTrail(gridPosition);
					if (shouldChangeDirection(gse.getCells(), pixelPosition)) {
						target = pickNewLocation(gse.getCells(), pixelPosition);
						updateState(pixelPosition, target);
					}
				}
			}
		}
	}

	/**
	 * Compute prediction table.
	 */
	private void makePrediction(Cell[][] cells) {

		int[][] temp = previousPrediction;
		previousPrediction = prediction;
		prediction = temp;
		for (int i = 0; i < boardInfo.gridSize.width; i++) {
			for (int j = 0; j < boardInfo.gridSize.height; j++) {
				prediction[i][j] = 0;
				if (!cells[i][j].type.isWalkable()) {
					prediction[i][j] = WALL_VALUE;
					previousPrediction[i][j] = 0;
					continue;
				}
				if (cells[i][j].type.isLethal()) {
					prediction[i][j] = Integer.MAX_VALUE;
					previousPrediction[i][j] = 0;
					continue;
				}
				if (cells[i][j].type == CellType.CELL_BONUS_BOMB
						|| cells[i][j].type == CellType.CELL_BONUS_RANGE) {
					prediction[i][j] = -1000;
					continue;
				}
			}
		}

		for (int i = 0; i < boardInfo.gridSize.width; i++) {
			for (int j = 0; j < boardInfo.gridSize.height; j++) {
				if (cells[i][j].type == CellType.CELL_BOMB) {
					updatePredictionCells(i, j);
				}
			}
		}
	}

	/**
	 * Update cells of the prediction table.
	 */
	private void updatePredictionCells(int i, int j) {
		for (int k = bombRange * (-1); k < bombRange; k++) {
			if (k == 0) {
				continue;
			}
			if (i + k >= 0 && i + k < boardInfo.gridSize.width) {
				if (previousPrediction[i + k][j] != Integer.MAX_VALUE
						&& previousPrediction[i + k][j] != WALL_VALUE) {
					prediction[i + k][j] = Math.max(prediction[i + k][j], Math
							.max(100 + Math.abs((k - bombRange) * 10),
									previousPrediction[i + k][j] + 100
											+ Math.abs((k - bombRange) * 10)));
				} else {
					prediction[i + k][j] = Math.max(prediction[i + k][j],
							100 + Math.abs((k - bombRange) * 10));
				}
			}
			if (j + k >= 0 && j + k < boardInfo.gridSize.height) {
				if (previousPrediction[i][j + k] != Integer.MAX_VALUE
						&& previousPrediction[i][j + k] != WALL_VALUE) {
					prediction[i][j + k] = Math.max(prediction[i][j + k], Math
							.max(100 + Math.abs((k - bombRange) * 10),
									previousPrediction[i][j + k] + 100
											+ Math.abs((k - bombRange) * 10)));
				} else {
					prediction[i][j + k] = Math.max(prediction[i][j + k],
							100 + Math.abs((k - bombRange) * 10));
				}
			}
		}

	}

	/**
	 * Update controller state. Compute new controler's information.
	 */
	private void updateState(Point sourcePixels, Point targetPixels) {
		direction = null;
		dropsBomb = false;

		if (target == null) {
			return;
		}

		final Point sourceXY = boardInfo.pixelToGrid(sourcePixels);
		final Point targetXY = boardInfo.pixelToGrid(targetPixels);

		if (sourceXY.x < targetXY.x) {
			if (countFrame % BOMB_FREQ == 0) {
				dropsBomb = isDropBombSave(targetXY, Direction.RIGHT);
			}
			direction = Direction.RIGHT;

		} else if (sourceXY.x > targetXY.x) {

			if (countFrame % BOMB_FREQ == 0) {
				dropsBomb = isDropBombSave(targetXY, Direction.LEFT);
			}
			direction = Direction.LEFT;

		} else if (sourceXY.y < targetXY.y) {
			if (countFrame % BOMB_FREQ == 0) {
				dropsBomb = isDropBombSave(targetXY, Direction.DOWN);
				;
			}
			direction = Direction.DOWN;
		} else if (sourceXY.y > targetXY.y) {
			if (countFrame % BOMB_FREQ == 0) {
				dropsBomb = isDropBombSave(targetXY, Direction.UP);
			}
			direction = Direction.UP;
		}
	}

	/**
	 * Check if dropping bomb is safe.
	 */
	private boolean isDropBombSave(Point targetXY, Direction right) {
		switch (right) {
		case RIGHT:
		case LEFT:
			for (int i = bombRange * (-1) - 1; i < bombRange + 1; i++) {
				if ((targetXY.x + i) >= 0
						&& targetXY.x + i < boardInfo.gridSize.width
						&& (prediction[targetXY.x + i][targetXY.y] == Integer.MAX_VALUE || prediction[targetXY.x
								+ i][targetXY.y] == WALL_VALUE)) {
					return false;
				}
			}
			break;

		case UP:
		case DOWN:
			for (int i = bombRange * (-1) - 1; i < bombRange + 1; i++) {
				if ((targetXY.y + i) >= 0
						&& targetXY.y + i < boardInfo.gridSize.height
						&& (prediction[targetXY.x][targetXY.y + i] == Integer.MAX_VALUE || prediction[targetXY.x][targetXY.y
								+ i] == WALL_VALUE)) {
					return false;
				}
			}
			break;
		}
		return true;
	}

	/**
	 * Picks a new target location to go to.
	 */
	private Point pickNewLocation(Cell[][] cells, Point ourPosition) {
		final Point currentCell = boardInfo.pixelToGrid(ourPosition);

		final ArrayList<Point> possibilities = Lists
				.newArrayListWithExpectedSize(4);
		addPossible(possibilities, cells, currentCell, 0, -1);
		addPossible(possibilities, cells, currentCell, -1, 0);
		addPossible(possibilities, cells, currentCell, 1, 0);
		addPossible(possibilities, cells, currentCell, 0, 1);

		if (possibilities.size() == 0)
			return null;
		Point newLoc = chooseBest(possibilities, currentCell);
		if (newLoc == null) {
			return null;
		}
		return boardInfo.gridToPixel(newLoc);
	}

	/**
	 * Choose best target to go.
	 */
	private Point chooseBest(ArrayList<Point> possibilities, Point ourPosition) {

		Point result = possibilities.get(0);
		List<Point> resultList = Lists.newArrayListWithExpectedSize(4);
		resultList.add(result);
		for (Point point : possibilities) {
			if (prediction[point.x][point.y] < prediction[result.x][result.y]) {
				result = point;
				resultList.clear();
				resultList.add(point);
				continue;
			}
			if (prediction[point.x][point.y] == prediction[result.x][result.y]) {
				resultList.add(point);

			}
		}

		if (trail.size() > 1) {
			for (int i = 0; resultList.size() > 1 && i < trail.size(); i++) {
				final Point oneBefore = trail.get(i);
				resultList.remove(oneBefore);
			}
		}

		return resultList.get(rnd.nextInt(resultList.size()));
	}

	/**
	 * Update the trail history.
	 */
	private void updateTrail(Point position) {
		if (position == null)
			return;
		if (trail.size() > 0 && position.equals(trail.get(0)))
			return;

		trail.add(0, position);
		while (trail.size() > TRAIL_SIZE)
			trail.remove(trail.size() - 1);
	}

	/**
	 * Check possible target cell and add it to the list.
	 */
	private void addPossible(List<Point> possibilities, Cell[][] cells,
			Point xy, int ox, int oy) {
		final Point t = new Point(xy.x + ox, xy.y + oy);
		if (boardInfo.isOnBoard(t)) {
			final Cell cell = cells[t.x][t.y];
			if (cell.type.isWalkable() && !cell.type.isLethal()) {
				possibilities.add(t);
			}
		}
	}

	/**
	 * Check if we reached the target or if we should change direction now.
	 */
	private boolean shouldChangeDirection(Cell[][] cells, Point ourPosition) {
		if (target == null)
			return true;

		final Point p = boardInfo.pixelToGrid(target);
		final CellType type = cells[p.x][p.y].type;
		if (!type.isWalkable() || type.isLethal() || prediction[p.x][p.y] > 0) {
			return true;
		}
		return BoardUtilities.isClose(target, ourPosition, FUZZINESS);
	}

	/**
	 * Determine this player in the player list.
	 */
	private IPlayerSprite identifyMyself(List<? extends IPlayerSprite> players) {
		for (IPlayerSprite ps : players) {
			if (name.equals(ps.getName())) {
				return ps;
			}
		}
		throw new RuntimeException("Player not on the list of players: " + name);
	}
}
