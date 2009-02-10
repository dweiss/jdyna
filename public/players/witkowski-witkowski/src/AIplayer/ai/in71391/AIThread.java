package AIplayer.ai.in71391;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import AIplayer.model.BoardInformationClass;
import AIplayer.model.PlayerInfo;
import AIplayer.model.PureController;
import AIplayer.model.SuperCell;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IPlayerController.Direction;

/**
 * Class used to produce new decision about move direction or put a bomb and
 * sending it to ControllerThread.
 * 
 * @author Lukasz Witkowski
 * 
 */
public class AIThread extends Thread {

	private final static Logger logger = Logger.getLogger("AIThread");

	/** Size of one cell. */
	private final int cellSize = Globals.DEFAULT_CELL_SIZE;

	/** Time of fuses in cell. */
	private final int fuseTime = 15;

	/** path find by my AIagent */
	List<Direction> path;

	/** path find by my AIagent */
	List<Direction> rememberPath;

	/** my position on board */
	private Point myPosition;

	/** range of my bombs */
	private short myRange;

	/** check if I am immortal */
	private boolean myImmortality;

	/** check if I am dead */
	private boolean myDead;

	/** AIagent decision */
	private PureController control = new PureController(false, null);

	/** list of frames not handle yet */
	ArrayList<BoardInformationClass> list = new ArrayList<BoardInformationClass>();

	/** Information to aiThread is passing through this queue */
	private LinkedBlockingQueue<BoardInformationClass> aiGameInfo;

	/** Information from aiThread passing change in controller */
	private LinkedBlockingQueue<PureController> contollerQueue;

	/** Name of bot player */
	private String myName;

	/** Package of private fields */
	Cell[][] board;
	short[][] bombsTime;
	short[][] bombsRange;
	short[][] fusesTime;
	ArrayList<Point> bonusList;
	ArrayList<PlayerInfo> livePlayers;
	PlayerInfo[] enemies;

	/** Variable to stop the thread */
	private volatile boolean threadDone = false;
	
	/** Artificil Intelligence algorithm */
	private AgentAI agentAI = new AgentAI();

	/**
	 * 
	 * @param myName
	 *            name of bot player
	 * @param aiGameInfo
	 *            queue of boards frames passed by PlayerControllerThread
	 * @param contollerQueue
	 *            queue of PureControllers passed to ControlThread.
	 */
	public AIThread(String myName,
			LinkedBlockingQueue<BoardInformationClass> aiGameInfo,
			LinkedBlockingQueue<PureController> contollerQueue) {
		this.aiGameInfo = aiGameInfo;
		this.contollerQueue = contollerQueue;
		this.myName = myName;
	}

	public void done() {
		threadDone = true;
	}

	public void run() {
		try {
			int number = aiGameInfo.drainTo(list);
			if (number == 0) {
				BoardInformationClass state_0 = aiGameInfo.poll(5,
						TimeUnit.SECONDS);
				aiGameInfo.drainTo(list);
				if (state_0 != null)
					list.add(0, state_0);
			}
			while (!threadDone) {
				// not thread safe
				PureController pC = new PureController(false, null);
				if (list.size() > 0) {
					logger.debug("Get move");
					pC = AIAlgorithm();
				} else {
					logger.error("No Info about board received");
					break;
				}

				list.clear();
				// take out form queue
				number = aiGameInfo.drainTo(list);
				if (number == 0) {
					BoardInformationClass state_0 = aiGameInfo.poll(5,
							TimeUnit.SECONDS);
					aiGameInfo.drainTo(list);
					if (state_0 != null)
						list.add(0, state_0);
					else {
						if (!threadDone) logger.info("No board receive!!!");
						break;
					}
				}
				logger.debug("Send frame "
						+ list.get(list.size() - 1).getFrameNumber() + " "
						+ pC.getCurrent());
				contollerQueue.put(pC);
			}
		} catch (InterruptedException e) {
			logger.error("AiThread - interrupt " + e.getMessage());
		}
	}

