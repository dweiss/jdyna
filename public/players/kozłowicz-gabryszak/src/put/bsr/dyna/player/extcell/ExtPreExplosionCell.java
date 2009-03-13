/**
 * Created on: 2009-01-17
 */
package put.bsr.dyna.player.extcell;

import org.jdyna.Cell;
import org.jdyna.Globals;

/**
 * Cell representing field that is in bomb range and will be in explosion in
 * some time.
 * 
 * @author Piotrek
 */
public class ExtPreExplosionCell extends ExtCell {

	/**
	 * Frames to explosion.
	 */
	public int fuseCounter;

	/**
	 * Constructor.
	 * 
	 * @param originalCell
	 *            cell to wrap
	 * @param fuseCounter
	 *            frames to explosion
	 */
	protected ExtPreExplosionCell(Cell originalCell, int fuseCounter) {
		super(originalCell);
		this.fuseCounter = fuseCounter;
	}

	/**
	 * Constructor using default fuse time.
	 * 
	 * @param originalCell
	 *            cell to wrap
	 */
	protected ExtPreExplosionCell(Cell originalCell) {
		this(originalCell, Globals.DEFAULT_FUSE_FRAMES);
	}
}
