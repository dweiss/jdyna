/**
 * Created on: 2009-01-17
 */
package put.bsr.dyna.player.extcell;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import put.bsr.dyna.player.ShadowPlayerInfo;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.Globals;

/**
 * Utility class for managing ExtCells.
 * 
 * @author Piotrek
 */
public class ExtCellUtil {
	/**
	 * Helper class to trace Bombs.
	 * 
	 * @author marcin
	 * 
	 */
	private static class Bomb {
		private final ExtBombCell cell;
		private final int row;
		private final int column;

		public Bomb(int row, int column, ExtBombCell bomb) {
			this.row = row;
			this.column = column;
			cell = bomb;
		}

		public ExtBombCell getCell() {
			return cell;
		}

		public int getRow() {
			return row;
		}

		public int getColumn() {
			return column;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append(cell.toString());
			builder.append("[" + row + "," + column + "]");
			return builder.toString();
		}
	}

	/**
	 * Comparator used to sort in ascending order bomb cells comparing their
	 * time to explode.
	 */
	private static Comparator<Bomb> fussinessComparator = new Comparator<Bomb>() {

		@Override
		public int compare(Bomb o1, Bomb o2) {
			return Integer.valueOf(o1.getCell().fuseCounter).compareTo(
					Integer.valueOf(o2.getCell().fuseCounter));
		}

	};

	/**
	 * Gets extended version of cells array.
	 * 
	 * @param cells
	 *            Original board representation.
	 * @param boardInfo
	 * @return Extended board information.
	 */
	private static ExtCell[][] getExtendedCells(Cell[][] cells,
			BoardInfo boardInfo) {
		ExtCell[][] extendedCells = new ExtCell[cells.length][];
		for (int i = 0; i < cells.length; i++) {
			extendedCells[i] = new ExtCell[cells[i].length];
			for (int j = 0; j < cells[i].length; j++) {
				extendedCells[i][j] = new ExtCell(cells[i][j]);
			}
		}
		return extendedCells;
	}

	/**
	 * Merges two representations of board (past and current). First one
	 * (oldExtCells) is from previous frame and the second one (cells) id from
	 * current frame.
	 * 
	 * @param oldExtCells
	 *            previous frame board
	 * @param cells
	 *            current frames board
	 * @param boardInfo
	 * @return Extended board information after recalculating bomb timers.
	 */
	public static ExtCell[][] mergeCells(ExtCell[][] oldExtCells,
			Cell[][] cells, BoardInfo boardInfo) {
		// if reading first time
		if (oldExtCells == null) {
			return getExtendedCells(cells, boardInfo);
		}
		ExtCell[][] newExtendedCells = new ExtCell[cells.length][];

		List<Bomb> bombs = new ArrayList<Bomb>();
		for (int i = 0; i < cells.length; i++) {
			newExtendedCells[i] = new ExtCell[cells[i].length];
			for (int j = 0; j < cells[i].length; j++) {
				if (oldExtCells[i][j] instanceof ExtBombCell) {
					// there were a bomb
					if (cells[i][j].type == CellType.CELL_BOMB) {
						// and it still exists
						// decrese it counter
						newExtendedCells[i][j] = oldExtCells[i][j];
						((ExtBombCell) newExtendedCells[i][j]).fuseCounter--;
						// add bomb to further process
						bombs.add(new Bomb(i, j,
								(ExtBombCell) newExtendedCells[i][j]));
					} else {
						// last time it was a bomb now it is empty field - after
						// detonation
						newExtendedCells[i][j] = new ExtCell(cells[i][j]);
					}
				} else if (cells[i][j].type == CellType.CELL_BOMB) {
					// the bomb has been just dropped
					ShadowPlayerInfo info = ShadowPlayerInfo
							.getPlayerInfoByPosition(new Point(i, j));
					int range = Globals.DEFAULT_BOMB_RANGE;
					if (info != null) {
						range = info.getBombRange();
					}
					ExtBombCell bomb = new ExtBombCell(cells[i][j], range);

					newExtendedCells[i][j] = bomb;
					bombs.add(new Bomb(i, j, bomb));
				} else {
					// normal cell
					newExtendedCells[i][j] = new ExtCell(cells[i][j]);
				}
			}
		}
		// sort bombs by their fuss counters to start bomb processing
		// from the ones with lower bombCounters
		Collections.sort(bombs, fussinessComparator);
		// mark bomb explosions
		for (Bomb bomb : bombs) {
			markBombCell(bomb, newExtendedCells, boardInfo);
			// DebugUtils.printBoard(newExtendedCells);

		}
//		if (bombs.size() > 0) {
//			DebugUtils.printBoard(newExtendedCells);
//			// NoobPlayerInfo.debug();
//		}
		return newExtendedCells;
	}

