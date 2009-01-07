package com.dawidweiss.dyna;

/**
 * A single cell in the board's grid.
 */
public class Cell
{
    /**
     * Cell type constant.
     */
    public final CellType type;

    /**
     * A counter associated with each cell. This controls, among other things, animation
     * sequences.
     */
    public int counter;

    /*
     * Only create instances from within the package.
     */
    protected Cell(CellType type)
    {
        this.type = type;
    }
    
    /**
     * Create and return an instance of a cell of given type.
     */
    public final static Cell getInstance(CellType type)
    {
        if (type.isExplosion())
        {
            return new ExplosionCell(type);
        }
        else if (type == CellType.CELL_BOMB)
        {
            return new BombCell();
        }
        else 
        {
            return new Cell(type);
        }
    }
    
    @Override
    public String toString()
    {
        return Character.toString(type.code);
    }
}
