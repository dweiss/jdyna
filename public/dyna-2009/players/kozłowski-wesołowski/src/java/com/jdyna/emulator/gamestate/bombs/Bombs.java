package com.jdyna.emulator.gamestate.bombs;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jdyna.CellType;
import org.jdyna.Globals;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jdyna.emulator.gamestate.Board;
import com.jdyna.emulator.gamestate.ExtendedPlayer;
import com.jdyna.emulator.gamestate.GridCoord;
import com.jdyna.emulator.gamestate.IPlayersInformationListener;
import com.jdyna.emulator.gamestate.bombs.BombState.BombStatus;

/**
 * <p>
 * Manages information about bombs on board. Remembers state of bombs and time of start and end of their explosions.
 * </p>
 * 
 * @author Michał Kozłowski
 * 
 */
public class Bombs implements IPlayersInformationListener {
	public final static int EXPLOSION_FRAMES = 14;
	public final static int BOMB_LIFETIME = Globals.DEFAULT_FUSE_FRAMES + EXPLOSION_FRAMES;
	protected final Map<GridCoord, BombState> bombs = new HashMap<GridCoord, BombState>();
	private final ZoneSafetyUpdater zoneSafetyUpdater;

	/**
	 * @param board Source of information about board.
	 */
	public Bombs(final Board board) {
		for (int i = 0; i < board.getWidth(); i++) {
			for (int j = 0; j < board.getHeight(); j++) {
				final CellType type = board.cellAt(i, j).getType();
				if (type == CellType.CELL_BOMB) {
					bombs.put(new GridCoord(i, j), new BombState(Globals.DEFAULT_FUSE_FRAMES, BombStatus.READY));
				} else if (type == CellType.CELL_BOOM_XY) {
					bombs.put(new GridCoord(i, j), new BombState(EXPLOSION_FRAMES, BombStatus.EXPLODED));
				}
			}
		}
		zoneSafetyUpdater = new ZoneSafetyUpdater();
	}

	/**
	 * @param grid To check if it has bomb
	 * @param framesShift Describes future in number of frames
	 * @return <code>true</code> if bomb will exist, otherwise <code>false</code>
	 */
	public boolean hasBomb(final GridCoord grid, final int framesShift) {
		final BombState bs = bombs.get(grid);
		if (bs == null) {
			return false;
		} else {
			return bombExists(bs, framesShift);
		}
	}

	/**
	 * @param grid To check its safety.
	 * @param framesShift Describes future in number of frames.
	 * @return <code>true<code> if cell will be safe for player, otherwise <code>false</code>
	 */
	public boolean isSafe(final GridCoord grid, final int framesShift) {
		final BombState b = bombs.get(grid);
		if (b != null && !b.isSafe(framesShift)) {
			return false;
		} else {
			return (zoneSafetyUpdater.isSafe(grid, framesShift));
		}
	}

	/**
	 * @param grid To check its safety.
	 * @return <code>true<code> if cell is ultimately safe (without any bombs in future), otherwise <code>false</code>
	 */
	public boolean isUltimatelySafe(final GridCoord grid) {
		final BombState b = bombs.get(grid);
		if (b != null) {
			return false;
		} else {
			return (zoneSafetyUpdater.isUltimatelySafe(grid));
		}
	}

	/**
	 * @param point To check its safety.
	 * @param framesShift Describes future in number of frames.
	 * @return <code>true<code> if cell will be ultimately safe (without any bombs in future), otherwise <code>false</code>
	 */
	public boolean isUltimatelySafe(final GridCoord grid, final int framesShift) {
		final BombState b = bombs.get(grid);
		if (b != null && bombExists(b, framesShift)) {
			return false;
		} else {
			return (zoneSafetyUpdater.isUltimatelySafe(grid, framesShift));
		}
	}