	private static void markBombCell(Bomb bomb, ExtCell[][] cells,
			BoardInfo boardInfo) {
		int col = bomb.getColumn();
		int row = bomb.getRow();
		int fuseTime = bomb.getCell().fuseCounter;

		// left
		// if cell is in range bomb range and it exists on board mark it as
		// PreExplosion
		for (int i = col - 1, bombRange = bomb.getCell().range; boardInfo
				.isOnBoard(new Point(row, i))
				&& bombRange > 0; i--, bombRange--) {
			ExtCell currentCell = cells[row][i];
			if (currentCell.type.isWalkable()) {
				// only walkable fields have to be marked as preExplosion
				cells[row][i] = new ExtPreExplosionCell(currentCell, fuseTime);
			} else if (currentCell instanceof ExtBombCell) {
				// if on explosion path there is another bomb synchronize theirs
				// fuss counters
				ExtBombCell bombInRange = (ExtBombCell) currentCell;
				if (bombInRange.fuseCounter > bomb.getCell().fuseCounter) {
					bombInRange.fuseCounter = bomb.getCell().fuseCounter;
				}
			} else {
				// not walkable and no bomb blocks explosion
				break;
			}
		}

		// right
		for (int i = col + 1, bombRange = bomb.getCell().range; boardInfo
				.isOnBoard(new Point(row, i))
				&& bombRange > 0; i++, bombRange--) {
			ExtCell currentCell = cells[row][i];
			if (currentCell.type.isWalkable()) {
				// only walkable fields have to be marked as preExplosion
				cells[row][i] = new ExtPreExplosionCell(currentCell, fuseTime);
			} else if (currentCell instanceof ExtBombCell) {
				// if on explosion path there is another bomb synchronize theirs
				// fuss counters
				ExtBombCell bombInRange = (ExtBombCell) currentCell;
				if (bombInRange.fuseCounter > bomb.getCell().fuseCounter) {
					bombInRange.fuseCounter = bomb.getCell().fuseCounter;
				}
			} else {
				// not walkable no bomb blocks explosion
				break;
			}
		}

		// down
		for (int i = row + 1, bombRange = bomb.getCell().range; boardInfo
				.isOnBoard(new Point(i, col))
				&& bombRange > 0; i++, bombRange--) {
			ExtCell currentCell = cells[i][col];
			if (currentCell.type.isWalkable()) {
				// only walkable fields have to be marked as preExplosion
				cells[i][col] = new ExtPreExplosionCell(currentCell, fuseTime);
			} else if (currentCell instanceof ExtBombCell) {
				// if on explosion path there is another bomb synchronize theirs
				// fuss counters
				ExtBombCell bombInRange = (ExtBombCell) currentCell;
				if (bombInRange.fuseCounter > bomb.getCell().fuseCounter) {
					bombInRange.fuseCounter = bomb.getCell().fuseCounter;
				}
			} else {
				// not walkable no bomb blocks explosion
				break;
			}
		}

		// up
		for (int i = row - 1, bombRange = bomb.getCell().range; boardInfo
				.isOnBoard(new Point(i, col))
				&& bombRange > 0; i--, bombRange--) {
			ExtCell currentCell = cells[i][col];
			// only walkable fields have to be marked as preExplosion
			if (currentCell.type.isWalkable()) {
				cells[i][col] = new ExtPreExplosionCell(currentCell, fuseTime);
			} else if (currentCell instanceof ExtBombCell) {
				// if on explosion path there is another bomb synchronize theirs
				// fuss counters
				ExtBombCell bombInRange = (ExtBombCell) currentCell;
				if (bombInRange.fuseCounter > bomb.getCell().fuseCounter) {
					bombInRange.fuseCounter = bomb.getCell().fuseCounter;
				}
			} else {
				// not walkable no bomb blocks explosion
				break;
			}
		}
	}

}
