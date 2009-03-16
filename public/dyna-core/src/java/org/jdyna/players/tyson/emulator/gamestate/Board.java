package org.jdyna.players.tyson.emulator.gamestate;

import java.util.List;

import org.jdyna.Cell;
import org.jdyna.CellType;
import org.jdyna.IPlayerController.Direction;

import com.google.common.collect.Lists;

/**
 * Stores state of game board.
 * 
 * @author Michał Kozłowski
 * @author Bartosz Wesołowski
 */
public final class Board
{
    private final int width;
    private final int height;
    private final ExtendedCell [][] cells;
    private final List<GridCoord> bonusCells = Lists.newLinkedList();

    /**
     * @param src Source of information about cells.
     */
    public Board(final Cell [][] src)
    {
        width = src.length;
        height = src[0].length;

        // initialize cells
        cells = new ExtendedCell [width] [];
        for (int i = 0; i < width; i++)
        {
            cells[i] = new ExtendedCell [height];
            for (int j = 0; j < height; j++)
            {
                cells[i][j] = new ExtendedCell(src[i][j]);
            }
        }

    }

    /**
     * Returns the cell which row and column equal coordinates read from parameter.
     * 
     * @see #cellAt(int, int)
     */
    public ExtendedCell cellAt(final GridCoord location)
    {
        return cells[location.x][location.y];
    }

    /**
     * Returns the cell which row and column equal coordinates read from parameter.
     * 
     * @see #cellAt(GridCoord)
     */
    public ExtendedCell cellAt(final int x, final int y)
    {
        return cells[x][y];
    }

    /**
     * Returns all walkable neighbors of the given cell.
     * 
     * @param cell A cell whose neighbors we want to find.
     * @return A list of all neighboring cells that you can walk on.
     */
    public List<GridCoord> getWalkableNeighbors(final GridCoord cell)
    {
        final List<GridCoord> result = Lists.newLinkedList();

        // get all neighbors
        final GridCoord up = upCell(cell);
        final GridCoord right = rightCell(cell);
        final GridCoord down = nextCell(cell, Direction.DOWN);
        final GridCoord left = leftCell(cell);

        // add neighbors which are not null to the result list
        if (up != null && cellAt(up).isWalkable())
        {
            result.add(up);
        }
        if (right != null && cellAt(right).isWalkable())
        {
            result.add(right);
        }
        if (down != null && cellAt(down).isWalkable())
        {
            result.add(down);
        }
        if (left != null && cellAt(left).isWalkable())
        {
            result.add(left);
        }

        return result;
    }

    /**
     * @return Width of board.
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * @return Height of board.
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * @param location Location of cell.
     * @param direction
     * @return Cell that lies next to given location in given direction or
     *         <code>null</code> if that cell doesn't exist.
     */
    public GridCoord nextCell(final GridCoord location, final Direction direction)
    {
        final GridCoord next;
        switch (direction)
        {
            case DOWN:
                next = downCell(location);
                break;
            case UP:
                next = upCell(location);
                break;
            case LEFT:
                next = leftCell(location);
                break;
            case RIGHT:
                next = rightCell(location);
                break;
            default:
                next = null;
                break;
        }
        return next;
    }

    /**
     * @return List of locations of bonuses.
     */
    public List<GridCoord> getBonusCells()
    {
        return bonusCells;
    }

    /**
     * Updates state of game board.
     * 
     * @param src Source of data.
     */
    void update(final Cell [][] src)
    {
        // update cells
        bonusCells.clear();
        for (int i = 0; i < cells.length; i++)
        {
            for (int j = 0; j < cells[i].length; j++)
            {
                cells[i][j].update(src[i][j]);
                if (cells[i][j].getType() == CellType.CELL_BONUS_BOMB
                    || cells[i][j].getType() == CellType.CELL_BONUS_RANGE)
                {
                    bonusCells.add(new GridCoord(i, j));
                }
            }
        }
    }

    private GridCoord leftCell(final GridCoord location)
    {
        if (location.x > 0)
        {
            return new GridCoord(location.x - 1, location.y);
        }
        else
        {
            return null;
        }
    }

    private GridCoord rightCell(final GridCoord location)
    {
        if (location.x + 1 < width)
        {
            return new GridCoord(location.x + 1, location.y);
        }
        else
        {
            return null;
        }
    }

    private GridCoord upCell(final GridCoord location)
    {
        if (location.y > 0)
        {
            return new GridCoord(location.x, location.y - 1);
        }
        else
        {
            return null;
        }
    }

    private GridCoord downCell(final GridCoord location)
    {
        if (location.y + 1 < cells[location.x].length)
        {
            return new GridCoord(location.x, location.y + 1);
        }
        else
        {
            return null;
        }
    }

}
