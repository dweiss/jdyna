package com.dawidweiss.dyna;

/**
 * Additional information about a {@link CellType}.
 */
final class CellInfo
{
    /** */
    public final TileInfo [] tiles;

    /**
     * How many frames it takes to advance the cell's counter?
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
