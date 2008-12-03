package com.dawidweiss.dyna;

/**
 * Additional information about a {@link CellType}, its graphical properties for example.
 */
public final class CellInfo
{
    /** */
    public final TileInfo [] tiles;

    /**
     * How many frames it takes to advance to the cell's counter? This is related to
     * animation, but also to things like explosions.
     */
    public final int advanceRate;

    CellInfo(TileInfo [] tiles, int advanceRate)
    {
        this.tiles = tiles;
        this.advanceRate = advanceRate;
    }

    CellInfo(TileInfo [] tiles)
    {
        this(tiles, 1);
    }
}
