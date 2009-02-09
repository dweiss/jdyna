package com.jdyna.pathfinder;

import com.dawidweiss.dyna.IPlayerController.Direction;
import com.jdyna.emulator.gamestate.PointCoord;

/**
 * Class representing a neighboring point, which is a point next to some other point we came from.
 * 
 * @author Bartosz Wesołowski
 */
@SuppressWarnings("serial")
final class Neighbor extends PointCoord {
	/** Direction we took to get here. */
	public final Direction direction;
	/** Number of frames it took to get here. */
	public final int frames;
	/** Is this point in another cell than his parent? */
	public final boolean exitsCell;

	public Neighbor(final PointCoord point, final Direction direction, final int frames, final boolean exitsCell) {
		super(point);
		this.direction = direction;
		this.frames = frames;
		this.exitsCell = exitsCell;
	}
}
