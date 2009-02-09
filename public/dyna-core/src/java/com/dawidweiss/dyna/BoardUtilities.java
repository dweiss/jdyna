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
    static void explode(Board board, List<BombCell> bombs, List<Point> crates, int x, int y)
    {
        // Horizontal mode.
        final BombCell c = (BombCell) board.cellAt(x, y);

        final int range = c.range;
        final int xmin = Math.max(0, x - range);
        final int xmax = Math.min(board.width - 1, x + range);
        final int ymin = Math.max(0, y - range);
        final int ymax = Math.min(board.height - 1, y + range);

        // Push the bomb on the list of exploded bombs and mark it as the centerpoint.
        if (c.type == CellType.CELL_BOMB)
        {
            bombs.add(c);
        }
        // Add flame attribution here. 
        board.cellAt(x, y, 
            overlap(c, (ExplosionCell) Cell.getInstance(CellType.CELL_BOOM_XY), c));

        // Propagate in all directions from the centerpoint.
        explode0(board, c, bombs, crates, range, x - 1, xmin, -1, x, y, true,  
            CellType.CELL_BOOM_X, CellType.CELL_BOOM_LX);
        explode0(board, c, bombs, crates, range, x + 1, xmax, +1, x, y, true,  
            CellType.CELL_BOOM_X, CellType.CELL_BOOM_RX);
        explode0(board, c, bombs, crates, range, y - 1, ymin, -1, x, y, false, 
            CellType.CELL_BOOM_Y, CellType.CELL_BOOM_TY);
        explode0(board, c, bombs, crates, range, y + 1, ymax, +1, x, y, false, 
            CellType.CELL_BOOM_Y, CellType.CELL_BOOM_BY);
    }

    /**
     * Helper method for {@link #explode(List, int, int, int)}, propagation
     * of the explosion. 
     */
    private static void explode0(
        Board board, BombCell bomb, List<BombCell> bombs, List<Point> crates,
        int range,
        int from, int to, int step,
        final int x, final int y,
        boolean horizontal, CellType during, CellType last)
    {
        for (int i = from; i != to + step; i += step)
        {
            final int lx = (horizontal ? i : x);
            final int ly = (horizontal ? y : i);

            final Cell cell = board.cellAt(lx, ly);
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
                    explode(board, bombs, crates, lx, ly);
                    break;
            }

            final ExplosionCell explosionCell = (ExplosionCell) 
                Cell.getInstance(((i == to) ? last : during));
            board.cellAt(lx, ly, overlap(board.cellAt(lx, ly), explosionCell, bomb));       
        }
    }

    /**
     * Overlap explosion images, updating flame attribution to whoever was the
     * owner of <code>bomb</code> parameter.
     */
    private static Cell overlap(Cell cell, ExplosionCell explosion, BombCell bomb)
    {
        if (!cell.type.isExplosion())
        {
            explosion.addAttribution(bomb.player);
            return explosion;
        }

        final ExplosionCell cell2 = (ExplosionCell) cell;
        if (cell2.type == explosion.type)
        {
            cell2.mergeAttributions(explosion);
            cell2.addAttribution(bomb.player);
            return cell2;
        }

        /*
         * We don't want to overlap with previous explosions, because it looks odd.
         * Merge attributions, however.
         */
        if (cell2.counter > 0)
        {
            explosion.addAttribution(bomb.player);
            explosion.mergeAttributions(cell2);
            return explosion;
        }

        final ExplosionCell cell3 = (ExplosionCell) Cell.getInstance(
            EXPLOSION_OVERLAPS.get(cell2.type).get(explosion.type));
        cell3.mergeAttributions(cell2, explosion);
        cell3.addAttribution(bomb.player);
        return cell3;
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

    /**
     * Return <code>true</code> if two points are "nearly in the same location". Fuzziness
     * controls the maximum distance between x and y coordinates. Fuzziness of zero means
     * the points must be at identical locations.
     */
    public static boolean isClose(Point a, Point b, int fuzziness)
    {
        return Math.abs(a.x - b.x) <= fuzziness && Math.abs(a.y - b.y) <= fuzziness;
    }
}
