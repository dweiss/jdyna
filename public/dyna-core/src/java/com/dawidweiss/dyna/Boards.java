package com.dawidweiss.dyna;

import java.awt.Point;
import java.io.*;
import java.util.*;

import com.google.common.collect.*;

/**
 * A collection of {@link Boards} and utilities to read them from plain text files.
 * 
 * @see #read(Reader)
 */
public final class Boards
{
    /*
     * 
     */
    private static class BoardSpec
    {
        public final List<String> lines;
        public final String name;

        public BoardSpec(String boardName, ArrayList<String> boardLines)
        {
            this.name = boardName;
            this.lines = boardLines;
        }
    }

    /**
     * A list of boards available.
     */
    private final HashMap<String, BoardSpec> byName = Maps.newHashMap();
    private final List<BoardSpec> byIndex = Lists.newArrayList();

    /*
     * 
     */
    private Boards(List<BoardSpec> specs)
    {
        for (BoardSpec spec : specs)
        {
            byName.put(spec.name, spec);
            byIndex.add(spec);
        }
    }

    /**
     * Return a given board by name.
     */
    public Board get(String name)
    {
        final BoardSpec spec = byName.get(name);
        if (spec == null) throw new RuntimeException("No such board: " + name);

        return buildBoard(spec);
    }

    /**
     * Return a given board by number.
     */
    public Board get(int boardNum)
    {
        return buildBoard(byIndex.get(boardNum));
    }

    /**
     * Read one or more board specification from a plain text file.
     * <p>
     * This parser is very simple and does not attempt to perform any input validation.
     * Such validation would be probably a valuable input patch.
     * 
     * @param reader The reader of plain-text boards. The reader is always closed upon
     *            return.
     */
    public static Boards read(Reader reader) throws IOException
    {
        final BufferedReader wrapped = new BufferedReader(reader);

        try
        {
            final ArrayList<BoardSpec> boards = Lists.newArrayList();
            final ArrayList<String> boardLines = Lists.newArrayList();

            String line;
            String boardName = null;
            boolean boardBuilding = false;
            while ((line = wrapped.readLine()) != null)
            {
                // Anything that comes between board definition and its end is discarded.
                if (!boardBuilding)
                {
                    if (line.startsWith("board:"))
                    {
                        boardName = line.replace("board:", "").trim();
                        boardBuilding = true;
                    }
                }
                else
                {
                    if ("end".equals(line))
                    {
                        boards.add(new BoardSpec(boardName, Lists
                            .newArrayList(boardLines)));
                        boardLines.clear();
                        boardBuilding = false;
                    }
                    else
                    {
                        boardLines.add(line.replaceAll("\\s+$", ""));
                    }
                }
            }

            return new Boards(boards);
        }
        finally
        {
            wrapped.close();
        }
    }

    /**
     * Build a board definition from a sequence of lines read from the file.
     */
    private static Board buildBoard(BoardSpec boardSpec)
    {
        final Multimap<Character, Point> playerPositions = Multimaps
            .newArrayListMultimap();
        final List<String> lines = boardSpec.lines;

        /*
         * Remove right spaces, calculate max. width and height.
         */
        final int height = lines.size();
        int width = 0;
        for (int i = 0; i < lines.size(); i++)
        {
            final String line = lines.get(i);
            lines.set(i, line);
            width = Math.max(width, line.length());
        }

        /*
         * Create columns.
         */
        final Cell [][] cells = new Cell [width] [];
        for (int col = 0; col < width; col++)
        {
            cells[col] = new Cell [height];
        }

        /*
         * Create rows.
         */
        for (int row = 0; row < height; row++)
        {
            for (int col = 0; col < width; col++)
            {
                if (col >= lines.get(row).length())
                {
                    cells[col][row] = Cell.getInstance(CellType.CELL_EMPTY);
                    continue;
                }

                char code = lines.get(row).charAt(col);

                if (code >= '0' && code <= '9')
                {
                    playerPositions.put(code, new Point(col, row));
                    code = ' ';
                }

                if (code == 'B')
                {
                    /*
                     * Special bomb with a very large fuse limit (seems not to explode on
                     * its own, must be triggered by another bomb).
                     */
                    final BombCell bomb = (BombCell) Cell.getInstance(CellType.CELL_BOMB);
                    bomb.fuseCounter = Integer.MAX_VALUE;
                    cells[col][row] = bomb;
                }
                else
                {
                    cells[col][row] = Cell.getInstance(CellType.valueOf(code));
                }
            }
        }

        return new Board(boardSpec.name, width, height, cells,
            createPositions(playerPositions));
    }

    /**
     * Convert a multimap with positions to an ordered list of position points, the order
     * is implied by alphabetic ordering of position codes.
     */
    private static Point [] createPositions(Multimap<Character, Point> playerPositions)
    {
        final Point [] positions = new Point [playerPositions.values().size()];

        int index = 0;
        for (Character code : Sets.newTreeSet(playerPositions.keys()))
        {
            for (Point p : playerPositions.get(code))
            {
                positions[index++] = p;
            }
        }

        return positions;
    }

    /**
     * Return available board names.
     */
    public Set<String> getBoardNames()
    {
        return this.byName.keySet();
    }
}
