package com.dawidweiss.dyna;

/**
 * Additional specification for a single cell on the {@link Board}.
 */
public enum Cell
{
    CELL_EMPTY((byte) ' '),

    CELL_WALL((byte) '#'),

    CELL_CRATE((byte) 'X'),
    CELL_CRATE_OUT((byte) 'x'),

    CELL_BOMB((byte) 'b');

    /**
     * Byte code for the cell.
     */
    public final byte code;

    /**
     * A static mapping between codes and enum constants.
     * 
     * @see #valueOf(byte)
     */
    private final static Cell [] cells;
    static
    {
        cells = new Cell [256];
        for (Cell c : Cell.values())
        {
            cells[c.code] = c;
        }
    }

    /*
     * 
     */
    private Cell(byte code)
    {
        this.code = code;
    }

    /**
     * @return Return an enum instance for a cell's character code.
     * @throws IllegalArgumentException If the code does not exist.
     */
    public static Cell valueOf(byte code)
    {
        return cells[code];
    }
}
