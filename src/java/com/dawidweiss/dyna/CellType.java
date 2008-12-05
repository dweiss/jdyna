package com.dawidweiss.dyna;

import java.util.EnumSet;

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
    CELL_BOMB('o'),

    /* Explosions. */
    CELL_BOOM_LX('<'), CELL_BOOM_RX('>'), CELL_BOOM_X('-'), CELL_BOOM_TY('^'), CELL_BOOM_BY(
        'v'), CELL_BOOM_Y('|'), CELL_BOOM_XY('+');

    /**
     * Character code for the cell (16 bits).
     */
    public final char code;

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

    /*
     * 
     */
    private CellType(char code)
    {
        this.code = code;
    }

    /**
     * @return Return an enum instance for a cell's character code.
     * @throws IllegalArgumentException If the code does not exist.
     */
    public static CellType valueOf(char code)
    {
        return cells[code];
    }

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
        LETHAL = EnumSet.of(CellType.CELL_BOOM_BY, CellType.CELL_BOOM_TY,
            CellType.CELL_BOOM_Y, CellType.CELL_BOOM_X, CellType.CELL_BOOM_LX,
            CellType.CELL_BOOM_RX, CellType.CELL_BOOM_XY);
    }

    /**
     * @return Returns <code>true</code> if this cell type is empty (players can walk on it).
     */
    public static boolean isWalkable(CellType type)
    {
        return type == CellType.CELL_EMPTY;
    }
}
