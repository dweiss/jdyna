package com.komwalczyk.dyna.ai.Killer2;

import java.awt.Point;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.BoardUtilities;
import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameStartEvent;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerSprite;

/**
 * Backbone class for Killer AI.
 */
public class KillerAI implements IPlayerController, IGameEventListener
{
    /**
     * Class used by path finder.
     */
    private class Node
    {
        public Point point;
        public Node before;

        public Node(Point point, Node before)
        {
            this.point = point;
            this.before = before;
        }

        @Override
        public boolean equals(Object obj)
        {
            return point.equals(obj);
        }

        @Override
        public int hashCode()
        {
            return point.hashCode();
        }
    }

    /**
     * State in which Killer is - SAFE or DANGER.
     */
    enum State
    { /* no bombs in nearby */
        SAFE, /* bombs in nearby but we are save at our place - not used */
        UNSAFETY, /*
                   * bomb wants to kill us !
                   */
        DANGER
    }

    private final static Logger logger = LoggerFactory.getLogger(KillerAI.class);
    /**
     * Determines how long before explosion we still count bomb as safe.
     */
    private final static int SAFE_FUSE = 20;
    private static final int SAFE_EXIT = 2;

    private String myName;
    private State currentState;
    private Cell [][] currentBoard;
    private BoardInfo boardInfo;
    private Direction current;
    private Bonuses bonuses;
    private PlayersBomb bombs;
    private Players players;

    private boolean dropsBombs;

    /**
     * our last frame
     */
    private int lastFrame;

    /**
     * Number of frames between current frame and last frame.
     */
    private int frameStep;

    /**
     * If true our bomberman does nothing - debug purposes.
     */
    private boolean collectorOnly = false;

    public KillerAI(String name)
    {
        myName = name;
        bonuses = new Bonuses();
        players = new Players();
        bombs = new PlayersBomb();

    }

    /**
     * Adjusts counter of new bomb - sets fuse counter of min from nearby bombs. Then it
     * musts adjust counter of nearby bombs.
     * 
     * @param bomb bomb to adjust counter.
     */
    private void adjustCounter(Bomb bomb)
    {

        Set<Bomb> bombsToCheck = new HashSet<Bomb>();
        boolean adjusted = false;
        for (Bomb nearBomb : bombs.getBombs())
        {
            if (bomb == nearBomb)
            {
                logger.info("Same bomb continuing");
                continue;
            }
            if (isInRangeOfBomb(nearBomb, bomb.getPosition()))
            {
                bombsToCheck.add(nearBomb);
                if (nearBomb.getFuseCounter() < bomb.getFuseCounter())
                {
                    logger.info("Counter set to: " + nearBomb.getFuseCounter());
                    bomb.setFuseCounter(nearBomb.getFuseCounter());
                    adjusted = true;
                }
            }
        }
        if (adjusted)
        {
            for (Bomb bombToCheck : bombsToCheck)
            {
                adjustCounter(bombToCheck);
            }
        }
    }

    /**
     * Checks if given points are in one line and closer than given value.
     * 
     * @param p1
     * @param p2
     * @return true if points are in one line (x1 == x2 or y1 == y2) and equal or cloesr
     *         than given value.
     */
    private boolean areInOneLineFurtherThan(Point p1, Point p2, int min)
    {
        return (p1.x == p2.x && Math.abs(p1.y - p2.y) <= min)
            || (p1.y == p2.y && Math.abs(p1.x - p2.x) <= min);
    }

    /**
     * Counts number of safe points around the grid point.
     * 
     * @param p point.
     * @return number of safe cells.
     */
    private int countSafe(Point p)
    {
        int count = 0;
        if (isSafePoint(new Point(p.x + 1, p.y), true))
        {
            count++;
        }
        if (isSafePoint(new Point(p.x - 1, p.y), true))
        {
            count++;
        }
        if (isSafePoint(new Point(p.x, p.y + 1), true))
        {
            count++;
        }
        if (isSafePoint(new Point(p.x, p.y - 1), true))
        {
            count++;
        }
        return count;
    }

    @Override
    public boolean dropsBomb()
    {
        if (collectorOnly)
        {
            return false;
        }

        return dropsBombs;
    }

