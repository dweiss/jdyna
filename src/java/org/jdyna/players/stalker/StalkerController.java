package org.jdyna.players.stalker;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdyna.*;
import org.jdyna.GameEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
class StalkerController implements IGameEventListener, IPlayerController
{
    private final static Logger logger = LoggerFactory
        .getLogger(StalkerController.class);

    /**
     * Current direction of player
     */
    private Direction currDirection = null;

    /**
     * Drop bombs indicator
     */
    private boolean dropBombs = false;

    /**
     * Number of frame
     */
    private int frame = 0;

    /**
     * Cell board model
     */
    private Cell [][] cells = null;

    /**
     * Cell bomb model (only bombs and explosion times are indicated)
     */
    private int [][][] board;

    /**
     * Players' bomb ranges
     */
    private HashMap<String, Integer> ranges = null;

    /**
     * Range bonuses present on board
     */
    private List<Point> rangeBonuses = new ArrayList<Point>();

    /**
     * List of players
     */
    private List<? extends IPlayerSprite> players = null;

    /**
     * Position of player on board (in cell coordinates)
     */
    private Point myPosition;

    /**
     * Player's name
     */
    private String myName;

    /**
     * Configuration for the game.
     */
    private GameConfiguration conf;

    /**
     * Bomb indicator
     */
    private static final int F_BOMB = Integer.MAX_VALUE;

    public StalkerController(String name)
    {
        myName = name;
    }

    /*
	 * 
	 */
    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        this.frame = frame;

