package com.krzysztofkazmierczyk.dyna.client.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.IPlayerSprite;
import com.dawidweiss.dyna.corba.bindings.CPlayer;
import com.krzysztofkazmierczyk.dyna.BombCell;
import com.krzysztofkazmierczyk.dyna.CellWithTime;
import com.krzysztofkazmierczyk.dyna.GameStateEventUpdater;
import com.krzysztofkazmierczyk.dyna.PlayerInfo;
import com.krzysztofkazmierczyk.dyna.client.game.wayFinder.WayCell;
import com.krzysztofkazmierczyk.dyna.client.game.wayFinder.WayFinder;

/** this class contains some static methods useful to operating on board and game. */
public class Utilities
{

    private final static Logger logger = Logger.getLogger("Utilities");

    /** Time in which we feel safe after or before explosion */
    public static final int SAFE_DELAY = 12;

    private static boolean canReachSafePoints(GameStateEventUpdater gseu, Point playerCell)
    {
        WayCell [][] wayCells = WayFinder.getWaysTable(gseu, playerCell);
        List<Integer> [][] explosionFrames = Utilities.getExplosionFrames(gseu);

        return canReachSafePoints(wayCells, explosionFrames);
    }

    /**
     * Checks if it is possible for player to move to poi with no bombs.
     */
    private static boolean canReachSafePoints(WayCell [][] wayCells,
        List<Integer> [][] explosionFrames)
    {
        for (int i = 0; i < wayCells.length; i++)
        {
            for (int j = 0; j < wayCells[i].length; j++)
            {
                if (wayCells[i][j].isReachable() && explosionFrames[i][j].isEmpty())
                {
                    return true;
                }
            }
        }
        return false;
    }

    /** Removes from List cell with given coordinates. */
    private static void changeCellTime(List<CellWithTime> list, Point point, int newTime)
    {
        for (Iterator<CellWithTime> it = list.iterator(); it.hasNext();)
        {
            CellWithTime cellWithTime = (CellWithTime) it.next();
            if (cellWithTime.x == point.x && cellWithTime.y == point.y)
            {
                cellWithTime.setFrameNO(newTime);
            }
        }
    }

    /** Clones game state event updater without any exceptions */
    private static GameStateEventUpdater cloneGameStateEventUpdater(
        GameStateEventUpdater gseu)
    {
        GameStateEventUpdater clonned = null;
        try
        {
            clonned = (GameStateEventUpdater) gseu.clone();
        }
        catch (CloneNotSupportedException e)
        {
            logger.log(Level.SEVERE, "Error during clonning GameStateteEventUpdater"
                + e.getMessage());
        }
        return clonned;
    }

    /**
     * This function is sensible for spoofing of other players, but I realy do not want to
     * search in the code procedure of naming players :(
     */
    public static int findMyName(CPlayer [] players, String myName)
    {
        for (int i = 0; i < players.length; i++)
        {
            // Ignore locale :)
            final CPlayer playerName = players[i];
            if (playerName.name.toLowerCase().startsWith(myName.toLowerCase()))
            {
                return i;
            }
        }
        logger.warning("I did not find myself in players name. One of us has bug.");
        return 0; // So I decide to be player 0 hahahaha :)
    }

    public static int findMyName(List<PlayerInfo> players, String myName)
    {
        for (int i = 0; i < players.size(); i++)
        {
            // Ignore locale :)
            final PlayerInfo playerName = players.get(i);
            if (playerName.getName().toLowerCase().startsWith(myName.toLowerCase()))
            {
                return i;
            }
        }
        logger.warning("I did not find myself in players name. One of us has bug.");
        return 0; // So I decide to be player 0 hahahaha :)

    }

