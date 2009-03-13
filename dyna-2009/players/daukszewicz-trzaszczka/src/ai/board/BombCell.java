package ai.board;

import org.jdyna.CellType;
/**
 * 
 * @author Slawek
 */
public class BombCell extends EditableCell {
	
	public int explosionFrame;
	public int range;
	public int x;
	public int y;
	public boolean lazy = false;
	
	public BombCell(CellType type) {
		super(type);
	}
}
