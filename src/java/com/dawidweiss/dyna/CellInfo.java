package com.dawidweiss.dyna;


/**
 * Mapping of {@link Cell} types to graphic files and their sub-tiles.
 */
public final class CellInfo
{
    /** */
    public final TileInfo [] tiles;

    /** How many frames it takes to advance to the next tile? */
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