    public static Point findNearestSafePlace(GameStateEventUpdater gseu,
        WayCell [][] wayCells, List<Integer> [][] bombCells, int playerNO)
    {

        final Point playerCell = gseu.getPlayers().get(playerNO).getCell(
            gseu.getBoardInfo());

        // get all cells on which any bombs explode or explode before reach this cell.
        List<Point> safeCells = getSafeCells(wayCells, bombCells);

        if (safeCells.isEmpty())
        {
            // Trying to turn back clock and check if it helps to find sage cells.
            GameStateEventUpdater clonned = cloneGameStateEventUpdater(gseu);
            clonned.setFrameNO(gseu.getFrameNO() - SAFE_DELAY);

            safeCells = getSafeCells(WayFinder.getWaysTable(gseu, playerCell), Utilities
                .getExplosionFrames(clonned));
        }

        if (safeCells.isEmpty())
        {

            final List<Point> reachableCells = getReachableCells(wayCells);

            int maxReachFrame = Integer.MIN_VALUE;

            // Searching cell with maximum safe time
            for (Point p : reachableCells)
            {
                final int reachTime = wayCells[p.x][p.y].getTimeOfArrive();
                final int maxFrame = getMaxFrameSafe(bombCells, p, reachTime);

                if (maxFrame > maxReachFrame)
                {
                    maxReachFrame = maxFrame;
                }
            }

            // choosing to safe points all reachable cell with reach time equal to max
            // reach time
            for (Point p : reachableCells)
            {
                final int reachTime = wayCells[p.x][p.y].getTimeOfArrive();
                final int maxFrame = getMaxFrameSafe(bombCells, p, reachTime);

                if (maxFrame == maxReachFrame)
                {
                    safeCells.add(p);
                }
            }
        }

        Point nearestPoint = null;
        int nearestPointdist = Integer.MAX_VALUE;

        for (Point p : safeCells)
        {
            final Integer timeOfArrive = wayCells[p.x][p.y].getTimeOfArrive();
            if (timeOfArrive < nearestPointdist)
            {
                nearestPoint = p;
                nearestPointdist = timeOfArrive;
            }
        }

        return nearestPoint;
    }

    /**
     * Returns neighbors of given point (points: up, down, left and right to given point).
     */
    public static List<Point> findNeihborPoints(final Point smallDistNodeCellPoint,
        Point dimension)
    {
        final List<Point> neighbors = new ArrayList<Point>(4);

        if (smallDistNodeCellPoint.x > 0)
        {
            neighbors.add(new Point(smallDistNodeCellPoint.x - 1,
                smallDistNodeCellPoint.y));
        }

        if (smallDistNodeCellPoint.y > 0)
        {
            neighbors.add(new Point(smallDistNodeCellPoint.x,
                smallDistNodeCellPoint.y - 1));
        }

        if (smallDistNodeCellPoint.x < dimension.x - 1)
        {
            neighbors.add(new Point(smallDistNodeCellPoint.x + 1,
                smallDistNodeCellPoint.y));
        }

        if (smallDistNodeCellPoint.y < dimension.y - 1)
        {
            neighbors.add(new Point(smallDistNodeCellPoint.x,
                smallDistNodeCellPoint.y + 1));
        }

        return neighbors;
    }

    private static List<CellWithTime> getBombCellsByExplosionTime(
        GameStateEventUpdater gseu)
    {

        List<CellWithTime> bombCells = new ArrayList<CellWithTime>();

        for (int i = 0; i < gseu.getCells().length; i++)
        {
            for (int j = 0; j < gseu.getCells()[i].length; j++)
            {
                if (gseu.getCells()[i][j] instanceof BombCell)
                {
                    bombCells.add(new CellWithTime(i, j,
                        ((BombCell) gseu.getCells()[i][j]).getExplosionFrameNO()));
                }
            }

        }

        sortByExplosionTime(bombCells);

        return bombCells;
    }

