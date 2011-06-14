package org.jdyna.players.tyson.emulator.gamestate;

import java.awt.Point;

/**
 * Coordinates of cell on the board's grid.
 * 
 * @author Bartosz Wesołowski
 */
@SuppressWarnings("serial")
public final class GridCoord extends Point
{

    public GridCoord(final int x, final int y)
    {
        super(x, y);
    }

    public GridCoord(final Point point)
    {
        super(point);
    }

}
