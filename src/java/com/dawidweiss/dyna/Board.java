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
public final class Board
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
     * Read one or more board specification from a plain text file. Look at example
     * specifications, they should be self-explanatory and intuitive.
     */
    public static List<Board> readBoards(Reader reader) throws IOException
    {
        final BufferedReader wrapped = new BufferedReader(reader);

        try
        {
            final ArrayList<Point> playerPositions = Lists.newArrayList();
            final ArrayList<Board> boards = Lists.newArrayList();
            final ArrayList<String> stack = Lists.newArrayList();
            String line;
            while ((line = wrapped.readLine()) != null)
            {
                // Trim right whitespace.
                line = line.replaceAll("\\s+$", "");

                if (line.isEmpty() || line.startsWith("--") || "board".equals(line)) continue;

                if ("end".equals(line))
                {
                    int height = stack.size();
                    int width = 0;
                    for (String s : stack)
                    {
                        width = Math.max(width, s.length());
                    }

                    final Cell [][] cells = new Cell [width] [];
                    for (int col = 0; col < width; col++)
                    {
                        cells[col] = new Cell [height];
                    }

                    for (int row = 0; row < stack.size(); row++)
                    {
                        for (int col = 0; col < width; col++)
                        {
                            if (col >= stack.get(row).length())
                            {
                                cells[col][row] = new Cell(CellType.CELL_EMPTY);
                            }
                            else
                            {
                                byte code = (byte) stack.get(row).charAt(col);
                                if (code > '0' && code < '9')
                                {
                                    playerPositions.add(new Point(col, row));
                                    code = ' ';
                                }

                                cells[col][row] = new Cell(CellType.valueOf(code));
                            }
                        }
                    }

                    boards.add(new Board(width, height, cells, playerPositions
                        .toArray(new Point [playerPositions.size()])));
                    stack.clear();
                }
                else
                {
                    stack.add(line);
                }
            }

            if (stack.size() > 0)
            {
                throw new IOException("Trailing non-empty lines: "
                    + Arrays.toString(stack.toArray()));
            }

            return boards;
        }
        finally
        {
            wrapped.close();
        }
    }
}