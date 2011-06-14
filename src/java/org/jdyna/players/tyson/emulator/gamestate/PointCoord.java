package org.jdyna.players.tyson.emulator.gamestate;

import java.awt.Point;

/**
 * Coordinates (points) of cell on the board's grid.
 * 
 * @author Bartosz Wesołowski
 */
@SuppressWarnings("serial")
public class PointCoord extends java.awt.Point
{

    public PointCoord(final int x, final int y)
    {
        super(x, y);
    }

    public PointCoord(final Point point)
    {
        super(point);
    }

}