    /**
     * For each cell returns list of explosion times of each bomb.
     */
    @SuppressWarnings("unchecked")
    public static List<Integer> [][] getExplosionFrames(GameStateEventUpdater gseu)
    {
        List<Integer> [][] result = new List [gseu.getCells().length] [];

        final Cell [][] cells = gseu.getCells();

        for (int i = 0; i < gseu.getCells().length; i++)
        {
            result[i] = new List [gseu.getCells()[i].length];

            for (int j = 0; j < gseu.getCells()[i].length; j++)
            {
                result[i][j] = new ArrayList<Integer>();
            }
        }

        List<CellWithTime> sortedBombCells = getBombCellsByExplosionTime(gseu);

        while (!sortedBombCells.isEmpty())
        {
            CellWithTime earliestExplosion = sortedBombCells.get(0);
            sortedBombCells.remove(0);

            final int x = earliestExplosion.x;
            final int y = earliestExplosion.y;
            final int explosionFrameNO = earliestExplosion.getFrameNO();

            if (!result[x][y].contains(explosionFrameNO))
            {
                result[x][y].add(explosionFrameNO);
            }
            final int range = ((BombCell) gseu.getCells()[x][y]).getRange();

            // Selecting all fields up to bomb
            for (int i = 1; i <= range; i++)
            {
                final int modified = x - i;
                if (modified >= 0)
                {
                    final Cell modifiedCell = gseu.getCells()[modified][y];
                    final CellType modifiedType = modifiedCell.type;
                    if (!result[modified][y].contains(explosionFrameNO)
                        && !CellType.CELL_WALL.equals(modifiedType))
                    {
                        result[modified][y].add(explosionFrameNO);
                    }

                    // Change dropping time to earlier and thus explosion time to proper
                    if (CellType.CELL_BOMB.equals(modifiedType))
                    {
                        Utilities.changeCellTime(sortedBombCells, new Point(modified, y),
                            explosionFrameNO);
                        ((BombCell) (cells[modified][y]))
                            .setExplosionFrameNO(explosionFrameNO);
                    }

                    if (!modifiedType.isWalkable())
                    {
                        break; // Do not process if board is not walkable (bomb does not
                        // hurt more).
                    }
                }
                else
                {
                    // modified out the board.
                    break;
                }
            }

            // Selecting all fields down to bomb
            for (int i = 1; i <= range; i++)
            {
                final int modified = x + i;
                if (modified < gseu.getCells().length)
                {
                    final Cell modifiedCell = gseu.getCells()[modified][y];
                    final CellType modifiedType = modifiedCell.type;
                    if (!result[modified][y].contains(explosionFrameNO)
                        && !CellType.CELL_WALL.equals(modifiedType))
                    {
                        result[modified][y].add(explosionFrameNO);
                    }

                    // Change dropping time to earlier and thus explosion time to proper
                    if (CellType.CELL_BOMB.equals(modifiedType))
                    {
                        Utilities.changeCellTime(sortedBombCells, new Point(modified, y),
                            explosionFrameNO);
                        ((BombCell) (cells[modified][y]))
                            .setExplosionFrameNO(explosionFrameNO);
                    }

                    if (!modifiedType.isWalkable())
                    {
                        break; // Do not process if board is not walkable (bomb doeas not
                        // hurt more).
                    }
                }
                else
                {
                    // modified out the board.
                    break;
                }
            }

            // Selecting all fields left to bomb
            for (int i = 1; i <= range; i++)
            {
                final int modified = y - i;
                if (modified >= 0)
                {
                    final Cell modifiedCell = gseu.getCells()[x][modified];
                    final CellType modifiedType = modifiedCell.type;
                    if (!result[x][modified].contains(explosionFrameNO)
                        && !CellType.CELL_WALL.equals(modifiedType))
                    {
                        result[x][modified].add(explosionFrameNO);
                    }

                    // Change dropping time to earlier and thus explosion time to proper
                    if (CellType.CELL_BOMB.equals(modifiedType))
                    {
                        Utilities.changeCellTime(sortedBombCells, new Point(x, modified),
                            explosionFrameNO);
                        ((BombCell) (cells[x][modified]))
                            .setExplosionFrameNO(explosionFrameNO);
                    }

                    if (!modifiedType.isWalkable())
                    {
                        break; // Do not process if board is not walkable (bomb doeas not
                        // hurt more).
                    }
                }
                else
                {
                    // modified out the board.
                    break;
                }
            }

            // Selecting all fields right to bomb
            for (int i = 1; i <= range; i++)
            {
                final int modified = y + i;
                if (modified < gseu.getCells()[0].length)
                {
                    final Cell modifiedCell = gseu.getCells()[x][modified];
                    final CellType modifiedType = modifiedCell.type;
                    if (!result[x][modified].contains(explosionFrameNO)
                        && !CellType.CELL_WALL.equals(modifiedType))
                    {
                        result[x][modified].add(explosionFrameNO);
                    }

                    // Change dropping time to earlier and thus explosion time to proper
                    if (CellType.CELL_BOMB.equals(modifiedType))
                    {
                        Utilities.changeCellTime(sortedBombCells, new Point(x, modified),
                            explosionFrameNO);
                        ((BombCell) (cells[x][modified]))
                            .setExplosionFrameNO(explosionFrameNO);
                    }

                    if (!modifiedType.isWalkable())
                    {
                        break; // Do not process if board is not walkable (bomb doeas not
                        // hurt more).
                    }
                }
                else
                {
                    // modified out the board.
                    break;
                }
            }

            sortByExplosionTime(sortedBombCells);
        }

        return result;

    }

