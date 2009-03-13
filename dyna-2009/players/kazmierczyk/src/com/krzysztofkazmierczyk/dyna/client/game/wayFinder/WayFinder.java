package com.krzysztofkazmierczyk.dyna.client.game.wayFinder;

import java.awt.Point;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.jdyna.*;

import com.dawidweiss.dyna.corba.bindings.CDirection;
import com.krzysztofkazmierczyk.dyna.BombCell;
import com.krzysztofkazmierczyk.dyna.GameStateEventUpdater;
import com.krzysztofkazmierczyk.dyna.PlayerInfo;
import com.krzysztofkazmierczyk.dyna.client.game.Utilities;

/** This class helps to find way to another cell */
public class WayFinder
{

    private static void addPointsToQueue(WayCell [][] result,
        final PriorityQueue<WayCell> q)
    {
        for (int i = 0; i < result.length; i++)
        {
            for (int j = 0; j < result[i].length; j++)
            {
                q.add(result[i][j]);
            }
        }
    }

    private static WayCell [][] constructEmptyWayCellTable(GameStateEventUpdater gseu)
    {
        final WayCell [][] result = new WayCell [gseu.getCells().length] [];

        for (int i = 0; i < gseu.getCells().length; i++)
        {
            result[i] = new WayCell [gseu.getCells()[i].length];

            for (int j = 0; j < gseu.getCells()[i].length; j++)
            {
                result[i][j] = new WayCell(new Point(i, j));
            }
        }

        return result;
    }

    private static PriorityQueue<WayCell> generatePriorityQueue(final int cellsNumber)
    {
        final PriorityQueue<WayCell> q = new PriorityQueue<WayCell>(cellsNumber + 1,
            new Comparator<WayCell>()
            {

                @Override
                public int compare(WayCell o1, WayCell o2)
                {
                    return o1.getTimeOfArrive() - o2.getTimeOfArrive();
                }

            });
        return q;
    }

    /**
     * Returns direction in which we should walk to the destination or {@code null} if
     * destination is unreachable.
     */
    public static CDirection getDirection(WayCell [][] wayCells, final Point start,
        final Point dest)
    {

        if (start.equals(dest))
        {
            return CDirection.NONE;
        }

        if (new Point(dest.x - 1, dest.y).equals(start))
        {
            return CDirection.RIGHT;
        }

        if (new Point(dest.x + 1, dest.y).equals(start))
        {
            return CDirection.LEFT;
        }

        if (new Point(dest.x, dest.y - 1).equals(start))
        {
            return CDirection.DOWN;
        }

        if (new Point(dest.x, dest.y + 1).equals(start))
        {
            return CDirection.UP;
        }

        WayCell destWayCell = wayCells[dest.x][dest.y];

        if (destWayCell.getCellOfArrive() == null)
        {
            return null;
        }
        else
        {
            return getDirection(wayCells, start, destWayCell.getCellOfArrive());
        }
    }

    /**
     * Returns for each cell time to reach or {@code Integer.MAX_VALUE} if unreachable.
     * and from which cell reach cell
     */
    public static WayCell [][] getWaysTable(GameStateEventUpdater gseu,
        Point startingPoint)
    {
        // Time to move between two vertically neighborhood cells in frames
        final int vCellWalkTime = Globals.DEFAULT_CELL_SIZE / PlayerInfo.speed.x;

        // Time to move between two horizontally neighborhood cells in frames
        final int hCellWalkTime = Globals.DEFAULT_CELL_SIZE / PlayerInfo.speed.y;

        final List<Integer> [][] explosionFrames = Utilities.getExplosionFrames(gseu);

        WayCell [][] result = constructEmptyWayCellTable(gseu);

        final int currentFrameNO = gseu.getFrameNO();

        final int startingX = startingPoint.x;
        final int startingY = startingPoint.y;

        result[startingX][startingY].setTimeOfArrive(currentFrameNO);

        // creating priority queue with distances to starting point
        final int cellsNumber = result.length * result[0].length;
        final PriorityQueue<WayCell> q = generatePriorityQueue(cellsNumber);

        // adding all elements to priority queue
        addPointsToQueue(result, q);

        while (true)
        {

            final WayCell smallDistNode = q.poll();

            // check if queue was empty or node is unreachable.
            // I could write this condition with equals but it seems to be more clear.
            if (smallDistNode == null
                || smallDistNode.getTimeOfArrive() == Integer.MAX_VALUE)
            {
                break;
            }

            final Point smallDistNodeCellPoint = smallDistNode.getCell();
            final List<Point> neighbors = Utilities.findNeihborPoints(
                smallDistNodeCellPoint, new Point(result.length, result[0].length));

            final int smallDistNodeDist = smallDistNode.getTimeOfArrive();

            for (Point point : neighbors)
            {
                CellType neighborCellType = gseu.getCells()[point.x][point.y].type;

                if ((neighborCellType.isWalkable() 
                    || ((neighborCellType == CellType.CELL_BOMB) 
                        && !isBomb(gseu, point, explosionFrames, currentFrameNO)))
                    && (Utilities.isCellSafe(explosionFrames, point, smallDistNodeDist)))
                {
                    final WayCell neighborWayCell = result[point.x][point.y];

                    final int neighborWalkTime = (smallDistNodeCellPoint.x == point.x) 
                        ? vCellWalkTime : hCellWalkTime;

                    final int alt = smallDistNodeDist + neighborWalkTime;

                    final int maxFrameSafe = Utilities.getMaxFrameSafe(explosionFrames,
                        smallDistNodeCellPoint, currentFrameNO);

                    final Integer neighborTimeOfArrive = neighborWayCell
                        .getTimeOfArrive();

                    /*
                     * We can go to this frame only if we will be able walk back to
                     * another frame. It may cause death if player will not be able to
                     * walk next but we hope it will work properly.
                     */
                    if (maxFrameSafe - neighborWalkTime > alt)
                    {

                        if (neighborTimeOfArrive == Integer.MAX_VALUE
                            || alt < neighborTimeOfArrive)
                        {
                            // We found the nearest cell to arrive

                            // We need to update queue
                            q.remove(neighborWayCell);

                            neighborWayCell.setCellOfArrive(smallDistNodeCellPoint);
                            neighborWayCell.setTimeOfArrive(alt);

                            q.add(neighborWayCell);
                        }
                    }
                }
            }

        }

        return result;
    }

    /** returns {@code true} if on given cell is bomb or explosion or just it */
    private static boolean isBomb(GameStateEventUpdater gseu, Point point,
        List<Integer> [][] explosionFrames, int frameNO)
    {
        final int x = point.x;
        final int y = point.y;

        final Cell cell = gseu.getCells()[x][y];
        if (cell instanceof BombCell)
        {
            return (frameNO < ((BombCell)cell).getBombcellEndFrameNO());
        }
        else
        {
            return false;
        }
    }

}
