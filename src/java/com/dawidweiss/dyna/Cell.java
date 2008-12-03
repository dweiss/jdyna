package com.dawidweiss.dyna;

/**
 * A single cell in the board's grid.
 */
public final class Cell
{
    /* */
    public final CellType type;

    /**
     * A counter associated with each cell. This controls, among other things, animation
     * sequences.
     */
    public int counter;

    /*
     * 
     */
    public Cell(CellType type)
    {
        this.type = type;
    }
}
