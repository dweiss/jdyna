package AIplayer.ai.in71391;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.jdyna.*;
import org.jdyna.IPlayerController.Direction;

import AIplayer.model.BfsInfo;
import AIplayer.model.CellAccess;
import AIplayer.model.CellDistance;
import AIplayer.model.PlayerInfo;
import AIplayer.model.Vertex;


/**
 * Class responsible for performing BFS through the board and find the best and
 * safe path to to selected targets.
 * 
 * @author Marcin Witkowski
 * 
 */
public class SearchPath {

	private final static Logger logger = Logger.getLogger("AI.Search");

	/* Game board */
	private Cell[][] board;
	/* Game board with times of estimates blowing times */
	private short[][] bombsTime;
	/* Game board with estimates bombs ranges */
	private short[][] bombsRange;
	/* Game board with bonus time because bonus could be blow up in near future */
	private short[][] bonuses;
	/* Game board with times of estimates fire times on cells */
	private short[][] fusesTime;
	/* Game board with times when cell is accessible */
	private CellAccess[][] access;

	int time;

	/* Default cell size */
	private final int cellSize = Globals.DEFAULT_CELL_SIZE;

	// Maximal bomb time on board
	private int maxBombTime = 0;
	// Maximal time to reach every place on the board
	private int bfsTime = 0;
	/* My position coordinates s */
	private Point myPosition;

	/* Approximation of fuses time */
	short fuseTime = 15;

	/* Table mark whether we was present at some cell [][] at some time [] */
	private boolean[][][] present;
	/* Priority queue of vertex during the time when perform BFS */
	private TreeSet<Vertex> available;

