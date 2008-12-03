package com.dawidweiss.dyna;

/**
 * Additional specification for a single cell on the {@link Board}.
 */
public enum CellType
{
    CELL_EMPTY((byte) ' '),

    CELL_WALL((byte) '#'),

    CELL_CRATE((byte) 'X'),
    CELL_CRATE_OUT((byte) 'x'),

    CELL_BOMB((byte) 'o'), 
    CELL_BOOM_LX((byte) '<'),
    CELL_BOOM_RX((byte) '>'),
    CELL_BOOM_X((byte) '-'),
    CELL_BOOM_TY((byte) '^'),
    CELL_BOOM_BY((byte) 'v'),
    CELL_BOOM_Y((byte) '|'),
    CELL_BOOM_XY((byte) '+');

    /**
     * Byte code for the cell.
     */
    public final byte code;

    /**
     * A static mapping between codes and enum constants.
     * 
     * @see #valueOf(byte)
     */
    private final static CellType [] cells;
    static
    {
        cells = new CellType [256];
        for (CellType c : CellType.values())
        {
            cells[c.code] = c;
        }
    }

    /*
     * 
     */
    private CellType(byte code)
    {
        this.code = code;
    }

    /**
     * @return Return an enum instance for a cell's character code.
     * @throws IllegalArgumentException If the code does not exist.
     */
    public static CellType valueOf(byte code)
    {
        return cells[code];
    }
}
