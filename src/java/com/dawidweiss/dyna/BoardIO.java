package com.dawidweiss.dyna;

import java.awt.Point;
import java.io.*;
import java.util.*;

import com.google.common.collect.Lists;

/**
 * {@link Board} data reader.
 */
public final class BoardIO
{
    private BoardIO()
    {
        // no instances.
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
                                cells[col][row] = Cell.getInstance(CellType.CELL_EMPTY);
                            }
                            else
                            {
                                char code = stack.get(row).charAt(col);
    
                                if (code > '0' && code < '9')
                                {
                                    playerPositions.add(new Point(col, row));
                                    code = ' ';
                                }

                                if (code == 'B')
                                {
                                    // Special bomb with very large fuse limit.
                                    cells[col][row] = Cell.getInstance(CellType.CELL_BOMB);
                                    ((BombCell) cells[col][row]).fuseCounter = Integer.MAX_VALUE; 
                                }
                                else
                                {
                                    cells[col][row] = Cell.getInstance(CellType.valueOf(code));
                                }
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
