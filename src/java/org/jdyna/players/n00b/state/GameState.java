package org.jdyna.players.n00b.state;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdyna.*;

import org.jdyna.players.n00b.state.Player.PlayerState;
import org.jdyna.players.n00b.state.Space.SpaceType;

/**
 * Constructs and maintains a detailed state of the game through an in-depth
 * frame-by-frame analysis of the board snapshots provided through events.
 */
public class GameState
{
    /**
     * Spaces present on the board.
     */
    private final Space [][] spaces;

    /**
     * Extra info about the board, used for pixel/grid calculations.
     */
    private final BoardInfo boardInfo;

    /**
     * Players currently taking part in the game.
     */
    private List<Player> players;
    private List<Player> playersRO;

    /**
     * The number of the last frame the state was updated on.
     */
    private int lastFrame;

    /**
     * Game settings and configuration.
     */
    private Globals conf;

    public GameState(GameStartEvent event)
    {
        this.conf = event.getConfiguration();
        this.boardInfo = event.getBoardInfo();
        spaces = new Space [boardInfo.gridSize.width] [boardInfo.gridSize.height];
        for (int i = 0; i < spaces.length; ++i)
        {
            for (int j = 0; j < spaces[i].length; ++j)
            {
                spaces[i][j] = new Space(i, j);
            }
        }
        players = new ArrayList<Player>();
        playersRO = Collections.unmodifiableList(players);
        lastFrame = -2;
    }

    /**
     * Returns the height of the board expressed in the number of spaces.
     */
    public int getBoardHeight()
    {
        return boardInfo.gridSize.height;
    }

    /**
     * Returns the width of the board expressed in the number of spaces.
     */
    public int getBoardWidth()
    {
        return boardInfo.gridSize.width;
    }

    /**
     * Returns an unmodifiable list of players.
     * 
     * @return
     */
    public List<Player> getPlayers()
    {
        return playersRO;
    }

    /**
     * Returns the player with the given name.
     * 
     * @return found player, or <code>null</code> if no player with the given name exists
     */
    public Player getPlayer(String name)
    {
        if (players == null || players.size() == 0)
        {
            return null;
        }
        for (Player player : players)
        {
            if (player.getName().equals(name))
            {
                return player;
            }
        }
        return null;
    }

    /**
     * Returns the space the given player is currently standing on.
     */
    public Space getPlayerSpace(Player player)
    {
        final Point p = boardInfo.pixelToGrid(player.getPosition());
        return spaces[p.x][p.y];

    }

    /**
     * Checks whether the given point lies outside the board.
     * 
     * @param p grid coordinates
     */
    public boolean isPointOutsideBoard(Point p)
    {
        if (p.x < 0 || p.y < 0 || p.x >= spaces.length || p.y >= spaces[0].length)
        {
            return true;
        }
        return false;
    }

    /**
     * Returns the space at the given coordinates.
     * 
     * @param p grid coordinates
     */
    public Space spaceAt(Point p)
    {
        return spaces[p.x][p.y];
    }

    /**
     * Returns the space at the given coordinates.
     * 
     * @param x horizontal grid coordinate (top->down)
     * @param y vertical grid coordinate (left->right)
     */
    public Space spaceAt(int x, int y)
    {
        return spaces[x][y];
    }

    /**
     * Returns the grid coordinates of the given point expressed in pixels.
     * 
     * @param p pixel coordinates
     * @return grid coordinates
     */
    public Point pixelToGrid(Point p)
    {
        return boardInfo.pixelToGrid(p);
    }

