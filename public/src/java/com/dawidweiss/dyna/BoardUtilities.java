package com.dawidweiss.dyna;

import java.awt.Point;
import java.util.*;

import com.google.common.collect.Maps;

/**
 * Static utilities related to management of {@link Cell}s on a {@link Board}. These
 * could be part of the {@link Board} class, but they take much space and are logically
 * together, so they are refactored into a separate class. 
 */
final class BoardUtilities
{
    private BoardUtilities()
    {
        // no instances.
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
     * 
     * @param bombs A list of bombs that exploded during this call.
     * @param crates A list of crate positions that should be removed as part of this explosion. 
     */
    static void explode(Board board, List<BombCell> bombs, List<Point> crates, 
        int x, int y, int range)
    {
        // Horizontal mode.
        final int xmin = Math.max(0, x - range);
        final int xmax = Math.min(board.width - 1, x + range);
        final int ymin = Math.max(0, y - range);
        final int ymax = Math.min(board.height - 1, y + range);

        // Push the bomb on the list of exploded bombs and mark it as the centerpoint.
        if (board.cells[x][y].type == CellType.CELL_BOMB)
        {
            bombs.add((BombCell) board.cells[x][y]);
        }
        board.cells[x][y] = Cell.getInstance(CellType.CELL_BOOM_XY);

        // Propagate in all directions from the centerpoint.
        explode0(board, bombs, crates, range, x - 1, xmin, -1, x, y, true,  
            CellType.CELL_BOOM_X, CellType.CELL_BOOM_LX);
        explode0(board, bombs, crates, range, x + 1, xmax, +1, x, y, true,  
            CellType.CELL_BOOM_X, CellType.CELL_BOOM_RX);
        explode0(board, bombs, crates, range, y - 1, ymin, -1, x, y, false, 
            CellType.CELL_BOOM_Y, CellType.CELL_BOOM_TY);
        explode0(board, bombs, crates, range, y + 1, ymax, +1, x, y, false, 
            CellType.CELL_BOOM_Y, CellType.CELL_BOOM_BY);
    }

    /**
     * Helper method for {@link #explode(List, int, int, int)}, propagation
     * of the explosion. 
     */
    private static void explode0(
        Board board, List<BombCell> bombs, List<Point> crates,
        int range,
        int from, int to, int step,
        final int x, final int y,
        boolean horizontal, CellType during, CellType last)
    {
        for (int i = from; i != to + step; i += step)
        {
            final int lx = (horizontal ? i : x);
            final int ly = (horizontal ? y : i);

            final Cell cell = board.cells[lx][ly];
            switch (cell.type)
            {
                case CELL_CRATE:
                    crates.add(new Point(lx, ly));
                    return;

                case CELL_WALL:
                    return;

                case CELL_BOMB:
                    if (Globals.DELAYED_BOMB_EXPLOSIONS)
                    {
                        /*
                         * Don't explode bombs immediately, just speed up their explosion.
                         */
                        ((BombCell) cell).fuseCounter = Math.min(5, ((BombCell) cell).fuseCounter);
                        return;
                    }

                    /*
                     * Default Dyna behavior: recursively explode the bomb at lx, ly, but still
                     * fill in the cells that we should fill.
                     */
                    explode(board, bombs, crates, lx, ly, range);
                    break;
            }

            board.cells[lx][ly] =
                overlap(board.cells[lx][ly], Cell.getInstance(((i == to) ? last : during)));        
        }
    }

    /**
     * Overlap explosion images.
     */
    private static Cell overlap(Cell cell, Cell explosion)
    {
        if (!cell.type.isExplosion())
        {
            return explosion;
        }

        if (cell.type == explosion.type)
        {
            return cell;
        }

        /*
         * We don't want to overlap with previous explosions, because it looks odd.
         */
        if (cell.counter > 0)
        {
            return explosion;
        }

        return Cell.getInstance(EXPLOSION_OVERLAPS.get(cell.type).get(explosion.type));
    }

    /**
     * Calculate manhattan distance between two locations. 
     */
    public static int manhattanDistance(Point a, Point b)
    {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    /**
     * Euclidian distance between two points.
     */
    public static double euclidianDistance(Point a, Point b)
    {
        final int x = a.x - b.x;
        final int y = a.y - b.y;
        return Math.sqrt(x * x + y * y);
    }
}