    /**
     * If enemy is null finds closest point and returns direction leading to it. Else
     * finds way to enemy and return direction
     * 
     * @param point point from which we seek path to safe spot
     * @param enemy can be null - if null looking for safe spot
     * @return direction
     */
    private Direction findPathToClosestEnemy(Point me, Point enemy)
    {
        Queue<Node> openNodes = new LinkedList<Node>();
        Set<Point> closeNodes = new HashSet<Point>();

        openNodes.add(new Node(me, null));
        Node temp;
        while ((temp = openNodes.poll()) != null)
        {
            Point grid = temp.point;
            if (enemy == null && isSafePoint(grid, true))
            {
                // logger.debug("punkt " + grid + " is safe");
                return getDirectionFromNodes(me, temp);
            }
            else if (grid.equals(enemy))
            {
                // logger.debug("znaleziono sciezke do closetst enemy");
                return getDirectionFromNodes(me, temp);
            }
            else
            {
                boolean canRisk = enemy == null ? true : false;
                // TODO some helper code to check possibliet
                if (grid.x + 1 < boardInfo.gridSize.width
                    && currentBoard[grid.x + 1][grid.y].type.isWalkable()
                    && !currentBoard[grid.x + 1][grid.y].type.isExplosion()
                    && (canRisk || isSafePoint(new Point(grid.x + 1, grid.y), true)))
                {
                    Node node = new Node(new Point(grid.x + 1, grid.y), temp);
                    if (!closeNodes.contains(node)) openNodes.add(node);
                }
                if (grid.x - 1 >= 0 && currentBoard[grid.x - 1][grid.y].type.isWalkable()
                    && !currentBoard[grid.x - 1][grid.y].type.isExplosion()
                    && (canRisk || isSafePoint(new Point(grid.x - 1, grid.y), true)))
                {
                    Node node = new Node(new Point(grid.x - 1, grid.y), temp);
                    if (!closeNodes.contains(node)) openNodes.add(node);
                }
                if (grid.y + 1 < boardInfo.gridSize.height
                    && currentBoard[grid.x][grid.y + 1].type.isWalkable()
                    && !currentBoard[grid.x][grid.y + 1].type.isExplosion()
                    && (canRisk || isSafePoint(new Point(grid.x, grid.y + 1), true)))
                {
                    Node node = new Node(new Point(grid.x, grid.y + 1), temp);
                    if (!closeNodes.contains(node)) openNodes.add(node);
                }
                if (grid.y - 1 >= 0 && currentBoard[grid.x][grid.y - 1].type.isWalkable()
                    && !currentBoard[grid.x][grid.y - 1].type.isExplosion()
                    && (canRisk || isSafePoint(new Point(grid.x, grid.y - 1), true)))
                {
                    Node node = new Node(new Point(grid.x, grid.y - 1), temp);
                    if (!closeNodes.contains(node)) openNodes.add(node);
                }
            }
            closeNodes.add(temp.point);
        }
        logger.debug("no safe path to closes enemy");
        return null;
    }

    /**
     * Finishes the game.
     */
    public void finishGame()
    {
    }

    /**
     * Get closest point from given set.
     * 
     * @param set set to look closest point
     * @param start point
     * @return closest poitn
     */
    private Point getClosestPoint(Set<Point> set, Point start)
    {
        Point min = new Point(0, 0);
        int minDist = Integer.MAX_VALUE;
        for (Point point : set)
        {
            int dist = BoardUtilities.manhattanDistance(point, min);
            if (dist < minDist)
            {
                min = point;
                minDist = dist;
            }
        }
        return min;
    }

    @Override
    public Direction getCurrent()
    {
        if (collectorOnly)
        {
            return null;
        }
        return current;
    }

    /**
     * Returns in which direction we should go to reach desired node.
     * 
     * @param start start point
     * @param temp node to go.
     * @return direction
     */
    private Direction getDirectionFromNodes(Point start, Node temp)
    {
        Node node = temp;
        if (!(node == null || node.before == null))
        {
            while (node.before.before != null)
            {
                node = node.before;
            }
        }

        // logger.debug("Starting point: " + start + " next piont: " + node.point);
        Point next = node.point;
        if (start.x == next.x)
        {
            if (start.y > next.y)
            {
                return Direction.UP;
            }
            else
            {
                return Direction.DOWN;
            }
        }
        else if (start.y == next.y)
        {
            if (start.x > next.x)
            {
                return Direction.LEFT;
            }
            else
            {
                return Direction.RIGHT;
            }
        }
        else
        {
            logger.debug("SHouldn't happen both x and y are different!!!");
        }
        logger.debug("unreachable");
        return null;
    }

