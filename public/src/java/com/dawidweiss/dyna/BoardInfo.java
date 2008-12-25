package com.dawidweiss.dyna;

import java.awt.Dimension;
import java.awt.Point;
import java.io.Serializable;


/**
 * Static board information.
 */
public final class BoardInfo implements Serializable
{
    /**
     * @see GameEvent#serialVersionUID
     */
    private static final long serialVersionUID = 0x200812241355L;    

    /**
     * Board dimensions (grid). 
     */
    public final Dimension gridSize;

    /**
     * X and Y dimensions of a single cell. This field exists only to support 
     * <i>possible</i> future changes of the baseline grid size. Such changes would have
     * to alter the game's controller as well, so they will not be trivial. You can
     * most likely assume it equals {@link Globals#DEFAULT_CELL_SIZE}. 
     */
    public final int cellSize;

    /**
     * Board dimensions (in pixels). This is what the game controller will use for
     * players coordinate system. Views should translate accordingly to their own
     * coordinate systems (usually 1:1). 
     */
    public final Dimension pixelSize;

    /*
     * 
     */
    BoardInfo(Board board)
    {
        this(new Dimension(board.width, board.height), Globals.DEFAULT_CELL_SIZE);
    }

    /**
     * Public board info constructor simply takes a grid size and default cell size.
     * 
     * @see Globals#DEFAULT_CELL_SIZE
     */
    public BoardInfo(Dimension gridSize, int defaultCellSize)
    {
        this.gridSize = gridSize;
        this.cellSize = defaultCellSize;
        this.pixelSize = new Dimension(
            gridSize.width * cellSize,
            gridSize.height * cellSize);        
    }

    /**
     * Convert pixel coordinates to grid cell coordinates.
     */
    public Point pixelToGrid(Point location)
    {
        return new Point(
            location.x / cellSize, 
            location.y / cellSize);
    }

    /**
     * Convert pixel coordinates to grid cell coordinates.
     */
    public Point pixelToGridOffset(Point location)
    {
        return new Point(
            location.x % cellSize, 
            location.y % cellSize);
    }

    /**
     * Convert from grid coordinates to pixel data. The result
     * is the centerpoint of the grid's cell.
     */
    public Point gridToPixel(Point location)
    {
        return new Point(
            location.x * cellSize + cellSize / 2, 
            location.y * cellSize + cellSize / 2);
    }
}