    /**
     * Returns maximum time when player is safe on this field (will be not killed by
     * explosion
     */
    public static int getMaxFrameSafe(List<Integer> [][] explosionFrames, Point point,
        int currentFrameNO)
    {
        final List<Integer> explosions = explosionFrames[point.x][point.y];

        Integer explosion = null;

        for (Iterator<Integer> it = explosions.iterator(); it.hasNext();)
        {
            explosion = (Integer) it.next();
            if (explosion > currentFrameNO)
            {
                break;
            }
        }

        if (explosion != null)
        {
            return explosion - SAFE_DELAY;
        }
        else
        {
            return Integer.MAX_VALUE;
        }
    }

    public static Point getNearestBonusPoint(Cell [][] cells, WayCell [][] wayCells)
    {
        Point result = null;
        int nearestPoint = Integer.MAX_VALUE;

        for (int i = 0; i < cells.length; i++)
        {
            for (int j = 0; j < cells[i].length; j++)
            {
                if (CellType.CELL_BONUS_BOMB.equals(cells[i][j].type)
                    || CellType.CELL_BONUS_RANGE.equals(cells[i][j].type))
                {
                    final Integer timeOfArrive = wayCells[i][j].getTimeOfArrive();
                    if (timeOfArrive < nearestPoint)
                    {
                        result = new Point(i, j);
                        nearestPoint = timeOfArrive;
                    }
                }
            }
        }
        return result;
    }

    /** Returns nearest player id or {@code null} if no player is reachable. */
    public static Integer getNearestPlayerNO(GameStateEventUpdater gseu,
        WayCell [][] wayCells, int myPlayerNO)
    {

        int minFrameNO = Integer.MAX_VALUE;
        Integer result = null;

        for (int i = 0; i < gseu.getPlayers().size(); i++)
        {
            PlayerInfo pi = gseu.getPlayers().get(i);

            if (!pi.isDead() && myPlayerNO != i)
            {
                final Point p = pi.getCell(gseu.getBoardInfo());
                final int frameNO = wayCells[p.x][p.y].getTimeOfArrive();

                if (frameNO < minFrameNO)
                {
                    result = i;
                    minFrameNO = frameNO;
                }
            }
        }

        return result;
    }

    private static List<Point> getReachableCells(WayCell [][] wayCells)
    {
        List<Point> reachableCells = new ArrayList<Point>();

        for (int i = 0; i < wayCells.length; i++)
        {
            for (int j = 0; j < wayCells[i].length; j++)
            {
                if (wayCells[i][j].isReachable())
                {
                    reachableCells.add(wayCells[i][j].getCell());
                }
            }
        }
        return reachableCells;
    }

    private static List<Point> getSafeCells(WayCell [][] wayCells,
        List<Integer> [][] bombCells)
    {
        // get all cells on which any bombs explode or explode before reach this cell. */
        final List<Point> safeCells = new ArrayList<Point>();

        // find all places which we can reach
        final List<Point> reachableCells = getReachableCells(wayCells);

        for (Point p : reachableCells)
        {
            final int reachTime = wayCells[p.x][p.y].getTimeOfArrive();
            final List<Integer> bombCell = bombCells[p.x][p.y];

            if (bombCell.isEmpty() || bombCell.get(bombCell.size() - 1) < reachTime)
            {
                safeCells.add(p);
            }
        }
        return safeCells;
    }

