package com.dawidweiss.dyna;

import java.awt.Point;
import java.io.*;
import java.util.*;

import com.google.common.collect.Lists;

/**
 * A board represents the state of cells on the playfield. This state is both static and
 * dynamic in the sense that cells are statically positioned on the grid, but their values
 * may change (i.e., when a bomb is placed on the board or when a crate is destroyed
 * during an explosion).
 * <p>
 * The board additionally includes information about {@link ISprite} objects (overlays
 * over the cell area).
 */
final class Board
{
    /** Board's width in cells. */
    public final int width;

    /** Board's height in cells. */
    public final int height;

    /**
     * The grid of board cells.
     */
    public final Cell [][] cells;

    /**
     * Default player positions on the board.
     */
    public final Point [] defaultPlayerPositions;

    /**
     * A list of sprites.
     */
    public final List<ISprite> sprites = Lists.newArrayList();

    /*
     * 
     */
    public Board(int width, int height, Cell [][] cells, Point [] playerPositions)
    {
        assert width > 0 && height > 0 && cells.length == (width * height);

        this.width = width;
        this.height = height;
        this.cells = cells;
        this.defaultPlayerPositions = playerPositions;
    }

    /**
     * Same as <code>{@link #cells}[p.x][p.y]</code>.
     */
    public Cell cellAt(Point p)
    {
        return cells[p.x][p.y];
    }
}