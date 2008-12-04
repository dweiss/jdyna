package com.dawidweiss.dyna;

import java.awt.Point;
import java.util.*;

import com.google.common.collect.Maps;

/**
 * Static utilities related to management of {@link Cell}s on a {@link Board}. These
 * could be part of the {@link Board} class, but they take much space and are logically
 * together, so they are refactored into a separate class. 
 */
public final class BoardUtilities
{
    private BoardUtilities()
    {
        // no instances.
    }

    /**
     * All {@link CellType}s that denote explosions.
     */
    public final static EnumSet<CellType> EXPLOSION_CELLS = EnumSet.of(
        CellType.CELL_BOMB, CellType.CELL_BOOM_BY, CellType.CELL_BOOM_TY,
        CellType.CELL_BOOM_Y, CellType.CELL_BOOM_X, CellType.CELL_BOOM_LX,
        CellType.CELL_BOOM_RX, CellType.CELL_BOOM_XY);

    /**
     * All cells that are animated and should be replaced with {@link CellType#CELL_EMPTY}
     * at the end of the animation sequence.
     */
    public final static EnumSet<CellType> ANIMATING_CELLS;
    static
    {
        ANIMATING_CELLS = EnumSet.copyOf(EXPLOSION_CELLS);
        ANIMATING_CELLS.addAll(EnumSet.of(CellType.CELL_CRATE_OUT));
    }

    /**
     * A grid of resulting {@link CellType}s when two explosions overlap.
     * 
     * @see #overlap(Cell, Cell)
     */
    private final static EnumMap<CellType, EnumMap<CellType, CellType>> EXPLOSION_OVERLAPS;
    static
    {
        final CellType [][] pairs = new CellType [][] {
            {CellType.CELL_BOOM_LX, CellType.CELL_BOOM_RX, CellType.CELL_BOOM_X},
            {CellType.CELL_BOOM_LX, CellType.CELL_BOOM_TY, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_LX, CellType.CELL_BOOM_BY, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_LX, CellType.CELL_BOOM_Y, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_LX, CellType.CELL_BOOM_X, CellType.CELL_BOOM_X},
            {CellType.CELL_BOOM_LX, CellType.CELL_BOOM_XY, CellType.CELL_BOOM_XY},
            
            {CellType.CELL_BOOM_RX, CellType.CELL_BOOM_TY, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_RX, CellType.CELL_BOOM_BY, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_RX, CellType.CELL_BOOM_Y, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_RX, CellType.CELL_BOOM_X, CellType.CELL_BOOM_X},
            {CellType.CELL_BOOM_RX, CellType.CELL_BOOM_XY, CellType.CELL_BOOM_XY},

            {CellType.CELL_BOOM_TY, CellType.CELL_BOOM_BY, CellType.CELL_BOOM_Y},
            {CellType.CELL_BOOM_TY, CellType.CELL_BOOM_Y, CellType.CELL_BOOM_Y},
            {CellType.CELL_BOOM_TY, CellType.CELL_BOOM_X, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_TY, CellType.CELL_BOOM_XY, CellType.CELL_BOOM_XY},
            
            {CellType.CELL_BOOM_BY, CellType.CELL_BOOM_Y, CellType.CELL_BOOM_Y},
            {CellType.CELL_BOOM_BY, CellType.CELL_BOOM_X, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_BY, CellType.CELL_BOOM_XY, CellType.CELL_BOOM_XY},            
            
            {CellType.CELL_BOOM_Y, CellType.CELL_BOOM_X, CellType.CELL_BOOM_XY},
            {CellType.CELL_BOOM_Y, CellType.CELL_BOOM_XY, CellType.CELL_BOOM_XY},            

            {CellType.CELL_BOOM_X, CellType.CELL_BOOM_XY, CellType.CELL_BOOM_XY},            
        };

        EXPLOSION_OVERLAPS = Maps.newEnumMap(CellType.class);
        for (CellType [] t : pairs)
        {
            if (!EXPLOSION_OVERLAPS.containsKey(t[0]))
            {
                final EnumMap<CellType, CellType> sub = Maps.newEnumMap(CellType.class);
                EXPLOSION_OVERLAPS.put(t[0], sub);
            }
            if (!EXPLOSION_OVERLAPS.containsKey(t[1]))
            {
                final EnumMap<CellType, CellType> sub = Maps.newEnumMap(CellType.class);
                EXPLOSION_OVERLAPS.put(t[1], sub);
            }

            EXPLOSION_OVERLAPS.get(t[0]).put(t[1], t[2]);
            EXPLOSION_OVERLAPS.get(t[1]).put(t[0], t[2]);
        }
    }
    
    /**
     * Explode <code>range</code> cells around (x,y), recursively propagating if other
     * bombs are found in the range.
     */
    static void explode(Board board, List<Point> crates, int x, int y, int range)
    {
        // Horizontal mode.
        final int xmin = Math.max(0, x - range);
        final int xmax = Math.min(board.width - 1, x + range);
        final int ymin = Math.max(0, y - range);
        final int ymax = Math.min(board.height - 1, y + range);

        // Mark centerpoint right away.
        board.cells[x][y] = new Cell(CellType.CELL_BOOM_XY);

        // Propagate in all directions from the centerpoint.
        explode0(board, crates, range, x - 1, xmin, -1, x, y, true,  
            CellType.CELL_BOOM_X, CellType.CELL_BOOM_LX);
        explode0(board, crates, range, x + 1, xmax, +1, x, y, true,  
            CellType.CELL_BOOM_X, CellType.CELL_BOOM_RX);
        explode0(board, crates, range, y - 1, ymin, -1, x, y, false, 
            CellType.CELL_BOOM_Y, CellType.CELL_BOOM_TY);
        explode0(board, crates, range, y + 1, ymax, +1, x, y, false, 
            CellType.CELL_BOOM_Y, CellType.CELL_BOOM_BY);
    }

    /**
     * Helper method for {@link #explode(List, int, int, int)}, propagation
     * of the explosion. 
     */
    private static void explode0(
        Board board,
        List<Point> crates, int range,
        int from, int to, int step,
        final int x, final int y,
        boolean horizontal, CellType during, CellType last)
    {
        for (int i = from; i != to + step; i += step)
        {
            final int lx = (horizontal ? i : x);
            final int ly = (horizontal ? y : i);

            switch (board.cells[lx][ly].type)
            {
                case CELL_CRATE:
                    crates.add(new Point(lx, ly));
                    return;

                case CELL_WALL:
                    return;

                case CELL_BOMB:
                    /*
                     * Recursively explode the bomb at lx, ly, but still
                     * fill in the cells that we should fill.
                     */
                    explode(board, crates, lx, ly, range);
                    break;
            }

            board.cells[lx][ly] =
                overlap(board.cells[lx][ly], new Cell(((i == to) ? last : during)));        
        }
    }

    /**
     * Overlap explosion images.
     */
    private static Cell overlap(Cell cell, Cell explosion)
    {
        if (!EXPLOSION_CELLS.contains(cell.type))
        {
            return explosion;
        }

        if (cell.type == explosion.type)
        {
            return cell;
        }

        return new Cell(EXPLOSION_OVERLAPS.get(cell.type).get(explosion.type));
    }
}
