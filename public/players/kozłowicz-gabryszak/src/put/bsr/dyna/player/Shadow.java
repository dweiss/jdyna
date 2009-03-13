package put.bsr.dyna.player;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.jdyna.*;

import put.bsr.dyna.player.extcell.ExtCell;
import put.bsr.dyna.player.extcell.ExtCellUtil;
import put.bsr.dyna.player.util.ShadowUtils;


/**
 * Shadow is always behind you, you can never run away from it (no matter how
 * hard you try).
 */
public final class Shadow implements IPlayerController, IGameEventListener {

	/**
	 * Player's default name.
	 */
	public static final String DEFAULT_NAME = "ShadowMaster";

	/** This player's name. */
	private String myName;

	/** Target position we're aiming at, in pixels. */
	private Point gridTarget;

	/** Trail history length. */
	private static final int TRAIL_SIZE = 5;

	/**
	 * Short history of recently visited cells (trail). Positions in grid
	 * coordinates.
	 */
	private ArrayList<Point> trail = new ArrayList<Point>(TRAIL_SIZE);

	/**
	 * Cached board info.
	 */
	private BoardInfo boardInfo;

	/**
	 * Player movement direction.
	 */
	private volatile Direction direction;

	/**
	 * Current event - the last that was received.
	 */
	private volatile GameStateEvent currentStateEvent;

	/**
	 * Extended board representation.
	 */
	private ExtCell[][] extCell;

	/*
	 * 
	 */
	/**
	 * Constructor.
	 */
	public Shadow(String name) {
		this.myName = name;
	}

	/**
	 * Whether or not to drop bombs.
	 */
	@Override
	public boolean dropsBomb() {
		return dropIfEnemyInRange();
	}

	private boolean dropIfEnemyInRange() {
		if (currentStateEvent != null) {
			ShadowPlayerInfo info = ShadowPlayerInfo.getInfoByName(myName);

			for (IPlayerSprite sprite : currentStateEvent.getPlayers()) {
				if (!sprite.getName().equals(myName) && !sprite.isImmortal() && !sprite.isDead()) {
					if (inBombRange(info, sprite)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean inBombRange(ShadowPlayerInfo myself, IPlayerSprite enemy) {
		final int range = myself.getBombRange();
		Cell[][] cells = currentStateEvent.getCells();
		Point enemyGridPosition = boardInfo.pixelToGrid(enemy.getPosition());
		Point myGridPosition = myself.getGridPosition();
		if (ShadowUtils.inSraightPath(myGridPosition, enemyGridPosition, range)) {
			// if enemy is in range - we have to check if explosion path isn't
			// blocked
			Direction directionToTarget = ShadowUtils.getDirection(
					myGridPosition, enemyGridPosition);
			if (directionToTarget == null) {
				//on the same cell - KILL HIM!
				return true;
			}
			int x = myGridPosition.x;
			int y = myGridPosition.y;
			for (int i = 0; i < range; i++) {
				switch (directionToTarget) {
				case DOWN:
					y++;
					break;
				case LEFT:
					x--;
					break;
				case RIGHT:
					x++;
					break;
				case UP:
					y--;
					break;
				}
				if (!cells[x][y].type.isWalkable()) {
					return false;
				} else if (enemyGridPosition.x == x && enemyGridPosition.y == y) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets current direction.
	 */
	@Override
	public Direction getCurrent() {
		return direction;
	}

	/**
	 * Create a named shadow player.
	 */
	public static Player createPlayer(String name) {
		return new Player(name, new Shadow(name));
	}

	@Override
	public void onFrame(int frame, List<? extends GameEvent> events) {

		for (GameEvent event : events) {
			if (event.type == GameEvent.Type.GAME_START) {
				processGameStartEvent((GameStartEvent) event);
			}
			if (event.type == GameEvent.Type.GAME_STATE && boardInfo != null) {
				processGameStateEvent((GameStateEvent) event);
			}
		}
	}

	private void processGameStateEvent(GameStateEvent event) {
		currentStateEvent = event;

		// find myself
		final IPlayerSprite myself = ShadowUtils.identifyPlayer(event
				.getPlayers(), myName);

		// read player information
		ShadowPlayerInfo.collectPlayersInfo(event.getPlayers(),
				extCell != null ? extCell : event.getCells(), boardInfo);

		// create extended cells representation
		extCell = ExtCellUtil.mergeCells(extCell, event.getCells(), boardInfo);

		// set target and direction
		if (myself.isDead()) {
			gridTarget = null;
			return;
		}
		setGridTargetAndDirection(myself, event.getPlayers());
	}

	private void setGridTargetAndDirection(IPlayerSprite myself,
			List<? extends IPlayerSprite> players) {
		final Point pixelPosition = myself.getPosition();
		final Point gridPosition = boardInfo.pixelToGrid(pixelPosition);

		Point tmpGridTarget = null;

		IPlayerSprite closestPlayer = ShadowUtils.getClosestPlayer(players,
				pixelPosition, myName, boardInfo);
		tmpGridTarget = boardInfo.pixelToGrid(closestPlayer.getPosition());

		// try to go to the closest player
		List<Point> movePath = new ArrayList<Point>();
		movePath = ShadowUtils.getShortest(boardInfo, extCell, gridPosition,
				tmpGridTarget);

		// if in danger go to the safe point
		if (!ShadowUtils.isInDanger(movePath, gridPosition, extCell)) {
			gridTarget = tmpGridTarget;
		} else {
			tmpGridTarget = ShadowUtils.findClosestSafePoint(boardInfo, extCell,
					gridPosition, false);
			movePath = ShadowUtils.getShortest(boardInfo, extCell,
					gridPosition, tmpGridTarget);
			gridTarget = tmpGridTarget;
		}

		// select direction
		if (!gridPosition.equals(gridTarget) && !movePath.isEmpty()) {
			direction = ShadowUtils.getDirection(gridPosition, movePath.get(0));
		} else {
			if (ShadowUtils.isAligned(pixelPosition, boardInfo)) {
				direction = null;
			}
		}
	}

	private void processGameStartEvent(GameStartEvent event) {
		this.boardInfo = ((GameStartEvent) event).getBoardInfo();
		this.gridTarget = null;
		this.trail.clear();
		this.direction = null;
		System.out.println("Game started");
	}

}
