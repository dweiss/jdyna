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
public final class BoardDataFactory
{
    /**
     * Helper class for building {@link TileInfo}s.
     */
    private final static class TileInfoBuilder
    {
        private final String imageName;
        private final int gridSize;
        public int w;
        public int h;

        public TileInfoBuilder(String imageName, int gridSize)
        {
            this.imageName = imageName;
            this.gridSize = gridSize;
            this.w = gridSize;
            this.h = gridSize;
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
                    frame[0] * gridSize, frame[1] * gridSize, w, h));
            }

            return tiles.toArray(new TileInfo [tiles.size()]);
        }
    }
    
    private BoardDataFactory()
    {
        // no instances.
    }    

    /**
     * Returns {@link BoardData} with Dyna classic resources.
     */
    public static BoardData getDynaClassic(GraphicsConfiguration conf)
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

        return new BoardData(conf, cells, GRID_SIZE, getDynaClassicPlayerData(conf));
    }

    /*
     * 
     */
    @SuppressWarnings("unchecked")
    private static EnumMap<Player.State, TileInfo []> [] getDynaClassicPlayerData(
        GraphicsConfiguration conf)
        throws IOException
    {
        final EnumMap<Player.State, TileInfo []> [] playerData = new EnumMap [] {
            getDynaClassicPlayerA(conf)
        };
        return playerData;
    }

    /*
     * 
     */
    private static EnumMap<Player.State, TileInfo []> getDynaClassicPlayerA(GraphicsConfiguration conf)
        throws IOException
    {
        final int GRID_SIZE = 24;
        final TileInfoBuilder tb = new TileInfoBuilder("02.png", GRID_SIZE);
        tb.w = GRID_SIZE - 1;
        tb.h = GRID_SIZE - 1;

        final EnumMap<Player.State, TileInfo[]> result = Maps.newEnumMap(Player.State.class);
        result.put(Player.State.DOWN, tb.tile(new int [][] {
            {0, 0}, {1, 0}, {2, 0}
        }));
        result.put(Player.State.UP, tb.tile(new int [][] {
            {9, 0}, {10, 0}, {11, 0}
        }));
        result.put(Player.State.LEFT, tb.tile(new int [][] {
            {6, 0}, {7, 0}, {8, 0}
        }));
        result.put(Player.State.RIGHT, tb.tile(new int [][] {
            {3, 0}, {4, 0}, {5, 0}
        }));
        result.put(Player.State.DEAD, tb.tile(new int [][] {
            {12, 0}, {0, 1}, {1, 1}, {2, 1}, {3, 1},
            {4, 1}, {5, 1}, {6, 1}
        }));

        return result;
    }
}