    /**
     * Checks if given point is in range of bomb (obstacles cannot be in way).
     * 
     * @param bomb bomb
     * @param point point
     * @return true if point is in range and no obstacles
     */
    private boolean isInRangeOfBomb(Bomb bomb, Point point)
    {
        return areInOneLineFurtherThan(bomb.getPosition(), point, bomb.getRange())
            && !isObstacleBetweenPoints(bomb.getPosition(), point);
    }

    /**
     * Checks if between two points are obstacles. Points must be in same line.
     * 
     * @param p1 p1
     * @param p2p2
     * @return true if obstacles between point
     */
    private boolean isObstacleBetweenPoints(Point p1, Point p2)
    {
        Point dxy = p1.x == p2.x ? /* on x */(p1.y < p2.y ? new Point(0, 1) : new Point(
            0, -1)) : /* on y */(p1.x < p2.x ? new Point(1, 0) : new Point(-1, 0));
        int steps = Math.max(Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y)) - 1;

        for (int i = 1; i <= steps; i++)
        {
            int dx = dxy.x * i;
            int dy = dxy.y * i;
            CellType cell = currentBoard[p1.x + dx][p1.y + dy].type;
            if (cell == CellType.CELL_WALL || cell == CellType.CELL_BOMB)
            {
                // logger.info("On point: " + new Point(p1.x + dx, p1.y + dy) +
                // " has obstacle: " + cell);
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates is given point is perfectly safe (not in range of bomb or bomb fuse
     * counter high).
     * 
     * @param position point to check
     * @param isGrid - flag telling if actual point is grid point or pixel
     * @return true if point is safe
     */
    private boolean isSafePoint(Point position, boolean isGrid)
    {
        Point grid;
        if (!isGrid)
        {
            grid = boardInfo.pixelToGrid(position);
        }
        else
        {
            grid = position;
        }
        if (grid.x < 0 || grid.x >= boardInfo.gridSize.width || grid.y < 0
            || grid.y >= boardInfo.gridSize.height)
        {
            return false; // out of boundss
        }
        if (!currentBoard[grid.x][grid.y].type.isWalkable()
            || currentBoard[grid.x][grid.y].type.isLethal()
            || currentBoard[grid.x][grid.y].type.isExplosion()
            || currentBoard[grid.x][grid.y].type == CellType.CELL_BOMB)
        {
            // logger.debug("Not safe!! point is lethat explosion or bomb");
            return false;
        }
        for (Bomb bomb : bombs.getBombs())
        {
            // if (areInOneLineFurtherThan(grid, bomb.getPosition(), bomb.getRange()))
            if (bomb.getFuseCounter() < SAFE_FUSE && isInRangeOfBomb(bomb, grid))
            {
                // logger.debug("Point: " + grid + " is not safe!!");
                return false;
            }
        }
        return true;
    }

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        for (GameEvent event : events)
        {
            if (event.type == GameEvent.Type.GAME_STATE)
            {
                // killer.calculate((GameStateEvent) event);
                GameStateEvent gse = (GameStateEvent) event;
                setCurrentBoard(gse.getCells());
                setPlayers(gse.getPlayers());
                process(frame);
            }
            else if (event.type == GameEvent.Type.GAME_START)
            {
                startGame((GameStartEvent) event, frame);
            }
            else if (event.type == GameEvent.Type.GAME_OVER)
            {
                finishGame();
            }
        }
    }

    public void process(int frame)
    {
        update(frame);
        currentState = recalcuteState();
        if (currentState == State.SAFE)
        {
            // Player me =
            if (players.getPlayer(myName) != null)
            {
                Point goal = null;
                if (bonuses.getBonuses().size() > 0)
                {
                    goal = getClosestPoint(bonuses.getBonuses(), boardInfo
                        .pixelToGrid(players.getPlayer(myName).getPosition()));
                }
                else
                {
                    Player close = players.getClosest(myName);
                    if (close != null)
                    {
                        goal = boardInfo.pixelToGrid(close.getPosition());
                    }
                }
                current = findPathToClosestEnemy(boardInfo.pixelToGrid(players.getPlayer(
                    myName).getPosition()), goal);
            }
        }

        else
        {
            current = findPathToClosestEnemy(boardInfo.pixelToGrid(players.getPlayer(
                myName).getPosition()), null);
        }
        recalculateBoardState();
        updateDropsBomb();
    }

    /**
     * Calculates all new info (new bombs and players and bonuses).
     */
    private void recalculateBoardState()
    {
        for (int x = 0; x < currentBoard.length; x++)
        {
            for (int y = 0; y < currentBoard[x].length; y++)
            {
                Point p = new Point(x, y);
                if (currentBoard[x][y].type == CellType.CELL_BOMB)
                {
                    Bomb bomb = bombs.getBomb(p);
                    if (bomb != null)
                    {
                        if (bomb.decreaseFuseCounter(frameStep))
                        {
                            bomb.blow();
                            bombs.removeBomb(p);

                        }
                    }
                }
                else if (currentBoard[x][y].type == CellType.CELL_BONUS_BOMB
                    || currentBoard[x][y].type == CellType.CELL_BONUS_RANGE)
                {
                    bonuses.addBonus(p, currentBoard[x][y].type);
                }
            }
        }
        // the second iteration over board need to be performed because we need to know
        // proper status of all bombs on board before setting new ones
        for (int x = 0; x < currentBoard.length; x++)
        {
            for (int y = 0; y < currentBoard[x].length; y++)
            {
                Point p = new Point(x, y);
                if (currentBoard[x][y].type == CellType.CELL_BOMB)
                {
                    Bomb bomb = bombs.getBomb(p);
                    if (bomb == null)
                    {
                        Player player = players.getPlayer(p);
                        if (player != null)
                        {
                            bomb = new Bomb(p, player);
                            player.decreaseBomb();
                            bombs.putBomb(p, bomb);
                            adjustCounter(bomb);
                        }
                    }

                }
            }
        }
        players.checkForBonuses(bonuses);

    }

    /**
     * Recalculate current state. If there is bomb in nearby and its close to blow we are
     * in danger and need to run away.
     * 
     * @return state
     */
    private State recalcuteState()
    {
        if (currentBoard == null)
        {// it shouldn't happen
            logger.error("No board given. Exiting.");
            throw new RuntimeException("No board info");
        }
        if (players.getPlayer(myName) != null
            && !isSafePoint(players.getPlayer(myName).getPosition(), false))
        {
            return State.DANGER;
        }
        return State.SAFE;
    }

    public void setCurrentBoard(Cell [][] currentBoard)
    {
        this.currentBoard = currentBoard;
    }

    public void setPlayers(List<? extends IPlayerSprite> players)
    {
        this.players.updatePlayers(players, boardInfo);
    }

    /**
     * Start the game! Initialize all necessary fields. Sets start frame.
     * 
     * @param event start game event
     * @param frame should be 0 but better be safe than sorry
     */
    public void startGame(GameStartEvent event, int frame)
    {
        boardInfo = event.getBoardInfo();
        lastFrame = frame;
    }

    /**
     * Marks new frame. We need to recalculate all info.
     * 
     * @param frame
     */
    public void update(int frame)
    {
        frameStep = frame - lastFrame;
        lastFrame = frame;
    }

    private void updateDropsBomb()
    {
        Player player = players.getClosest(myName);
        Player me = players.getPlayer(myName);

        if (player != null && me != null)// we are asking about drop bomb state before
        // first event!!!
        {
            for (Bomb bomb : bombs.getBombs())
            {
                if (isInRangeOfBomb(bomb, boardInfo.pixelToGrid(me.getPosition()))
                    && bomb.getFuseCounter() < SAFE_FUSE)// dont drop bomb if we are close
                // to another one and it will
                // blow soon
                {
                    dropsBombs = false;
                }
                else if (countSafe(boardInfo.pixelToGrid(me.getPosition())) <= SAFE_EXIT)// don
                // 't
                // drop
                // bomb
                // if
                // there
                // is
                // only
                // one
                // safe
                // neighbour
                {
                    dropsBombs = false;
                }
            }
            int distance = BoardUtilities.manhattanDistance(boardInfo.pixelToGrid(player
                .getPosition()), boardInfo.pixelToGrid(me.getPosition()));
            if (distance < me.getRange()) // drop bomb if closest enemy is closer than our
            // range even if there are obstacles
            {
                dropsBombs = true;
            }
        }
    }

}