    /**
     * Updates the state according to the provided event data.
     */
    public void update(int currentFrame, GameStateEvent event)
    {
        final int frameDifference = currentFrame - lastFrame;
        if (frameDifference < 1)
        {
            // we're already past that frame, skip it
            return;
        }
        lastFrame = currentFrame;

        // players first
        if (players.size() == 0)
        {
            for (IPlayerSprite pl : event.getPlayers())
            {
                players.add(new Player(conf, pl.getName(), pl.getPosition()));
            }
        }
        else
        {
            for (IPlayerSprite pl : event.getPlayers())
            {
                boolean found = false;
                for (Player player : players)
                {
                    if (player.getName().equals(pl.getName()))
                    {
                        player.setPosition(pl.getPosition());
                        if (pl.isDead())
                        {
                            player.setState(PlayerState.PWNED);
                        }
                        else if (pl.isImmortal())
                        {
                            player.setState(PlayerState.CHEATING);
                        }
                        else
                        {
                            player.setState(PlayerState.PWNING);
                        }
                        found = true;
                        break;
                    }
                }
                if (!found)
                {
                    players.add(new Player(conf, pl.getName(), pl.getPosition()));
                }
            }
        }

        // timers next
        for (int i = 0; i < spaces.length; ++i)
        {
            for (int j = 0; j < spaces[i].length; ++j)
            {
                final Space space = spaces[i][j];

                if (space.getBomb() != null)
                {
                    space.getBomb()
                        .setTimer(space.getBomb().getTimer() - frameDifference);
                }

                final List<Integer> timers = space.getTimers();
                for (int k = 0; k < timers.size(); ++k)
                {
                    final Integer timer = timers.get(k) - frameDifference;
                    if (timer <= -Bomb.EXPLOSION_DURATION)
                    {
                        timers.remove(k);
                        k--;
                    }
                    else
                    {
                        timers.set(k, timer);
                    }
                }
            }
        }

        // solid spaces (walls, crates)
        for (int i = 0; i < spaces.length; ++i)
        {
            for (int j = 0; j < spaces[i].length; ++j)
            {
                final Space space = spaces[i][j];
                final Cell cell = event.getCells()[i][j];

                if (cell.type == CellType.CELL_WALL)
                {
                    space.setType(SpaceType.WALL);
                }
                else if (cell.type == CellType.CELL_CRATE)
                {
                    space.setType(SpaceType.CRATE);
                }
            }
        }

        // other spaces
        for (int i = 0; i < spaces.length; ++i)
        {
            for (int j = 0; j < spaces[i].length; ++j)
            {
                final Space space = spaces[i][j];
                final Cell cell = event.getCells()[i][j];

                // check power-ups
                if ((space.getType() == SpaceType.BONUS_BOMB && cell.type != CellType.CELL_BONUS_BOMB)
                    || (space.getType() == SpaceType.BONUS_RANGE && cell.type != CellType.CELL_BONUS_RANGE))
                {
                    if (!cell.type.isExplosion())
                    {
                        // check who picked it up
                        final Player player = findClosestPlayer(space);
                        if (player != null)
                        {
                            if (space.getType() == SpaceType.BONUS_BOMB)
                            {
                                player.collectBomb();
                            }
                            else
                            {
                                player.increaseRange();
                            }
                        }
                    }
                }

                // update spaces
                if (cell.type == CellType.CELL_WALL || cell.type == CellType.CELL_CRATE)
                {
                    // solid spaces already taken care of
                    continue;
                }
                else if (cell.type == CellType.CELL_BOMB)
                {
                    if (space.getType() != SpaceType.BOMB)
                    {
                        // check whose bomb it is
                        final Player owner = findClosestPlayer(space);

                        // plant it
                        dropBomb(owner, space, frameDifference - 1);
                    }
                }
                else if (cell.type.isExplosion() || cell.type == CellType.CELL_CRATE_OUT)
                {
                    if (space.isSafe())
                    {
                        // our data is wrong - add an extra timer to correct it
                        space.addTimer(-cell.counter);

                        // if there was a bomb here, it's gone by now
                        if (space.getType() == SpaceType.BOMB)
                        {
                            space.getBomb().disarm();
                            space.setBomb(null);
                        }
                    }
                    else
                    {
                        // detonation right on time
                        if (space.getType() == SpaceType.BOMB)
                        {
                            space.getBomb().detonate();
                            space.setBomb(null);
                        }
                    }
                    space.setType(SpaceType.CLEAR);
                }
                else
                {
                    if (space.getType() == SpaceType.BOMB)
                    {
                        // we have a bomb that didn't go off? oops
                        space.getBomb().disarm();
                        space.setBomb(null);
                    }
                    if (cell.type == CellType.CELL_BONUS_BOMB)
                    {
                        space.setType(SpaceType.BONUS_BOMB);
                    }
                    else if (cell.type == CellType.CELL_BONUS_RANGE)
                    {
                        space.setType(SpaceType.BONUS_RANGE);
                    }
                    else
                    {
                        space.setType(SpaceType.CLEAR);
                    }
                }
            }
        }
    }

