package com.jdyna.emulator.gamestate.bombs;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Contains bombs and their states. Used in relation with cell's coordinates to describe which bombs can threaten this
 * cell.
 * 
 * @author Michał Kozłowski
 */
final class ZoneSafety {
	private Map<Point, BombState> bombs = new HashMap<Point, BombState>();
	private Set<Point> upToDate = Sets.newHashSet();

	protected void addZoneBomb(final Point p, final BombState bzs) {
		bombs.put(p, bzs);
		upToDate.add(p);
	}

	protected void removeOutOfDate() {
		bombs.keySet().retainAll(upToDate);
		upToDate.clear();
	}

	protected boolean isUltimatelySafe() {
		return bombs.isEmpty();
	}

	protected boolean isUltimatelySafe(final int framesShift) {
		for (BombState bzs : bombs.values()) {
			if (Bombs.bombExists(bzs, framesShift)) {
				return false;
			}
		}
		return true;
	}

	protected boolean isSafe(final int framesShift) {
		for (BombState bzs : bombs.values()) {
			if (!bzs.isSafe(framesShift)) {
				return false;
			}
		}
		return true;
	}

	protected boolean willExploded(final int framesShift) {
		for (BombState bzs : bombs.values()) {
			if (!Bombs.bombExists(bzs, framesShift)) {
				return true;
			}
		}
		return false;
	}
}