        for (GameEvent ge : events)
        {
            if (ge.type == Type.GAME_START)
            {
                GameStartEvent gse = (GameStartEvent) ge;
                this.conf = gse.getConfiguration();
            }
            else if (ge.type == Type.GAME_OVER)
            {
                try
                {
                    super.finalize();
                    this.finalize();
                }
                catch (Throwable e)
                {
                    logger.error("Could not finalize the controller", e);
                }
            } 
            else if (ge.type == Type.GAME_STATE)
            {
                GameStateEvent gse = (GameStateEvent) ge;
                this.cells = gse.getCells();

                players = gse.getPlayers();
                for (IPlayerSprite ips : players)
                {
                    if (ips.getName().equals(myName))
                    {
                        if (ips.isDead())
                        {
                            return;
                        }
                        myPosition = getBoardPosition(ips.getPosition());
                    }
                }
                currDirection = null;
                dropBombs = false;

                detectDeadPlayers();
                lookupCollectedRangeBonuses();
                createBoardModel();

                // debugBombs();
                // debugCells();

                // Procedural logic of the player
                if (inRange() || isBomb(myPosition))
                {
                    escapeMove();
                }
                else
                {
                    if (opponentNear().length > 0 && canPlaceBomb())
                    {
                        dropBombs = true;
                    }
                    doOffensiveMove();
                }
            }
        }

    }

    /**
     * Swaps all players' positions (including the player himself) with bomb cells and
     * tries to find an escape move. If it succeeds, a bomb can be safely placed.
     * 
     * @return Player can safely place bomb or not.
     */
    private boolean canPlaceBomb()
    {
        Direction oldDirection = currDirection;
        boolean canPlaceBomb = false;
        Point [] opponents = allOpponents();
        Cell [] originalCells = new Cell [opponents.length + 1];
        int counter = 0;

        for (Point p : opponents)
        {
            originalCells[counter++] = cells[p.x][p.y];
            cells[p.x][p.y] = Cell.getInstance(CellType.CELL_BOMB);
        }
        originalCells[counter] = cells[myPosition.x][myPosition.y];
        cells[myPosition.x][myPosition.y] = Cell.getInstance(CellType.CELL_BOMB);

        createBoardModel();
        escapeMove();
        canPlaceBomb = (currDirection != null);

        counter = 0;
        for (Point p : opponents)
        {
            cells[p.x][p.y] = originalCells[counter++];
        }
        cells[myPosition.x][myPosition.y] = originalCells[counter];

        createBoardModel();
        currDirection = oldDirection;

        return canPlaceBomb;
    }

    /**
     * @return Array of non-dead and non-self players in game.
     */
    private Point [] allOpponents()
    {
        ArrayList<Point> opponents = new ArrayList<Point>();

        for (IPlayerSprite ips : players)
        {
            Point playerPosition = getBoardPosition(ips.getPosition());
            if (!ips.getName().equals(myName) && !ips.isDead())
            {
                opponents.add(playerPosition);
            }
        }
        return opponents.toArray(new Point [opponents.size()]);
    }

    /**
     * @return Array of players' positions that are standing near in line of the player.
     */
    private Point [] opponentNear()
    {
        Point [] near =
        {
            new Point(myPosition.x - 1, myPosition.y),
            new Point(myPosition.x + 1, myPosition.y),
            new Point(myPosition.x - 2, myPosition.y),
            new Point(myPosition.x + 2, myPosition.y),
            new Point(myPosition.x - 3, myPosition.y),
            new Point(myPosition.x + 3, myPosition.y),
            new Point(myPosition.x, myPosition.y - 1),
            new Point(myPosition.x, myPosition.y + 1),
            new Point(myPosition.x, myPosition.y - 2),
            new Point(myPosition.x, myPosition.y + 2),
            new Point(myPosition.x, myPosition.y - 3),
            new Point(myPosition.x, myPosition.y + 3), myPosition
        };

        ArrayList<Point> opponents = new ArrayList<Point>();

        for (IPlayerSprite ips : players)
        {
            Point playerPosition = getBoardPosition(ips.getPosition());
            for (Point p : near)
            {
                if (!ips.getName().equals(myName) && !ips.isDead()
                    && p.equals(playerPosition))
                {
                    opponents.add(p);
                }
            }
        }
        return opponents.toArray(new Point [opponents.size()]);
    }

    /**
     * BFS algorithm that looks up nearest field on which an opponent or any bonus is
     * located.
     */
    private void doOffensiveMove()
    {
        ArrayList<PointS> nodes = new ArrayList<PointS>();
        ArrayList<PointS> visited = new ArrayList<PointS>();
        visited.add(new PointS(myPosition));
        nodes.add(new PointS(myPosition));
        while (nodes.size() > 0)
        {
            PointS p = nodes.remove(0);
            if (cells[p.x][p.y].type == CellType.CELL_BONUS_BOMB
                || cells[p.x][p.y].type == CellType.CELL_BONUS_RANGE
                || (getPlayerNames(p).length > 0 && p.depth > 1))
            {
                currDirection = p.move;
                return;
            }

            Point [] moves = getFreeAdjacentCells(p);
            for (Point next : moves)
            {
                PointS nextPoint = new PointS(next);
                if (p.move == null)
                {
                    nextPoint.move = getDirection(p, next);
                }
                else
                {
                    nextPoint.move = p.move;
                }

                nextPoint.depth = p.depth + 1;
                if (!visited.contains(nextPoint)
                    && (p.depth > 2 || canReachField(nextPoint)))
                {
                    visited.add(nextPoint);
                    nodes.add(nextPoint);
                }
            }
        }
    }

    /**
     * Checks if any range bonus has been collected and increases range for a given
     * player. If it is ambiguous to detect which player collected the bonus, all players
     * standing on a bonus cell are getting their range increased.
     */
    private void lookupCollectedRangeBonuses()
    {
        ArrayList<Point> collectedBonuses = new ArrayList<Point>();
        for (Point p : rangeBonuses)
        {
            if (cells[p.x][p.y].type != CellType.CELL_BONUS_RANGE)
            {
                String [] playerNames = getPlayerNames(p);
                for (String playerName : playerNames)
                {
                    // [NPE]
                    if (!ranges.containsKey(playerName)) ranges.put(playerName, 0);

                    ranges.put(playerName, ranges.get(playerName) + 1);
                }
                collectedBonuses.add(p);
            }
        }
        for (Point collectedBonus : collectedBonuses)
        {
            rangeBonuses.remove(collectedBonus);
        }
    }

    /**
     * Returns players' names standing on point p.
     * 
     * @param p Point to check
     * @return Array of players' names
     */
    private String [] getPlayerNames(Point p)
    {
        ArrayList<String> playersOnCell = new ArrayList<String>();
        for (IPlayerSprite ips : players)
        {
            if (getBoardPosition(ips.getPosition()).equals(p) && !ips.isDead())
            {
                playersOnCell.add(ips.getName());
            }
        }
        return playersOnCell.toArray(new String [playersOnCell.size()]);
    }

    /**
     * Adds a new bonus to the bonuses list if it has just appeared.
     * 
     * @param p Point where a bonus has been found
     */
    private void updateBonuses(Point p)
    {
        for (Point bonus : rangeBonuses)
        {
            if (bonus.equals(p)) return;
        }
        rangeBonuses.add(p);
    }

    /**
     * @return <b>true</b> if player is in range of a bomb, <b>false</b> otherwise
     */
    private boolean inRange()
    {
        return (board[myPosition.x][myPosition.y][0] > 0);
    }

    /**
     * BFS algorithm used to lookup the nearest safe cell on board.
     */
    private void escapeMove()
    {
        int maxDepth = 0;
        Direction dirToMove = null;
        ArrayList<PointS> nodes = new ArrayList<PointS>();
        ArrayList<PointS> visited = new ArrayList<PointS>();
        visited.add(new PointS(myPosition));
        nodes.add(new PointS(myPosition));
        while (nodes.size() > 0)
        {
            PointS p = nodes.remove(0);
            if (board[p.x][p.y][0] == 0)
            {
                if (p.depth > maxDepth && !p.mayBeDeadEnd)
                {
                    maxDepth = p.depth;
                    dirToMove = p.move;
                }
            }
            Point [] moves = getFreeAdjacentCells(p);
            for (Point next : moves)
            {
                PointS nextPoint = new PointS(next);
                if (p.move == null)
                {
                    nextPoint.move = getDirection(p, next);
                }
                else
                {
                    nextPoint.move = p.move;
                }
                nextPoint.depth = p.depth + 1;
                if (!visited.contains(nextPoint) && canReachField(nextPoint)
                    && getPlayerNames(nextPoint).length == 0)
                {
                    visited.add(nextPoint);
                    nodes.add(nextPoint);
                }
            }
        }
        currDirection = dirToMove;
    }

    /**
     * Checks if the given field can be reached on the escape/attack path.
     * 
     * @param p Field to check
     * @return <b>true</b> if the field is accessible in given path, <b>false</b>
     *         otherwise
     */
    private boolean canReachField(PointS p)
    {
        return (isSafe(p) && (board[p.x][p.y][0] == 0 || board[p.x][p.y][0]
            - ((double) p.depth + 1) * Constants.DEFAULT_CELL_SIZE > 0));
    }

    /**
     * @param from Source point
     * @param to Destination point
     * @return Direction for the player to move in order to move from one point to
     *         another.
     */
    private Direction getDirection(Point from, Point to)
    {
        if (from.x > to.x) return Direction.LEFT;
        if (from.x < to.x) return Direction.RIGHT;
        if (from.y > to.y) return Direction.UP;
        if (from.y < to.y) return Direction.DOWN;
        return null;
    }

    /**
     * Returns array of adjacent cells that are accessible from the given one.
     * 
     * @param p Starting point
     * @return Array of free adjacent cells
     */
    private Point [] getFreeAdjacentCells(Point p)
    {
        ArrayList<Point> adjCells = new ArrayList<Point>();

        if (p.x > 0 && isSafe(new Point(p.x - 1, p.y)))
        {
            adjCells.add(new Point(p.x - 1, p.y));
        }
        if (p.x < cells.length - 1 && isSafe(new Point(p.x + 1, p.y)))
        {
            adjCells.add(new Point(p.x + 1, p.y));
        }
        if (p.y > 0 && isSafe(new Point(p.x, p.y - 1)))
        {
            adjCells.add(new Point(p.x, p.y - 1));
        }
        if (p.y < cells[p.x].length - 1 && isSafe(new Point(p.x, p.y + 1)))
        {
            adjCells.add(new Point(p.x, p.y + 1));
        }
        return adjCells.toArray(new Point [adjCells.size()]);
    }

    /**
     * Checks for dead players and sets their bomb range to default.
     */
    private void detectDeadPlayers()
    {
        ranges = new HashMap<String, Integer>();
        for (IPlayerSprite ips : players)
        {
            if (ips.isDead())
            {
                ranges.put(ips.getName(), conf.DEFAULT_BOMB_RANGE);
            }
        }
    }

    /**
     * Creates the bomb board model, where just bombs and their predicted explosion times
     * are stored.
     */
    private void createBoardModel()
    {
        if (board == null)
        {
            board = new int [cells.length] [cells[0].length] [3];
        }

        // reset non-bomb fields
        for (int i = 0; i < board.length; i++)
        {
            for (int j = 0; j < board[i].length; j++)
            {
                if (cells[i][j].type != CellType.CELL_BOMB)
                {
                    board[i][j][0] = 0;
                    board[i][j][1] = 0;
                    board[i][j][2] = 0;
                }
            }
        }

        for (int i = 0; i < cells.length; i++)
        {
            for (int j = 0; j < cells[i].length; j++)
            {

                // bomb on board
                if (cells[i][j].type == CellType.CELL_BOMB)
                {
                    // it is a new bomb
                    if (board[i][j][0] != F_BOMB)
                    {
                        board[i][j][0] = F_BOMB;
                        board[i][j][1] = frame;
                        board[i][j][2] = getPlayerRange(new Point(i, j));
                    }
                    calcRanges(new Point(i, j));
                }

                // bonus on board
                if (cells[i][j].type == CellType.CELL_BONUS_RANGE)
                {
                    updateBonuses(new Point(i, j));
                }
            }
        }
    }

    /**
     * Updates bomb board model with another bomb.
     * 
     * @param p Bomb cell
     */
    private void calcRanges(Point p)
    {
        int x = p.x;
        int y = p.y;
        int range = board[p.x][p.y][2];
        int explosionTime = conf.DEFAULT_FUSE_FRAMES - (frame - board[p.x][p.y][1]);

        while (range > 0 && x > 0)
        {
            x--;
            if (isBlocked(new Point(x, y)))
            {
                if (isBomb(new Point(x, y)))
                {
                    if (board[x][y][1] > 0)
                    {
                        board[x][y][1] = Math.min(board[x][y][1], board[p.x][p.y][1]);
                        board[p.x][p.y][1] = board[x][y][1];
                    }
                }
                break;
            }
            else
            {
                if (board[x][y][0] == 0)
                {
                    board[x][y][0] = explosionTime;
                }
                else
                {
                    board[x][y][0] = Math.min(board[x][y][0], explosionTime);
                }
                range--;
            }
        }

        x = p.x;
        y = p.y;
        range = board[p.x][p.y][2];
        explosionTime = conf.DEFAULT_FUSE_FRAMES - (frame - board[p.x][p.y][1]);
        while (range > 0 && x < board.length - 1)
        {
            x++;
            if (isBlocked(new Point(x, y)))
            {
                if (isBomb(new Point(x, y)))
                {
                    if (board[x][y][1] > 0)
                    {
                        board[x][y][1] = Math.min(board[x][y][1], board[p.x][p.y][1]);
                        board[p.x][p.y][1] = board[x][y][1];
                    }
                }
                break;
            }
            else
            {
                if (board[x][y][0] == 0)
                {
                    board[x][y][0] = explosionTime;
                }
                else
                {
                    board[x][y][0] = Math.min(board[x][y][0], explosionTime);
                }
                range--;
            }
        }

        x = p.x;
        y = p.y;
        range = board[p.x][p.y][2];
        explosionTime = conf.DEFAULT_FUSE_FRAMES - (frame - board[p.x][p.y][1]);
        while (range > 0 && y > 0)
        {
            y--;
            if (isBlocked(new Point(x, y)))
            {
                if (isBomb(new Point(x, y)))
                {
                    if (board[x][y][1] > 0)
                    {
                        board[x][y][1] = Math.min(board[x][y][1], board[p.x][p.y][1]);
                        board[p.x][p.y][1] = board[x][y][1];
                    }
                }
                break;
            }
            else
            {
                if (board[x][y][0] == 0)
                {
                    board[x][y][0] = explosionTime;
                }
                else
                {
                    board[x][y][0] = Math.min(board[x][y][0], explosionTime);
                }
                range--;
            }
        }

        x = p.x;
        y = p.y;
        range = board[p.x][p.y][2];
        explosionTime = conf.DEFAULT_FUSE_FRAMES - (frame - board[p.x][p.y][1]);
        while (range > 0 && y < board[x].length - 1)
        {
            y++;
            if (isBlocked(new Point(x, y)))
            {
                if (isBomb(new Point(x, y)))
                {
                    if (board[x][y][1] > 0)
                    {
                        board[x][y][1] = Math.min(board[x][y][1], board[p.x][p.y][1]);
                        board[p.x][p.y][1] = board[x][y][1];
                    }
                }
                break;
            }
            else
            {
                if (board[x][y][0] == 0)
                {
                    board[x][y][0] = explosionTime;
                }
                else
                {
                    board[x][y][0] = Math.min(board[x][y][0], explosionTime);
                }
                range--;
            }
        }
    }

    /**
     * Checks if given cell is a safe place to stay. Somewhat opposite to
     * {@link #isBlocked}.
     * 
     * @param p Board cell position
     * @return <b>true</b> if player can move to that position, <b>false</b> otherwise
     */
    private boolean isSafe(Point p)
    {
        return (cells[p.x][p.y].type == CellType.CELL_BONUS_BOMB
            || cells[p.x][p.y].type == CellType.CELL_BONUS_RANGE || cells[p.x][p.y].type == CellType.CELL_EMPTY);
    }

    /**
     * Checks if a bomb is deployed in given cell.
     * 
     * @param p Board cell position
     * @return <b>true</b> if a bomb exists in given cell, <b>false</b> otherwise
     */
    private boolean isBomb(Point p)
    {
        return cells[p.x][p.y].type == CellType.CELL_BOMB;
    }

    /**
     * Checks if given cell is not accessible by the player. Somewhat opposite to
     * {@link #isSafe}.
     * 
     * @param p Board cell position
     * @return <b>true</b> if cell is blocked by a bomb, crate or wall <b>false</b>
     *         otherwise
     */
    private boolean isBlocked(Point p)
    {
        return (cells[p.x][p.y].type == CellType.CELL_BOMB
            || cells[p.x][p.y].type == CellType.CELL_CRATE
            || cells[p.x][p.y].type == CellType.CELL_CRATE_OUT || cells[p.x][p.y].type == CellType.CELL_WALL);
    }

    /**
     * Returns the range of player standing on a given cell. If more than one player is
     * standing on one cell, the returned value is the largest range of all those players.
     * 
     * @param p Board cell position
     * @return Maximum player range
     */
    private int getPlayerRange(Point p)
    {
        int maxRange = conf.DEFAULT_BOMB_RANGE;
        for (IPlayerSprite ips : players)
        {
            if (!ips.isDead() && getBoardPosition(ips.getPosition()).equals(p))
            {
                if (ranges != null && ranges.get(ips.getName()) != null 
                    && ranges.get(ips.getName()) > maxRange)
                {
                    maxRange = ranges.get(ips.getName());
                }
            }
        }
        return maxRange;
    }

    /**
     * Recalculates board coordinates from real (pixel) coordinates.
     * 
     * @param p
     * @return
     */
    private Point getBoardPosition(Point p)
    {
        return new Point(p.x / Constants.DEFAULT_CELL_SIZE, p.y / Constants.DEFAULT_CELL_SIZE);
    }

    /**
     * Prints out the bomb model array ~per each second (25 frames).
     */
    @SuppressWarnings("unused")
    private void debugBombs()
    {
        if (frame % 25 == 0)
        {
            for (int i = 0; i < board[0].length; i++)
            {
                String row = "";
                for (int j = 0; j < board.length; j++)
                {
                    if (cells[j][i].type == CellType.CELL_WALL
                        || cells[j][i].type == CellType.CELL_CRATE)
                    {
                        row += "X  ";
                    }
                    else
                    {
                        if (board[j][i][0] == F_BOMB)
                        {
                            row += "O  ";
                        }
                        else if (board[j][i][0] == 0)
                        {
                            row += "   ";
                        }
                        else
                        {
                            row += board[j][i][0];
                            if (board[j][i][0] < 10)
                            {
                                row += "  ";
                            }
                            else
                            {
                                row += " ";
                            }
                        }
                    }
                }
                logger.debug(row);
            }
            logger.debug("\n");
        }
    }

    /**
     * Prints out the board model array ~per each second (25 frames).
     */
    @SuppressWarnings("unused")
    private void debugCells()
    {
        if (frame % 25 == 0)
        {
            for (int i = 0; i < cells.length; i++)
            {
                String row = "";
                for (int j = 0; j < cells[i].length; j++)
                {
                    if (isBlocked(new Point(i, j)))
                    {
                        row += "X  ";
                    }
                    else if (isSafe(new Point(i, j)))
                    {
                        row += "   ";
                    }
                }
                logger.debug(row);
            }
        }
    }

    @Override
    public boolean dropsBomb()
    {
        return dropBombs;
    }

    @Override
    public Direction getCurrent()
    {
        return currDirection;
    }
}
