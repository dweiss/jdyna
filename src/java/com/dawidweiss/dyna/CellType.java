package com.dawidweiss.dyna;

import java.util.EnumMap;
import java.util.EnumSet;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * A cell type contains the cell's numeric code. An enum is basically the same as
 * integer-coded constant and efficient to use in sets and maps.
 */
public enum CellType
{
    /* Empty cell. */
    CELL_EMPTY(' '),

    /* Simple wall. */
    CELL_WALL('#'),

    /* Crates (destroyable). */
    CELL_CRATE('X'), CELL_CRATE_OUT('x'),

    /* Bomb. */
    CELL_BOMB('b'),

    /* Explosions. */
    CELL_BOOM_LX('<'), CELL_BOOM_RX('>'), CELL_BOOM_X('-'), CELL_BOOM_TY('^'), CELL_BOOM_BY(
        'v'), CELL_BOOM_Y('|'), CELL_BOOM_XY('+');

    /**
     * Character code for the cell (16 bits).
     */
    public final char code;

    /*
     * 
     */
    private CellType(char code)
    {
        this.code = code;
    }

    /**
     * A static mapping between codes and enum constants. 64k is not a problem with
     * today's architecture, huh? (we don't care about code points).
     * 
     * @see #valueOf(char)
     */
    private final static CellType [] cells;
    static
    {
        cells = new CellType [Character.MAX_VALUE];
        for (CellType c : CellType.values())
        {
            cells[c.code] = c;
        }
    }

    /**
     * @return Return an enum instance for a cell's character code.
     * @throws IllegalArgumentException If the code does not exist.
     */
    public static CellType valueOf(char code)
    {
        final CellType c = cells[code];
        if (c == null)
        {
            throw new RuntimeException("No cell with code: " + (int) code + " ('" + code
                + "')");
        }

        return cells[code];
    }

    /**
     * Is the given type an explosion cell?
     */
    public static boolean isExplosion(CellType type)
    {
        return EXPLOSION_CELLS.contains(type);
    }

    public final static EnumSet<CellType> EXPLOSION_CELLS = EnumSet.of(
        CellType.CELL_BOOM_BY, CellType.CELL_BOOM_TY,
        CellType.CELL_BOOM_Y, CellType.CELL_BOOM_X, CellType.CELL_BOOM_LX,
        CellType.CELL_BOOM_RX, CellType.CELL_BOOM_XY);

    /**
     * @return Returns <code>true</code> if this cell type is lethal.
     */
    public static boolean isLethal(CellType type)
    {
        return LETHAL.contains(type);
    }

    private final static EnumSet<CellType> LETHAL;
    static
    {
        LETHAL = Sets.newEnumSet(EXPLOSION_CELLS, CellType.class);
    }

    /**
     * @return Returns <code>true</code> if this cell type is empty (players can walk on
     *         it).
     */
    public static boolean isWalkable(CellType type)
    {
        return WALKABLES.contains(type);
    }
    
    private final static EnumSet<CellType> WALKABLES;
    static
    {
        WALKABLES = EnumSet.of(CellType.CELL_EMPTY);
        WALKABLES.addAll(EXPLOSION_CELLS);
    }
    

    /**
     * All cells that are animated and should be replaced with {@link CellType#CELL_EMPTY}
     * at the end of the animation sequence.
     */
    public final static EnumMap<CellType, Integer> ANIMATING_CELLS = Maps.newEnumMap(CellType.class);
    static
    {
        /*
         * Number of frames * frameRate. These values should be image-independent,
         * but we hardcode the constants used in classic dyna.
         */
        final int explosionFrameCount = 7 * 2;
        final int crateFrameCount = 7 * 2;

        for (CellType c : EXPLOSION_CELLS)
        {
            ANIMATING_CELLS.put(c, explosionFrameCount);
        }
        ANIMATING_CELLS.put(CellType.CELL_CRATE_OUT, crateFrameCount);
    }    

    /**
     * Return the frame counter after which the cell should be replaced with
     * {@link #CELL_EMPTY}. If zero, the cell should not be removed at all.
     */
    public static int getRemoveAtCounter(CellType type)
    {
        final Integer i = ANIMATING_CELLS.get(type);
        return (i == null ? 0 : i);
    }

    private final static CellType [] BY_ORDINALS;
    static
    {
        int max = 0;
        for (CellType t : CellType.values()) max = Math.max(max, t.ordinal());
        BY_ORDINALS = new CellType [max + 1];
        for (CellType t : CellType.values()) BY_ORDINALS[t.ordinal()] = t;
    }

    /**
     * Convert from enum's ordinal number to enum type.
     */
    public static CellType valueOf(int ordinal)
    {
        return BY_ORDINALS[ordinal];
    }
}