	public SearchPath(Cell[][] board) {

		this.board = new Cell[board.length][board[0].length];
		this.bombsTime = new short[board.length][board[0].length];
		this.bombsRange = new short[board.length][board[0].length];
		this.fusesTime = new short[board.length][board[0].length];

		access = new CellAccess[board.length][board[0].length];

		available = new TreeSet<Vertex>();

		maxBombTime = Math.max(75 + 15 + 17, 8 * (board.length + board[0].length));

		present = new boolean[board.length][board[0].length][maxBombTime];

		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				for (int t = 0; t < maxBombTime; t++) {
					present[i][j][t] = false;
				}
			}
		}

	}

	/**
	 * Setting data uses by BFS.
	 */
	public void searchSetData(Cell[][] board, short[][] bombsTime, short[][] bombsRange, short[][] fusesTime,
			Point[] bonus, Point myPosition) {

		this.myPosition = myPosition;

		bfsTime = 0;
		// Set maxBombTime
		for (int i = 0; i < bombsTime.length; i++) {
			for (int j = 0; j < bombsTime[0].length; j++) {
				if (bombsTime[i][j] > bfsTime)
					bfsTime = bombsTime[i][j];
			}
		}
		bfsTime += fuseTime + 17;

		maxBombTime = Math.max(75 + 15 + 17, 8 * (board.length + board[0].length));

		for (int i = 0; i < board.length; i++)
			System.arraycopy(board[i], 0, this.board[i], 0, board[i].length);
		for (int i = 0; i < board.length; i++)
			System.arraycopy(bombsTime[i], 0, this.bombsTime[i], 0, board[i].length);
		for (int i = 0; i < board.length; i++)
			System.arraycopy(bombsRange[i], 0, this.bombsRange[i], 0, board[i].length);
		for (int i = 0; i < board.length; i++)
			System.arraycopy(fusesTime[i], 0, this.fusesTime[i], 0, board[i].length);

		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				access[i][j] = new CellAccess(maxBombTime);
			}
		}

		for (int i = 0; i < bombsTime.length; i++) {
			for (int j = 0; j < bombsTime[0].length; j++) {

				if (!board[i][j].type.isWalkable())
					access[i][j].alwaysProhibitAcces();

				if (fusesTime[i][j] > 0) {
					access[i][j].prohibitAcces(0, fusesTime[i][j]);
				}

				if (board[i][j].type.equals(CellType.CELL_CRATE_OUT)) {
					access[i][j].setAccesibleFrom(fusesTime[i][j]);
				}
			}
		}

		bonuses = new short[board.length][board[0].length];
		// Set bonuses
		for (Point p : bonus) {
			bonuses[p.x][p.y] = Short.MAX_VALUE;
		}

		time = 0;

		process();

		// It is important when I am standing on the bomb.
		this.board[myPosition.x][myPosition.y] = board[myPosition.x][myPosition.y];
	}

	/**
	 * Convert pixel coordinates to grid cell coordinates.
	 */
	private Point pixelToGrid(Point location) {
		return new Point(location.x / cellSize, location.y / cellSize);
	}

	/**
	 * Propagate bomb explosion and set some cell as inaccessible
	 */
	private void bombsExplosion(int x, int y) {

		for (int r = 0; r <= bombsRange[x][y]; r++) {
			if (board[x + r][y].type.compareTo(CellType.CELL_WALL) == 0
					|| board[x + r][y].type.compareTo(CellType.CELL_CRATE_OUT) == 0)
				break;

			if (board[x + r][y].type.compareTo(CellType.CELL_CRATE) == 0) {
				board[x + r][y] = Cell.getInstance(CellType.CELL_EMPTY);
				fusesTime[x + r][y] = fuseTime;
				access[x + r][y].setAccesibleFrom(time + fusesTime[x + r][y] + 1);
				break;
			}

			if (board[x + r][y].type.compareTo(CellType.CELL_BONUS_BOMB) == 0
					|| board[x + r][y].type.compareTo(CellType.CELL_BONUS_RANGE) == 0)
				bonuses[x + r][y] = (short) time;

			if (board[x + r][y].type.compareTo(CellType.CELL_BOMB) == 0) {
				board[x + r][y] = Cell.getInstance(CellType.CELL_EMPTY);
				bombsTime[x + r][y] = 0;
				bombsExplosion(x + r, y);
			}

			if (fusesTime[x][y] == 0)
				fusesTime[x + r][y] = fuseTime;
			else
				fusesTime[x + r][y] = (short) Math.max(fusesTime[x][y], fusesTime[x + r][y]);
			board[x + r][y] = Cell.getInstance(CellType.CELL_EMPTY);
			access[x + r][y].prohibitAcces(time - 1, fusesTime[x + r][y] + 1);

		}
		for (int r = 0; r <= bombsRange[x][y]; r++) {
			if (board[x - r][y].type.compareTo(CellType.CELL_WALL) == 0
					|| board[x - r][y].type.compareTo(CellType.CELL_CRATE_OUT) == 0)
				break;

			if (board[x - r][y].type.compareTo(CellType.CELL_CRATE) == 0) {
				board[x - r][y] = Cell.getInstance(CellType.CELL_EMPTY);
				fusesTime[x - r][y] = fuseTime;
				access[x - r][y].setAccesibleFrom(time + fusesTime[x - r][y] + 1);
				break;
			}
			if (board[x - r][y].type.compareTo(CellType.CELL_BONUS_BOMB) == 0
					|| board[x - r][y].type.compareTo(CellType.CELL_BONUS_RANGE) == 0)
				bonuses[x - r][y] = (short) time;

			if (board[x - r][y].type.compareTo(CellType.CELL_BOMB) == 0) {
				board[x - r][y] = Cell.getInstance(CellType.CELL_EMPTY);
				bombsTime[x - r][y] = 0;
				bombsExplosion(x - r, y);
			}

			if (fusesTime[x][y] == 0)
				fusesTime[x - r][y] = fuseTime;
			else
				fusesTime[x - r][y] = (short) Math.max(fusesTime[x][y], fusesTime[x - r][y]);
			board[x - r][y] = Cell.getInstance(CellType.CELL_EMPTY);
			access[x - r][y].prohibitAcces(time - 1, fusesTime[x - r][y] + 1);
		}

		for (int r = 0; r <= bombsRange[x][y]; r++) {
			if (board[x][y + r].type.compareTo(CellType.CELL_WALL) == 0
					|| board[x][y + r].type.compareTo(CellType.CELL_CRATE_OUT) == 0)
				break;

			if (board[x][y + r].type.compareTo(CellType.CELL_CRATE) == 0) {
				board[x][y + r] = Cell.getInstance(CellType.CELL_EMPTY);
				fusesTime[x][y + r] = fuseTime;
				access[x][y + r].setAccesibleFrom(time + fusesTime[x][y + r] + 1);
				break;
			}
			if (board[x][y + r].type.compareTo(CellType.CELL_BONUS_BOMB) == 0
					|| board[x][y + r].type.compareTo(CellType.CELL_BONUS_RANGE) == 0)
				bonuses[x][y + r] = (short) time;

			if (board[x][y + r].type.compareTo(CellType.CELL_BOMB) == 0) {
				board[x][y + r] = Cell.getInstance(CellType.CELL_EMPTY);
				bombsTime[x][y + r] = 0;
				bombsExplosion(x, y + r);
			}

			if (fusesTime[x][y] == 0)
				fusesTime[x][y + r] = fuseTime;
			else
				fusesTime[x][y + r] = (short) Math.max(fusesTime[x][y], fusesTime[x][y + r]);
			board[x][y + r] = Cell.getInstance(CellType.CELL_EMPTY);
			access[x][y + r].prohibitAcces(time - 1, fusesTime[x][y + r] + 1);
		}
		for (int r = 0; r <= bombsRange[x][y]; r++) {
			if (board[x][y - r].type.compareTo(CellType.CELL_WALL) == 0
					|| board[x][y - r].type.compareTo(CellType.CELL_CRATE_OUT) == 0)
				break;

			if (board[x][y - r].type.compareTo(CellType.CELL_CRATE) == 0) {
				board[x][y - r] = Cell.getInstance(CellType.CELL_EMPTY);
				fusesTime[x][y - r] = fuseTime;
				access[x][y - r].setAccesibleFrom(time + fusesTime[x][y - r] + 1);
				break;
			}
			if (board[x][y - r].type.compareTo(CellType.CELL_BONUS_BOMB) == 0
					|| board[x][y - r].type.compareTo(CellType.CELL_BONUS_RANGE) == 0)
				bonuses[x][y - r] = (short) time;

			if (board[x][y - r].type.compareTo(CellType.CELL_BOMB) == 0) {
				board[x][y - r] = Cell.getInstance(CellType.CELL_EMPTY);
				bombsTime[x][y - r] = 0;
				bombsExplosion(x, y - r);
			}

			if (fusesTime[x][y] == 0)
				fusesTime[x][y - r] = fuseTime;
			else
				fusesTime[x][y - r] = (short) Math.max(fusesTime[x][y], fusesTime[x][y - r]);
			board[x][y - r] = Cell.getInstance(CellType.CELL_EMPTY);
			access[x][y - r].prohibitAcces(time - 1, fusesTime[x][y - r] + 1);
		}
	}

	/**
	 * Search for a bomb that will next explode.
	 */
	private void bombProcess() {
		for (int i = 0; i < bombsTime.length; i++) {
			for (int j = 0; j < bombsTime[0].length; j++) {
				if (bombsTime[i][j] == 1) {
					board[i][j] = Cell.getInstance(CellType.CELL_EMPTY);
					bombsExplosion(i, j);
				}
			}
		}
	}

	/**
	 * Perform simulation of cell states to the time when the last bomb will
	 * explode. It blow up bombs and propagate fuses through the board.
	 */
	private void process() {
		while (time < maxBombTime) {
			// Process Next Frame
			time++;
			bombProcess();

			for (int i = 0; i < bombsTime.length; i++) {
				for (int j = 0; j < bombsTime[0].length; j++) {
					if (bombsTime[i][j] > 0)
						bombsTime[i][j]--;
					if (fusesTime[i][j] == 1) {
						access[i][j].setAccesibleFrom(time + 1);
						board[i][j] = Cell.getInstance(CellType.CELL_EMPTY);
					}
					if (fusesTime[i][j] > 0)
						fusesTime[i][j]--;
				}
			}

		}
	}

	/**
	 * Search for the safe path ( that is one that allow us to avoid to get
	 * killed to the time when all bombs will explode ) or evaluate whether
	 * propose path is safe to the time when all bombs will explode.
	 * 
	 * @return save path
	 */
	public List<Direction> avoid(Point start,Point startPixel, List<Direction> propPath) {

		available.clear();
		available.add(new Vertex(null, startPixel, 1, null));

		time = 0;

		// time to get away 
		int awayTime = Math.max(bfsTime - 17, 16);

		// confirm that our proposition move is safe
		if (propPath != null) {
			boolean propPathAcc = true;
			while (time < awayTime) {
				BFS(propPath, null);
				if (available.size() == 0) {
					propPathAcc = false;
					break;
				}
			}

			for (Vertex vv : available) {
				present[pixelToGrid(vv.getCoordinates()).x][pixelToGrid(vv.getCoordinates()).y][vv.getTime()] = false;
			}

			if (propPathAcc) {
				return propPath;
			} else
				return null;
		}

		
		// check if our position is safe
		boolean notSafe = false;
		for (int i = 0; i < awayTime; i++) {
			if (access[start.x][start.y].isInaccessible(i)) {
				notSafe = true;
				break;
			}
		}

		// stay if it is safe
		if (!notSafe && propPath == null) {
			List<Direction> result = new ArrayList<Direction>();
			result.add(null);
			return result;
		}

		available.clear();
		available.add(new Vertex(null, startPixel, 1, null));

		time = 0;

		while (time < awayTime) {
			BFS(null, null);
			if (available.size() == 0) {
				logger.debug("No place to run!! DEATH!!");
				return null;
			}
		}
		for (Vertex vv : available) {
			present[pixelToGrid(vv.getCoordinates()).x][pixelToGrid(vv.getCoordinates()).y][vv.getTime()] = false;
		}

		// we take this direction that take us farthest possible
		int max = 0;
		Vertex last = null;
		for (Vertex vv : available) {
			if (Math.abs(pixelToGrid(vv.getCoordinates()).x - start.x)
					+ Math.abs(pixelToGrid(vv.getCoordinates()).y - start.y) > max) {
				max = Math.abs(pixelToGrid(vv.getCoordinates()).x - start.x)
						+ Math.abs(pixelToGrid(vv.getCoordinates()).y - start.y);
				last = vv;
			}
		}

		return createPath(last);
	}

	/* BFS variables */
	/* current point coordinates */
	private Point pt;
	/* current cell coordinates */
	private Point ptGrid;
	/* current time */
	private int timeNow;

	/**
	 * Core of the BFS process, check if cell is accessible and add vertex to
	 * vertex queue until reach proper cell
	 * 
	 * @param propPath
	 *            path to analyze ( first move of BFS must be the same as first
	 *            move of the path )
	 * @param destPoint
	 *            point we would like to reach
	 * @return last vertex on vertex list
	 */
	private Vertex BFS(List<Direction> propPath, Point destPoint) {

		// take first one
		Vertex vertex = available.pollFirst();

		pt = new Point(vertex.getCoordinates());
		ptGrid = pixelToGrid(pt);
		timeNow = vertex.getTime();
		time = timeNow;

		present[ptGrid.x][ptGrid.y][timeNow] = false;

		// when we reach destPoint
		if (destPoint != null && ptGrid.equals(destPoint)) {
			return vertex;
		}

		int stepCost = 0;

		// choose first move same as is in propPath
		if (timeNow == 1 && propPath != null && vertex.getLast() == null) {
			stepCost = 1;
			Point move = new Point(vertex.getCoordinates());
			Point moveGrid = pixelToGrid(move);

			if (propPath.get(0) != null)
				switch (propPath.get(0)) {
				case UP:
					moveGrid.y--;
					move = CellDistance.movePointUP(move);
					stepCost = CellDistance.distanceUP(pt);
					break;
				case DOWN:
					moveGrid.y++;
					move = CellDistance.movePointDOWN(move);
					stepCost = CellDistance.distanceDOWN(pt);
					break;
				case LEFT:
					moveGrid.x--;
					move = CellDistance.movePointLEFT(move);
					stepCost = CellDistance.distanceLEFT(pt);
					break;
				case RIGHT:
					moveGrid.x++;
					move = CellDistance.movePointRIGHT(move);
					stepCost = CellDistance.distanceRIGHT(pt);
					break;
				}

			if (((!access[moveGrid.x][moveGrid.y].isInaccessiblePeriod(timeNow + stepCost, 6)) && !access[ptGrid.x][ptGrid.y]
					.isInaccessiblePeriod(timeNow, stepCost))
					|| (board[myPosition.x][myPosition.y].type.compareTo(CellType.CELL_BOMB) == 0
							&& (!access[moveGrid.x][moveGrid.y].isInaccessiblePeriod(timeNow + stepCost, 3)) || propPath
							.get(0) == null)) {
				available.add(new Vertex(vertex, move, timeNow + stepCost, propPath.get(0)));
				present[moveGrid.x][moveGrid.y][timeNow + stepCost] = true;
			}
		} else {
			// if we are staying on a bomb we don't check whether cell we are
			// staying at is accessible
			if (ptGrid.equals(myPosition) && board[myPosition.x][myPosition.y].type.compareTo(CellType.CELL_BOMB) == 0) {
				if (vertex.getLast() != null)
					timeNow++;

				// Up
				Point up = new Point(ptGrid);
				stepCost = CellDistance.distanceUP(pt);
				up.y--;

				if (stepCost > 0 && !access[up.x][up.y].isInaccessiblePeriod(timeNow + stepCost, 6)) {
					if (!present[up.x][up.y][timeNow + stepCost]) {
						available
								.add(new Vertex(vertex, CellDistance.movePointUP(pt), timeNow + stepCost, Direction.UP));
						present[up.x][up.y][timeNow + stepCost] = true;
					}
				}

				// Right
				Point right = new Point(ptGrid);
				right.x++;
				stepCost = CellDistance.distanceRIGHT(pt);
				if (stepCost > 0 && !access[right.x][right.y].isInaccessiblePeriod(timeNow + stepCost, 6)) {
					if (!present[right.x][right.y][timeNow + stepCost]) {
						available.add(new Vertex(vertex, CellDistance.movePointRIGHT(pt), timeNow + stepCost,
								Direction.RIGHT));
						present[right.x][right.y][timeNow + stepCost] = true;
					}
				}

				// Down
				Point down = new Point(ptGrid);
				stepCost = CellDistance.distanceDOWN(pt);
				down.y++;
				if (stepCost > 0 && !access[down.x][down.y].isInaccessiblePeriod(timeNow + stepCost, 6)) {
					if (!present[down.x][down.y][timeNow + stepCost]) {
						available.add(new Vertex(vertex, CellDistance.movePointDOWN(pt), timeNow + stepCost,
								Direction.DOWN));
						present[down.x][down.y][timeNow + stepCost] = true;
					}
				}

				// Left
				Point left = new Point(ptGrid);
				stepCost = CellDistance.distanceLEFT(pt);
				left.x--;
				if (stepCost > 0 && !access[left.x][left.y].isInaccessiblePeriod(timeNow + stepCost, 6)) {
					if (!present[left.x][left.y][timeNow + stepCost]) {
						available.add(new Vertex(vertex, CellDistance.movePointLEFT(pt), timeNow + stepCost,
								Direction.LEFT));
						present[left.x][left.y][timeNow + stepCost] = true;
					}
				}

				// Stay
				Point stay = new Point(ptGrid);
				stepCost = 1;
				if (!access[stay.x][stay.y].isInaccessiblePeriod(timeNow + stepCost, 3)) {
					if (!present[stay.x][stay.y][timeNow + stepCost]) {
						available.add(new Vertex(vertex, pt, timeNow + stepCost, null));
						present[stay.x][stay.y][timeNow + stepCost] = true;
					}
				}
			} else {
				// This code is repetition of the upper one in main part but it
				// is more easily to test new variants of tactics

				// Up
				Point up = new Point(ptGrid);
				stepCost = CellDistance.distanceUP(pt);
				up.y--;

				if (stepCost > 0 && !access[ptGrid.x][ptGrid.y].isInaccessiblePeriod(timeNow, stepCost)
						&& !access[up.x][up.y].isInaccessiblePeriod(timeNow + stepCost, 6)) {
					if (!present[up.x][up.y][timeNow + stepCost]) {
						available
								.add(new Vertex(vertex, CellDistance.movePointUP(pt), timeNow + stepCost, Direction.UP));
						present[up.x][up.y][timeNow + stepCost] = true;
					}
				}

				// Right
				Point right = new Point(ptGrid);
				right.x++;
				stepCost = CellDistance.distanceRIGHT(pt);
				if (stepCost > 0 && !access[ptGrid.x][ptGrid.y].isInaccessiblePeriod(timeNow, stepCost)
						&& !access[right.x][right.y].isInaccessiblePeriod(timeNow + stepCost, 6)) {
					if (!present[right.x][right.y][timeNow + stepCost]) {
						available.add(new Vertex(vertex, CellDistance.movePointRIGHT(pt), timeNow + stepCost,
								Direction.RIGHT));
						present[right.x][right.y][timeNow + stepCost] = true;
					}
				}

				// Down
				Point down = new Point(ptGrid);
				stepCost = CellDistance.distanceDOWN(pt);
				down.y++;
				if (stepCost > 0 && !access[ptGrid.x][ptGrid.y].isInaccessiblePeriod(timeNow, stepCost)
						&& !access[down.x][down.y].isInaccessiblePeriod(timeNow + stepCost, 6)) {
					if (!present[down.x][down.y][timeNow + stepCost]) {
						available.add(new Vertex(vertex, CellDistance.movePointDOWN(pt), timeNow + stepCost,
								Direction.DOWN));
						present[down.x][down.y][timeNow + stepCost] = true;
					}
				}

				// Left
				Point left = new Point(ptGrid);
				stepCost = CellDistance.distanceLEFT(pt);
				left.x--;
				if (stepCost > 0 && !access[ptGrid.x][ptGrid.y].isInaccessiblePeriod(timeNow, stepCost)
						&& !access[left.x][left.y].isInaccessiblePeriod(timeNow + stepCost, 6)) {
					if (!present[left.x][left.y][timeNow + stepCost]) {
						available.add(new Vertex(vertex, CellDistance.movePointLEFT(pt), timeNow + stepCost,
								Direction.LEFT));
						present[left.x][left.y][timeNow + stepCost] = true;
					}
				}

				// Stay
				Point stay = new Point(ptGrid);
				stepCost = 1;
				if (!access[stay.x][stay.y].isInaccessiblePeriod(timeNow + stepCost, 3)) {
					if (!present[stay.x][stay.y][timeNow + stepCost]) {
						available.add(new Vertex(vertex, pt, timeNow + stepCost, null));
						present[stay.x][stay.y][timeNow + stepCost] = true;
					}
				}
			}

		}
		return null;
	}

	/**
	 * Perform BFS search looking for the closest bonus, crate or enemy
	 * 
	 * @param startPixel
	 *            start point
	 * @param bfsInfo
	 *            store information about BFS searching
	 * @param enemies
	 *            list of enemies positions
	 */
	public void searchBFS(Point startPixel, BfsInfo bfsInfo, PlayerInfo[] enemies) {

		available.clear();
		available.add(new Vertex(null, new Point(startPixel.x, startPixel.y), 1, null));

		time = 0;
		// Time when find first object
		int min = Integer.MAX_VALUE;

		Set<Point> enemiesPos = new HashSet<Point>();
		for (PlayerInfo pl : enemies) {
			enemiesPos.add(new Point(pl.getPosition_x(), pl.getPosition_y()));
		}
		
		int searchTime = Math.min(maxBombTime - 16, 200);
		while ((bfsInfo.isBonus() || bfsInfo.isBrick() || bfsInfo.isEnemy()) && time < searchTime) {
			BFS(null, null);
			if (available.size() == 0)
				return;
			Vertex v = available.first();
			Point pt = pixelToGrid(v.getCoordinates());
			// find bonus
			if (bonuses[pt.x][pt.y] > time && bfsInfo.isBonus()) {
				bfsInfo.setBonusPath(createPath(v));
				bfsInfo.setBonus(false);
				bfsInfo.setEnemy(false);
				bfsInfo.setBrick(false);
				if (time < min)
					min = time;
			}
			// find crate
			if ((board[pt.x - 1][pt.y].type.compareTo(CellType.CELL_CRATE) == 0
					|| board[pt.x + 1][pt.y].type.compareTo(CellType.CELL_CRATE) == 0
					|| board[pt.x][pt.y - 1].type.compareTo(CellType.CELL_CRATE) == 0 || board[pt.x][pt.y + 1].type
					.compareTo(CellType.CELL_CRATE) == 0)
					&& bfsInfo.isBrick()) {
				bfsInfo.setBrickPath(createPath(v));
				bfsInfo.setBrick(false);
				if (time < min)
					min = time;
			}
			// find enemy
			if (enemiesPos.contains(pt) && bfsInfo.isEnemy()) {
				bfsInfo.setEnemyPath(createPath(v));
				bfsInfo.setEnemy(false);
				bfsInfo.setBonus(false);
				bfsInfo.setBrick(false);
				if (time < min)
					min = time;
			}
		}

		// if time > bfsTime you musn't check avoid
		if (min >= bfsTime - 16)
			bfsInfo.setCheckAvoid(true);

		for (Vertex vv : available) {
			present[pixelToGrid(vv.getCoordinates()).x][pixelToGrid(vv.getCoordinates()).y][vv.getTime()] = false;
		}

		return;
	}

	/**
	 * Create path to the target using linked list of Vertex
	 * 
	 * @param last
	 *            last vertex on the path
	 * @return path from BFS start point to the selected target
	 */
	private List<Direction> createPath(Vertex last) {
		if (last != null) {
			// Read the path
			List<Direction> path = new ArrayList<Direction>();
			Vertex vt = null;

			while (true) {
				vt = last.getLast();
				if (vt == null)
					break;
				else {
					path.add(last.getMoveDirection());
				}
				last = last.getLast();
			}
			Collections.reverse(path);
			return path;
		} else
			return null;

	}

}
