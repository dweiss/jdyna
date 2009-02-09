package ai.board;

import com.dawidweiss.dyna.CellType;
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
