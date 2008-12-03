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

        final EnumMap<CellType, CellInfo> cells = Maps.newEnumMap(CellType.class);

        cells.put(CellType.CELL_EMPTY, new CellInfo(tb.tile(0, 0)));
        cells.put(CellType.CELL_WALL, new CellInfo(tb.tile(1, 0)));

        cells.put(CellType.CELL_CRATE, new CellInfo(tb.tile(2, 0)));
        cells.put(CellType.CELL_CRATE_OUT, new CellInfo(tb.tile(new int [][] {
            {3, 0}, {3, 0}, {4, 0}, {5, 0}, {6, 0}, {7, 0}, {8, 0}}), 2));

        cells.put(CellType.CELL_BOMB, new CellInfo(tb.tile(new int [][] {
            {10, 0}, {9, 0}, {11, 0}, {9, 0}}), 4));

        final int boomAdvance = 2;
        cells.put(CellType.CELL_BOOM_TY, new CellInfo(tb.tile(new int [][] {
            {3, 1}, {2, 1}, {1, 1}, {0, 1}, {1, 1}, {2, 1}, {3, 1}}), boomAdvance));
        cells.put(CellType.CELL_BOOM_RX, new CellInfo(tb.tile(new int [][] {
            {7, 1}, {6, 1}, {5, 1}, {4, 1}, {5, 1}, {6, 1}, {7, 1}}), boomAdvance));
        cells.put(CellType.CELL_BOOM_BY, new CellInfo(tb.tile(new int [][] {
            {11, 1}, {10, 1}, {9, 1}, {8, 1}, {9, 1}, {10, 1}, {11, 1}}), boomAdvance));
        cells.put(CellType.CELL_BOOM_LX, new CellInfo(tb.tile(new int [][] {
            {15, 1}, {14, 1}, {13, 1}, {12, 1}, {13, 1}, {14, 1}, {15, 1}}), boomAdvance));
        cells.put(CellType.CELL_BOOM_Y, new CellInfo(tb.tile(new int [][] {
            {19, 1}, {18, 1}, {17, 1}, {16, 1}, {17, 1}, {18, 1}, {19, 1}}), boomAdvance));
        cells.put(CellType.CELL_BOOM_X, new CellInfo(tb.tile(new int [][] {
            {3, 2}, {2, 2}, {1, 2}, {0, 2}, {1, 2}, {2, 2}, {3, 2}}), boomAdvance));

        cells.put(CellType.CELL_BOOM_XY, new CellInfo(tb.tile(new int [][] {
            {8, 2}, {6, 2}, {5, 2}, {4, 2}, {5, 2}, {6, 2}, {7, 2}}), boomAdvance));

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