	private PureController AIAlgorithm() {
		BoardInformationClass lastBoard = list.get(list.size() - 1);

		myRange = 0;
		myPosition = findMyPosition(lastBoard);
		if (myDead)
			return new PureController(false, null);
		if (myPosition != null) {
			Point lastPosition = myPosition;
			myPosition = new Point(myPosition.x, myPosition.y);
			// predict position on next frame
			if (control.getCurrent() != null)
				switch (control.getCurrent()) {
				case UP:
					myPosition.y -= 2;
					break;
				case DOWN:
					myPosition.y += 2;
					break;
				case LEFT:
					myPosition.x -= 2;
					break;
				case RIGHT:
					myPosition.x += 2;
					break;
				}

			int width = lastBoard.cells.size();
			int height = lastBoard.cells.get(0).size();

			logger.debug("AITHREAD "
					+ myPosition.x
					/ cellSize
					+ " "
					+ " "
					+ myPosition.y
					/ cellSize
					+ " "
					+ lastBoard.cells.get(myPosition.x / cellSize).get(
							myPosition.y / cellSize).type + " "
					+ lastBoard.getFrameNumber() + "  " + lastPosition.x + " "
					+ lastPosition.y + " " + myPosition.x + " " + myPosition.y);

			board = new Cell[width][height];
			bombsTime = new short[width][height];
			bombsRange = new short[width][height];
			fusesTime = new short[width][height];
			bonusList = new ArrayList<Point>();
			livePlayers = new ArrayList<PlayerInfo>();
			for (int g = 0; g < lastBoard.playersOnBoard.size(); g++) {
				if (!lastBoard.playersOnBoard.get(g).isDead())
					livePlayers.add(lastBoard.playersOnBoard.get(g));
			}
			enemies = new PlayerInfo[livePlayers.size()];
			enemies = livePlayers.toArray(enemies);

			for (int i = 0; i < width; i++)
				for (int j = 0; j < height; j++) {
					SuperCell sCell = lastBoard.cells.get(i).get(j);
					board[i][j] = Cell.getInstance(sCell.type);
					if (sCell.type.equals(CellType.CELL_BOMB)) {

						if ((short) (sCell.getBombinfo().getTimeToBlow() - lastBoard
								.getFrameNumber()) > 0)
							bombsTime[i][j] = (short) (sCell.getBombinfo()
									.getTimeToBlow() - lastBoard
									.getFrameNumber());
						else {
							bombsTime[i][j] = 1;
							fusesTime[i][j] = (short) (1 + fuseTime
									- lastBoard.getFrameNumber() + sCell
									.getBombinfo().getTimeToBlow());
						}
						bombsRange[i][j] = (short) sCell.getBombinfo()
								.getRange();
					}
					if (sCell.type.equals(CellType.CELL_BONUS_BOMB)
							|| sCell.type.equals(CellType.CELL_BONUS_RANGE)) {
						bonusList.add(new Point(i, j));
					}
					if (sCell.type.equals(CellType.CELL_CRATE_OUT)) {
						fusesTime[i][j] = (short) (1 + fuseTime
								- lastBoard.getFrameNumber() + sCell
								.getBombinfo().getTimeToBlow());
					}
				}
			Point[] bonuses = new Point[bonusList.size()];
			bonuses = bonusList.toArray(bonuses);
			
			agentAI.setData(board, bombsTime, bombsRange,
					fusesTime, enemies, myPosition, myRange, bonuses);
			
			path = agentAI.move();
			if (path == null) {
				if (myImmortality) {
					CellType upCell = lastBoard.cells.get(
							myPosition.x / cellSize).get(
							myPosition.y / cellSize - 1).type;
					CellType downCell = lastBoard.cells.get(
							myPosition.x / cellSize).get(
							myPosition.y / cellSize + 1).type;
					CellType leftCell = lastBoard.cells.get(
							myPosition.x / cellSize - 1).get(
							myPosition.y / cellSize).type;
					CellType rightCell = lastBoard.cells.get(
							myPosition.x / cellSize + 1).get(
							myPosition.y / cellSize).type;
					if (upCell.compareTo(CellType.CELL_WALL) != 0
							&& upCell.compareTo(CellType.CELL_CRATE_OUT) != 0
							&& upCell.compareTo(CellType.CELL_CRATE) != 0) {
						return new PureController(false, Direction.UP);
					}
					if (downCell.compareTo(CellType.CELL_WALL) != 0
							&& downCell.compareTo(CellType.CELL_CRATE_OUT) != 0
							&& downCell.compareTo(CellType.CELL_CRATE) != 0) {
						return new PureController(false, Direction.DOWN);
					}
					if (leftCell.compareTo(CellType.CELL_WALL) != 0
							&& leftCell.compareTo(CellType.CELL_CRATE_OUT) != 0
							&& leftCell.compareTo(CellType.CELL_CRATE) != 0) {
						return new PureController(false, Direction.LEFT);
					}
					if (rightCell.compareTo(CellType.CELL_WALL) != 0
							&& rightCell.compareTo(CellType.CELL_CRATE_OUT) != 0
							&& rightCell.compareTo(CellType.CELL_CRATE) != 0) {
						return new PureController(false, Direction.RIGHT);
					}
				}
			} else {
				if (agentAI.getBomb()) {
					control = new PureController(agentAI.getBomb(), null);
				} else if (path.size() > 0) {
					control = new PureController(false, path.get(0));
					path.remove(0);
				}
			}
			StringBuffer sB = new StringBuffer();
			if (path != null) {
				for (int i = 0; i < path.size(); i++) {
					sB.append(path.get(i));
					sB.append(" ");
				}
			}
			logger.debug("THREAD PATH " + " " + sB.toString());
		}
		return control;
	}

	/**
	 * Find and set my position and range on board.
	 * 
	 * @param board
	 * @return
	 */
	private Point findMyPosition(BoardInformationClass board) {
		Point result = null;
		int myNumber = 0;
		for (int g = 0; g < board.playersOnBoard.size(); g++) {
			if (board.playersOnBoard.get(g).getName().equals(myName)) {
				myNumber = g;
				result = new Point(board.playersOnBoard.get(g).getPosition_x(),
						board.playersOnBoard.get(g).getPosition_y());
				myRange = (short) board.playersOnBoard.get(g).getRange();
				myImmortality = board.playersOnBoard.get(g).isImmortal();
				myDead = board.playersOnBoard.get(g).isDead();
			} else {
				board.playersOnBoard.get(g).setPosition_x(
						board.playersOnBoard.get(g).getPosition_x() / cellSize);
				board.playersOnBoard.get(g).setPosition_y(
						board.playersOnBoard.get(g).getPosition_y() / cellSize);
			}
		}
		board.playersOnBoard.remove(myNumber);
		return result;
	}

}
