/**
 * Created on: 2009-01-17
 */
package put.bsr.dyna.player.extcell;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.Globals;

/**
 * 
 * Cell with bomb. Stores bomb range and time to explosion.
 * 
 * @author Piotrek
 * 
 */
public class ExtBombCell extends ExtCell {

	/**
	 * Frames to explosion.
	 */
	public int fuseCounter;
	public final int range;

	protected ExtBombCell(Cell originalCell, int fuseCounter, int range) {
		super(originalCell);
		this.fuseCounter = fuseCounter;
		this.range = range;
	}

	protected ExtBombCell(Cell originalCell, int range) {
		this(originalCell, Globals.DEFAULT_FUSE_FRAMES, range);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ExtBombCell");
		builder.append("[" + fuseCounter + "],[" + range + "]");
		return builder.toString();
	}

}
