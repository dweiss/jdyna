package com.dawidweiss.dyna.view;

import java.awt.Dimension;
import java.awt.Point;

/**
 * Static board information.
 */
public final class BoardInfo
{
    /**
     * Board dimensions (grid). 
     */
    public final Dimension gridSize;

    /**
     * X and Y dimensions of a single cell.
     */
    public final int cellSize;

    /**
     * Board dimensions (pixels).
     */
    public final Dimension pixelSize;

    /*
     * 
     */
    public BoardInfo(Dimension gridSize, int cellSize)
    {
        this.gridSize = gridSize;
        this.cellSize = cellSize;
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
