package ai.player;

import java.awt.Point;

import com.dawidweiss.dyna.Globals;
/**
 * 
 * @author Asia
 */
public class PlayerInfo {
	
	public int bombRange=Globals.DEFAULT_BOMB_RANGE;
	public int bombCount=Globals.DEFAULT_BOMB_COUNT;
	public int [] position;
	public Point point;
	public int frame;
	public String name;
	
	public PlayerInfo(String name,int frame) {
		this.name=name;
		this.frame=frame;
	}
}
