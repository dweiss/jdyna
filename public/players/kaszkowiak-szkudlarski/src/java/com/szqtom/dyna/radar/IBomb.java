package com.szqtom.dyna.radar;


import java.awt.Point;

import com.dawidweiss.dyna.Cell;
public interface IBomb {

	boolean isInDestructionZone(int x, int y, Cell[][] board);

	int frameToExpolosion(int actualFrame);
	
	boolean isInPoint(Point point);
	
	boolean equal(IBomb bomb);
	
	boolean equal(int x, int y);

}
 