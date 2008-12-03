package com.dawidweiss.dyna;

import java.util.ArrayList;
import java.util.EnumMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Static set of {@link CellInfo}s.
 */
public final class CellInfoFactory
{
    /**
     * Classic Dyna Blaster image set.
     */
    public static final CellInfo DYNA_CLASSIC;

    static
    {
        final int GRID_SIZE = 16;
        final EnumMap<Cell, TileInfo[]> cells = Maps.newEnumMap(Cell.class);
        final TileInfoBuilder tb = new TileInfoBuilder("05.png", GRID_SIZE);
        cells.put(Cell.CELL_EMPTY, tb.tile(0, 0));
        cells.put(Cell.CELL_WALL, tb.tile(1, 0));
        cells.put(Cell.CELL_CRATE, tb.tile(2, 0));
        cells.put(Cell.CELL_CRATE_OUT, tb.tile(new int [][] {
            {3, 0}, {4, 0}, {5, 0}, {6, 0}, {7, 0}, {8, 0}}));
        DYNA_CLASSIC = new CellInfo(cells, GRID_SIZE);
    }
    
    private CellInfoFactory()
    {
        // no instances.
    }
}

/**
 * Helper class for building {@link TileInfo}s.
 */
final class TileInfoBuilder
{
    private final String imageName;
    private final int gridSize;

    public TileInfoBuilder(String imageName, int gridSize)
    {
        this.imageName = imageName;
        this.gridSize = gridSize;
    }

    public TileInfo [] tile(int x, int y)
    {
        return tile(new int [][] {{x,y}});
    }

    public TileInfo [] tile(int []... frames)
    {
        final ArrayList<TileInfo> tiles = Lists.newArrayList();
        for (int [] frame : frames)
        {
            assert frame.length == 2;
            tiles.add(new TileInfo(imageName, 
                frame[0] * gridSize, frame[1] * gridSize, gridSize, gridSize));
        }

        return tiles.toArray(new TileInfo [tiles.size()]);
    }
}