	/** Updates information about bombs on board. */
	public void updateBombs(final int nrOfFrames, final Board board) {
		for (int i = 0; i < board.getWidth(); i++) {
			for (int j = 0; j < board.getHeight(); j++) {
				final GridCoord cell = new GridCoord(i, j);
				final CellType type = board.cellAt(cell).getType();
				if (type == CellType.CELL_BOMB) {
					updateBombCell(nrOfFrames, cell);
				} else if (type == CellType.CELL_BOOM_XY) {
					updateExplosionCell(nrOfFrames, cell);
				} else {
					bombs.remove(cell);
				}
			}
		}
	}

	/**
	 * Updates information about cells threatened by bombs explosions.
	 * 
	 * @param nrOfFrames Number of frames from last update.
	 * @param board Source of information about board.
	 */
	public void updateZoneSafety(final int nrOfFrames, final Board board) {
		zoneSafetyUpdater.update(nrOfFrames, board, this);
	}

	/**
	 * @see ZoneSafetyUpdater#someBombZoneWillExploded(Point, int)
	 */
	public boolean someBombZoneWillExploded(final GridCoord grid, final int framesShift) {
		return zoneSafetyUpdater.someBombZoneWillExploded(grid, framesShift);
	}

	/**
	 * @return Bombs locations with timers.
	 */
	public Map<GridCoord, Integer> getReadyBombsWithTimers() {
		final Map<GridCoord, Integer> result = Maps.newHashMap();
		for (Entry<GridCoord, BombState> entry : bombs.entrySet()) {
			final BombState v = entry.getValue();
			if (v.getStatus() == BombStatus.READY) {
				result.put(entry.getKey(), v.getTimer());
			}
		}
		return result;
	}

	@Override
	public void update(ExtendedPlayer exPl) {
		// set bomb range
		final BombState bs = bombs.get(exPl.getCell());
		if (bs != null) {
			bs.setRange(exPl.getRange());
		}
	}

	BombState getBombState(final GridCoord grid) {
		return bombs.get(grid);
	}

	Set<GridCoord> getExplosions() {
		final Set<GridCoord> result = Sets.newHashSet();
		for (Entry<GridCoord, BombState> entry : bombs.entrySet()) {
			final BombState v = entry.getValue();
			if (v.getStatus() == BombStatus.EXPLODED) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	boolean isBombReady(final GridCoord grid) {
		final BombState b = bombs.get(grid);
		return (b != null && b.getStatus() == BombStatus.READY);
	}

	boolean updateTimerIfSmaller(final GridCoord grid, final int newTimer) {
		final BombState b = bombs.get(grid);
		if (b != null) {
			if (b.getTimer() > newTimer) {
				b.setTimer(newTimer);
				return true;
			}
		}
		return false;
	}

	static boolean bombExists(final BombState rs, final int framesShift) {
		if (rs.getStatus() == BombStatus.EXPLODED) {
			return rs.getTimer() >= framesShift;
		} else {
			return rs.getTimer() + EXPLOSION_FRAMES >= framesShift;
		}
	}

	private void updateBombCell(final int nrOfFrames, final GridCoord grid) {
		BombState bomb = bombs.get(grid);
		if (bomb == null || bomb.getStatus() != BombStatus.READY) {
			bomb = new BombState(Globals.DEFAULT_FUSE_FRAMES, BombStatus.READY);
			bombs.put(grid, bomb);
		} else {
			bomb.setTimer(bomb.getTimer() - nrOfFrames);
		}
	}

	private void updateExplosionCell(final int nrOfFrames, final GridCoord grid) {
		BombState bomb = bombs.get(grid);
		if (bomb == null) {
			bombs.put(grid, new BombState(EXPLOSION_FRAMES, BombStatus.READY));
		} else if (bomb.getStatus() == BombStatus.READY) {
			bomb.setStatus(BombStatus.EXPLODED);
			bomb.setTimer(EXPLOSION_FRAMES);
		} else {
			bomb.setTimer(bomb.getTimer() - nrOfFrames);
		}
	}
}
