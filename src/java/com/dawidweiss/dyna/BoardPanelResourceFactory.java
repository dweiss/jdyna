package com.dawidweiss.dyna;

import java.awt.GraphicsConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Static set of {@link CellInfo}s.
 */
public final class BoardPanelResourceFactory
{
    /**
     * Returns {@link BoardPanelResources} with Dyna classic resources.
     */
    public static BoardPanelResources getDynaClassic(GraphicsConfiguration conf)
        throws IOException
    {
        final int GRID_SIZE = 16;
        final TileInfoBuilder tb = new TileInfoBuilder("05.png", GRID_SIZE);

        final EnumMap<Cell, CellInfo> cells = Maps.newEnumMap(Cell.class);

        cells.put(Cell.CELL_EMPTY, new CellInfo(tb.tile(0, 0)));
        cells.put(Cell.CELL_WALL, new CellInfo(tb.tile(1, 0)));

        cells.put(Cell.CELL_CRATE, new CellInfo(tb.tile(2, 0)));
        cells.put(Cell.CELL_CRATE_OUT, new CellInfo(tb.tile(new int [][] {
            {3, 0}, {4, 0}, {5, 0}, {6, 0}, {7, 0}, {8, 0}})));

        cells.put(Cell.CELL_BOMB, new CellInfo(tb.tile(new int [][] {
            {10, 0}, {9, 0}, {11, 0}, {9, 0}}), 4));

        return new BoardPanelResources(conf, cells, GRID_SIZE);
    }

    private BoardPanelResourceFactory()
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