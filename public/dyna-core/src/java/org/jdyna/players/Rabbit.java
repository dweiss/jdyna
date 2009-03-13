package org.jdyna.players;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.jdyna.*;

import com.google.common.collect.Lists;

/**
 * Rabbits are shy and kind animals. A rabbit never drops any bombs, it just runs like
 * hell.
 */
public final class Rabbit implements IPlayerController, IPlayerController2,
    IGameEventListener
{
    /**
     * Distance measurement fuzziness.
     */
    private static final int FUZZINESS = Globals.DEFAULT_CELL_SIZE / 3;

    /** This player's name. */
    private String name;

    /** Target position we're aiming at, in pixels. */
    private Point target;

    /** Trail history length. */
    private static final int TRAIL_SIZE = 5;

    /** Short history of recently visited cells (trail). Positions in grid coords. */
    private ArrayList<Point> trail = new ArrayList<Point>(TRAIL_SIZE);

    /** */
    private Random rnd = new Random();

    /**
     * Cached board info.
     */
    private BoardInfo boardInfo;

    /**
     * 
     */
    private volatile Direction direction;

    /*
     *
     */
    public Rabbit(String name)
    {
        this.name = name;
    }

    /*
     * 
     */
    @Override
    public boolean dropsBomb()
    {
        return getState().dropsBomb;
    }

    /*
     * 
     */
    @Override
    public Direction getCurrent()
    {
        return getState().direction;
    }

    /*
     * 
     */
    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        /*
         * Slowdown rabbit if a system property is set.
         */
        try
        {
            final String rabbitSlowdown = System.getProperty("rabbit.slowdown");
            if (!StringUtils.isEmpty(rabbitSlowdown))
            {
                Thread.sleep(Integer.parseInt(rabbitSlowdown));
            }
        }
        catch (InterruptedException e)
        {
            // Ignore interrupts.
        }

        for (GameEvent event : events)
        {
            if (event.type == GameEvent.Type.GAME_START)
            {
                this.boardInfo = ((GameStartEvent) event).getBoardInfo();
                this.target = null;
                this.trail.clear();
                this.direction = null;
            }

            if (event.type == GameEvent.Type.GAME_STATE)
            {
                final GameStateEvent gse = (GameStateEvent) event;

                final IPlayerSprite myself = identifyMyself(gse.getPlayers());
                if (myself.isDead())
                {
                    target = null;
                }
                else
                {
                    final Point pixelPosition = myself.getPosition();
                    final Point gridPosition = boardInfo.pixelToGrid(pixelPosition);

                    updateTrail(gridPosition);
                    if (shouldChangeDirection(gse.getCells(), pixelPosition))
                    {
                        target = pickNewLocation(gse.getCells(), pixelPosition);
                        updateState(pixelPosition, target);
                    }
                }
            }
        }
    }

    /**
     * Update the trail history.
     */
    private void updateTrail(Point position)
    {
        if (position == null) return;
        if (trail.size() > 0 && position.equals(trail.get(0))) return;

        trail.add(0, position);
        while (trail.size() > TRAIL_SIZE)
            trail.remove(trail.size() - 1);
    }

    /**
     * Update controller state.
     */
    private void updateState(Point sourcePixels, Point targetPixels)
    {
        direction = null;

        if (target == null)
        {
            return;
        }

        final Point sourceXY = boardInfo.pixelToGrid(sourcePixels);
        final Point targetXY = boardInfo.pixelToGrid(targetPixels);

        if (sourceXY.x < targetXY.x) direction = Direction.RIGHT;
        else if (sourceXY.x > targetXY.x) direction = Direction.LEFT;
        else if (sourceXY.y < targetXY.y) direction = Direction.DOWN;
        else if (sourceXY.y > targetXY.y) direction = Direction.UP;
    }

    /**
     * Picks a new target location to go to.
     */
    private Point pickNewLocation(Cell [][] cells, Point ourPosition)
    {
        final Point currentCell = boardInfo.pixelToGrid(ourPosition);

        final ArrayList<Point> possibilities = Lists.newArrayListWithExpectedSize(4);
        addPossible(possibilities, cells, currentCell, 0, -1);
        addPossible(possibilities, cells, currentCell, -1, 0);
        addPossible(possibilities, cells, currentCell, 1, 0);
        addPossible(possibilities, cells, currentCell, 0, 1);

        if (possibilities.size() == 0) return null;

        /*
         * Avoid going back the same route, if possible.
         */
        if (trail.size() > 1)
        {
            for (int i = 0; possibilities.size() > 1 && i < trail.size(); i++)
            {
                final Point oneBefore = trail.get(i);
                possibilities.remove(oneBefore);
            }
        }

        return boardInfo
            .gridToPixel(possibilities.get(rnd.nextInt(possibilities.size())));
    }

    /**
     * Check possible target cell and add it to the list.
     */
    private void addPossible(List<Point> possibilities, Cell [][] cells, Point xy,
        int ox, int oy)
    {
        final Point t = new Point(xy.x + ox, xy.y + oy);
        if (boardInfo.isOnBoard(t))
        {
            final Cell cell = cells[t.x][t.y];
            if (cell.type.isWalkable() && !cell.type.isLethal())
            {
                possibilities.add(t);
            }
        }
    }

    /**
     * Check if we reached the target or if we should change direction now.
     */
    private boolean shouldChangeDirection(Cell [][] cells, Point ourPosition)
    {
        if (target == null) return true;

        final Point p = boardInfo.pixelToGrid(target);
        final CellType type = cells[p.x][p.y].type;
        if (!type.isWalkable() || type.isLethal())
        {
            return true;
        }
        return BoardUtilities.isClose(target, ourPosition, FUZZINESS);
    }

    /**
     * Determine this player in the player list.
     */
    private IPlayerSprite identifyMyself(List<? extends IPlayerSprite> players)
    {
        for (IPlayerSprite ps : players)
        {
            if (name.equals(ps.getName()))
            {
                return ps;
            }
        }
        throw new RuntimeException("Player not on the list of players: " + name);
    }

    /**
     * Create a named rabbit player.
     */
    public static Player createPlayer(String name)
    {
        return new Player(name, new Rabbit(name));
    }

    /*
     * 
     */
    @Override
    public ControllerState getState()
    {
        return new ControllerState(this.direction, false, 
            /* We go for at most 4 frames. */ 4);
    }
}
