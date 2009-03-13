package pl.khroolick.dyna;

import java.awt.Point;

import org.jdyna.Globals;


public class PlayerInfo {
	public int bombs = Globals.DEFAULT_BOMB_COUNT;
	public int range = Globals.DEFAULT_BOMB_RANGE;
	public Point boardPoss;
	public Point pixelPoss;
    public boolean isImmortal;
	public boolean isDead;
	
}
