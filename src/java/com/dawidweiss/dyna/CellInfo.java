package com.dawidweiss.dyna;

import java.util.Collection;
import java.util.EnumMap;

/**
 * Mapping of {@link Cell} types to graphic files and their sub-tiles.
 */
public final class CellInfo
{
    /**
     * Each cell's dimensions (width, height) in pixels.
     */
    public final int GRID_SIZE;

    /* */
    private final EnumMap<Cell, TileInfo[]> tiles; 

    CellInfo(EnumMap<Cell, TileInfo[]> tiles, int gridSize)
    {
        this.tiles = tiles;
        this.GRID_SIZE = gridSize;
    }

    /**
     * @return Return the set of {@link Cell}s this mapping has information for.
     */
    public Collection<Cell> getCells()
    {
        return tiles.keySet();
    }

    /**
     * Return tile images for a given cell. 
     */
    public TileInfo [] getTileInfo(Cell c)
    {
        return tiles.get(c);
    }
}
