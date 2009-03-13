/**
 * 
 */
package put.bsr.dyna.player.util;

import put.bsr.dyna.player.extcell.ExtCell;
import put.bsr.dyna.player.extcell.ExtPreExplosionCell;

/**
 * Utility class used during development to print board on console.
 * 
 * @author marcin 
 */
public class DebugUtils {

	static String decodeCell(ExtCell cell) {
		if (cell instanceof ExtPreExplosionCell) {
			ExtPreExplosionCell extCell = (ExtPreExplosionCell) cell;
			if (extCell.fuseCounter < 10) {
				return  "0"+Integer.toString(extCell.fuseCounter);
			}
			return Integer.toString(extCell.fuseCounter);
		}
		switch (cell.type) {
		case CELL_BOMB:
			return "oB";
		case CELL_BONUS_BOMB:
			return "BB";
		case CELL_EMPTY:
			return "  ";
		case CELL_WALL:
			return "##";
		case CELL_BONUS_RANGE:
			return "BR";
		case CELL_CRATE:
			return "XX";
		}
		return null;
	}
	
	/**
	 * Print board.
	 * 
	 * @param cells
	 */
	static public void printBoard(ExtCell[][] cells) {
		
		int ROW_LENGTH = cells[0].length;
		for (int i = 0 ; i < ROW_LENGTH ; i++) {
			for (int j = 0 ; j < cells.length ; j++) {
				System.out.print(decodeCell(cells[j][i]));
			}
			System.out.println();
		}
	}

}
