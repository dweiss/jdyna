package ai.board;

import org.jdyna.CellType;

/**
 * A single cell in the board's grid.
 * 
 * @author Slawek
 */
public class EditableCell implements Cloneable {
	/**
	 * Cell type constant.
	 */
	public CellType type;

	/**
	 * A counter associated with each cell. This controls, among other things,
	 * animation sequences.
	 */
	public int counter;

	public int id;

	public int length;

	/*
	 * Only create instances from within the package.
	 */
	public EditableCell(CellType type) {
		this.type = type;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		EditableCell ed = new EditableCell(this.type);
		return ed;
	}
}