    /**
     * Finds the living player that is closest to the given space.
     * 
     * @return found player, or <code>null</code> if all players are either dead or
     *         immortal
     */
    private Player findClosestPlayer(Space space)
    {
        if (players == null || players.size() == 0)
        {
            return null;
        }

        final Point center = boardInfo.gridToPixel(new Point(space.x, space.y));
        Double minDistance = null;
        Player closestPlayer = null;

        for (Player player : players)
        {
            if (player.getState() == PlayerState.PWNING && player.getPosition() != null)
            {
                final Point playerGridPosition = boardInfo.pixelToGrid(player
                    .getPosition());
                double distance;

                if (spaces[playerGridPosition.x][playerGridPosition.y] == space)
                {
                    distance = Math.max(Math.abs(center.x - player.getPosition().x), Math
                        .abs(center.y - player.getPosition().y));
                }
                else
                {
                    distance = center.distance(player.getPosition());
                }
                if (minDistance == null || distance < minDistance)
                {
                    minDistance = distance;
                    closestPlayer = player;
                }
            }
        }

        return closestPlayer;
    }

    /**
     * Plants a bomb at the given space.
     * <p>
     * This method automatically calculates the range, estimated time of detonation and
     * spaces that will be affected by this bomb's explosion. If any of those spaces
     * contain a bomb scheduled to detonate later than this bomb, that bomb is disarmed
     * and replanted to ensure proper explosion estimation.
     * 
     * @param owner player who plants the bomb
     * @param location space the bomb is being planted on
     * @param compensation number of frames lost between this frame and the last frame
     *            received
     */
    private void dropBomb(Player owner, Space location, int compensation)
    {
        final Bomb bomb = new Bomb(owner, conf);
        final List<Space> affectedSpaces = new ArrayList<Space>();

        // check if this bomb will be prematurely detonated by another explosion
        Integer detonationTime = location.getNextExplosion();
        if (detonationTime == null)
        {
            // no explosions found, use the default detonation time
            detonationTime = conf.DEFAULT_FUSE_FRAMES + 1 - compensation;
        }
        bomb.setTimer(detonationTime);

        // get all the spaces affected by this bomb's explosion
        affectedSpaces.add(location);
        affectedSpaces.addAll(propagateExplosion(location.x + 1, location.y, location.x
            + bomb.range, location.y, detonationTime));
        affectedSpaces.addAll(propagateExplosion(location.x - 1, location.y, location.x
            - bomb.range, location.y, detonationTime));
        affectedSpaces.addAll(propagateExplosion(location.x, location.y + 1, location.x,
            location.y + bomb.range, detonationTime));
        affectedSpaces.addAll(propagateExplosion(location.x, location.y - 1, location.x,
            location.y - bomb.range, detonationTime));
        bomb.setAffectedSpaces(affectedSpaces);

        // add timers
        for (Space space : affectedSpaces)
        {
            space.addTimer(detonationTime);

            final Bomb currentBomb = space.getBomb();
            if (currentBomb != null && currentBomb.getTimer() > detonationTime)
            {
                // replace the current bomb with a new one
                currentBomb.disarm();
                space.setBomb(null);
                dropBomb(currentBomb.owner, space, 0);
            }
        }

        // plant the bomb
        location.setBomb(bomb);
    }

    /**
     * Propagates the explosion from <code>Space (x1, y1)</code> to <code>(x2, y2)</code>.
     * 
     * @param timer time left until the explosion
     * @return a list of spaces affected by the explosion
     */
    private List<Space> propagateExplosion(int x1, int y1, int x2, int y2, int timer)
    {
        final List<Space> affected = new ArrayList<Space>();
        final int xd = x1 - x2 > 0 ? -1 : 1;
        final int yd = y1 - y2 > 0 ? -1 : 1;

        for (int i = x1; xd * i <= xd * x2; i += xd)
        {
            for (int j = y1; yd * j <= yd * y2; j += yd)
            {

                if (!boardInfo.isOnBoard(new Point(i, j)))
                {
                    return affected;
                }

                final Space inRange = spaces[i][j];
                if (inRange.getType() == SpaceType.WALL)
                {
                    // explosions don't go through walls
                    return affected;
                }

                affected.add(inRange);

                if (inRange.getType() == SpaceType.CRATE
                    && (inRange.getNextExplosion() == null || inRange.getNextExplosion() >= timer))
                {
                    // explosions stop at the first crate
                    return affected;
                }
            }
        }

        return affected;
    }

}
