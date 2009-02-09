package put.bsr.dyna.player.extcell;

import com.dawidweiss.dyna.Cell;

/**
 * 
 * Extended cell used in extended Board representation.
 * @author Piotrek
 *
 */
public class ExtCell extends Cell {

	private Cell originalCell;

	protected ExtCell(Cell originalCell) {
		super(originalCell.type);
		this.originalCell = originalCell;
	}

	public Cell getOriginalCell() {
		return originalCell;
	}
}