    public static boolean isCellSafe(List<Integer> [][] explosionFrames, Point point,
        int currentFrameNO)
    {
        return (getMaxFrameSafe(explosionFrames, point, currentFrameNO) - currentFrameNO >= SAFE_DELAY);
    }

    /** Returns true if player is safe and will be safe soon. */
    public static boolean isPlayerSafe(GameStateEventUpdater gseu,
        List<Integer> [][] explosionFrames, WayCell [][] wayCells, int playerNO)
    {

        PlayerInfo pi = gseu.getPlayers().get(playerNO);
        Point playerCell = gseu.getBoardInfo().pixelToGrid(pi.getPosition());

        final List<Integer> bombCell = explosionFrames[playerCell.x][playerCell.y];

        if ((!bombCell.isEmpty())
            && (bombCell.get(0) < gseu.getFrameNO() + 2 * SAFE_DELAY))
        {
            return false;
        }

        if (!canReachSafePoints(wayCells, explosionFrames))
        {
            return false;
        }

        GameStateEventUpdater clonned = null;

        try
        {
            clonned = (GameStateEventUpdater) gseu.clone();
        }
        catch (CloneNotSupportedException e)
        {
            logger.log(Level.SEVERE, "Error during clonning GameStateteEventUpdater"
                + e.getMessage());
        }

        clonned.setFrameNO(clonned.getFrameNO() + 2 * SAFE_DELAY);

        return canReachSafePoints(clonned, playerCell);
    }

    /**
     * Returns player id on the given grid point or {@code null} if any player standing on
     * this grid point. If more than one player is standing on this grid, it will be
     * returned player with lower id.
     */
    public static Integer playerIDOnTheCell(BoardInfo boardInfo, Point gridPoint,
        List<? extends IPlayerSprite> sprites)
    {
        for (int i = 0; i < sprites.size(); i++)
        {
            Point spritePos = sprites.get(i).getPosition();
            if (boardInfo.pixelToGrid(spritePos).equals(gridPoint))
            {
                return i;
            }
        }
        return null;
    }

    /**
     * Returns {@code true} if player has chance to escape for the bombs.
     * 
     * @param playerNO Number of player who dropping bomb.
     **/
    public static boolean safeToDropBomb(GameStateEventUpdater gseu, int playerNO)
    {

        GameStateEventUpdater clonned = null;

        clonned = cloneGameStateEventUpdater(gseu);

        Cell [][] cells = clonned.getCells();

        PlayerInfo pi = clonned.getPlayers().get(playerNO);
        Point playerCell = clonned.getBoardInfo().pixelToGrid(pi.getPosition());

        cells[playerCell.x][playerCell.y] = new BombCell(playerNO, clonned.getFrameNO(),
            pi.getBombRange());

        if (isPlayerSafe(clonned, getExplosionFrames(clonned), WayFinder.getWaysTable(
            clonned, playerCell), playerNO))
        {
            // Checking for each player if it is still to drop bomb if the player dropped
            // bomb
            for (int i = 0; i < gseu.getPlayers().size(); i++)
            {

                PlayerInfo pi2 = gseu.getPlayers().get(i);

                if ((i != playerNO) && (pi2.getBombCount() > 0))
                {
                    GameStateEventUpdater clonned2 = null;

                    clonned2 = cloneGameStateEventUpdater(gseu);

                    Point playerCell2 = clonned2.getBoardInfo().pixelToGrid(
                        pi2.getPosition());

                    Cell [][] cells2 = clonned2.getCells();

                    cells2[playerCell2.x][playerCell2.y] = new BombCell(i, clonned2
                        .getFrameNO(), pi2.getBombRange());

                    if (!isPlayerSafe(clonned2, getExplosionFrames(clonned2), WayFinder
                        .getWaysTable(clonned2, playerCell2), playerNO))
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        else
        {
            return false;
        }

    }

    private static void sortByExplosionTime(List<CellWithTime> list)
    {
        Collections.sort(list, new Comparator<CellWithTime>()
        {

            @Override
            public int compare(CellWithTime o1, CellWithTime o2)
            {
                return o1.getFrameNO() - o2.getFrameNO();
            };
        });
    }
}
