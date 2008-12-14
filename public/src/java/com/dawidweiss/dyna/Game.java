package com.dawidweiss.dyna;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.dawidweiss.dyna.IPlayerController.Direction;
import com.dawidweiss.dyna.view.IBoardSnapshot;
import com.dawidweiss.dyna.view.IPlayerSprite;
import com.google.common.collect.Lists;

/**
 * Game controller. The controller and <b>all the objects involved in the game</b> are
 * single-threaded and should be accessed from within the game loop only.
 */
public final class Game
{
    /* */
    public final Board board;

    /**
     * Static player information. 
     */
    private Player [] players;

    /**
     * Dynamic information about players involved in the game.
     */
    private PlayerInfo [] playerInfos;
    
    /**
     * Player views for listeners.
     */
    private IPlayerSprite [] playerViews;

    /**
     * A list of killed players. 
     */
    private final ArrayList<Standing> standings = Lists.newArrayList();

    /** Single frame delay, in milliseconds. */
    private int framePeriod;

    /** Timestamp of the last frame's start. */
    private long lastFrameTimestamp;

    /** Game listeners. */
    private final ArrayList<IGameListener> listeners = Lists.newArrayList();

    /** Board dimensions. */
    private BoardInfo boardData;

    /**
     * How many frames to 'linger' after the game is over.
     */
    private int lingerFrames = Globals.DEFAULT_LINGER_FRAMES;

    /**
     * Static board view for listeners. 
     */
    private IBoardSnapshot boardSnapshot = new IBoardSnapshot() {
        public Cell [][] getCells()
        {
            /*
             * This should be a 'safe' copy of the actual board cell structure,
             * even within the same VM, so that nobody can cheat.
             */
            return board.cells;
        }

        public IPlayerSprite [] getPlayers()
        {
            return playerViews;
        }
    };

    /**
     * Creates a single game.
     */
    public Game(Board board, BoardInfo boardInfo, Player... players)
    {
        this.board = board;
        this.boardData = boardInfo;
        this.players = players;
    }

    /**
     * Starts the main game loop and runs the whole thing.
     */
    public GameResult run()
    {
        setupPlayers();

        int frame = 0;
        GameResult result = null;
        do
        {
            waitForFrame();
            processBoardCells();
            processPlayers();

            fireNextFrameEvent(frame);
            frame++;

            if (result == null)
            {
                result = checkGameOver();
            }
        } while (result == null || lingerFrames-- > 0);

        return result;
    }

    /**
     * Check game over conditions.
     */
    private GameResult checkGameOver()
    {
        Player winner = null;
        int dead = 0;
        int alive = 0;
        for (PlayerInfo pi : playerInfos)
        {
            if (pi.isDead())
            {
                dead++;
            }
            else
            {
                alive++;
                winner = pi.player;
            }
        }
        final int all = playerInfos.length;

        /*
         * There is one alive player, everyone else is dead. 
         */
        if (alive == 1 && dead == all - 1)
        {
            standings.add(0, new Standing(winner, 0));
            return new GameResult(winner, standings);
        }

        /*
         * Everyone is dead (draw).
         */
        if (dead == all)
        {
            return new GameResult(null, standings);
        }

        return null;
    }

    /**
     * Set the frame rate. Zero means no delays.
     */
    public void setFrameRate(double framesPerSecond)
    {
        framePeriod = (framesPerSecond == 0 ? 0 : (int) (1000 / framesPerSecond));
    }

    /*
     * 
     */
    public void addListener(IGameListener listener)
    {
        listeners.add(listener);
    }

    /*
     * 
     */
    public void removeListener(IGameListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * Fire "next frame" event to listeners.
     */
    private void fireNextFrameEvent(int frame)
    {
        for (IGameListener gl : listeners)
        {
            gl.onNextFrame(frame, boardSnapshot);
        }
    }

    /**
     * Move players according to their controller signals, drop bombs,
     * check collisions.
     */
    private void processPlayers()
    {
        final ArrayList<PlayerInfo> killed = Lists.newArrayList();

        for (int i = 0; i < players.length; i++)
        {
            /*
             * Process controller direction signals.
             */
            final PlayerInfo pi = playerInfos[i];
            final IPlayerController c = players[i].controller;

            final IPlayerController.Direction signal = c.getCurrent();
            pi.updateState(signal);

            /*
             * Dead can't dance.
             */
            if (pi.isDead())
            {
                continue;
            }

            if (signal != null)
            {
                movePlayer(pi, signal);
            }

            if (c.dropsBomb() && pi.bombCount > 0)
            {
                dropBomb(pi);
            }

            /*
             * check collisions against bombs and other active cells.
             */
            checkCollisions(killed, pi);
        }
        
        /*
         * Update standings. Assign <b>the same</b> victim number to all the people
         * killed in the same round.
         */
        if (killed.size() > 0)
        {
            final int victimNumber = players.length - standings.size() - 1;
            for (PlayerInfo pi : killed)
            {
                standings.add(0, new Standing(pi.player, victimNumber));
            }
        }
    }

    /**
     * Check collisions against bombs and other active cells.
     */
    private void checkCollisions(List<PlayerInfo> kills, PlayerInfo pi)
    {
        /*
         * Check collisions against grid cells. We only care about the cell directly 
         * under the player.
         */
        final Point xy = boardData.pixelToGrid(pi.location);
        final Cell c = board.cellAt(xy);
        if (CellType.isLethal(c.type))
        {
            // For whom the bell tolls...
            pi.kill();
            kills.add(pi);
        }
    }

    /**
     * Attempt to drop a bomb at the given location (if the player has any bombs left
     * and the cell under its feet is empty).
     */
    private void dropBomb(PlayerInfo pi)
    {
        final Point xy = boardData.pixelToGrid(pi.location);
        if (board.cellAt(xy).type == CellType.CELL_EMPTY && pi.bombCount > 0)
        {
            pi.bombCount--;

            final BombCell bomb = (BombCell) Cell.getInstance(CellType.CELL_BOMB);
            bomb.player = pi;
            bomb.range = pi.bombRange;
            board.cells[xy.x][xy.y] = bomb;
        }
    }

    /**
     * The movement-constraint code below has been engineered by trial and error by
     * looking at the behavior of the original Dyna Blaster game. The basic logic is that
     * we attempt to figure out the "target" cell towards which the player is heading and
     * "guide" the player's coordinates towards the target. This way there is a
     * possibility to run on diagonals (crosscut along the edge of a cell).
     */
    private void movePlayer(PlayerInfo pi, Direction signal)
    {
        final Point xy = boardData.pixelToGrid(pi.location);
        final Point txy;

        switch (signal)
        {
            case LEFT:
                txy = new Point(xy.x - 1, xy.y);
                break;
            case RIGHT:
                txy = new Point(xy.x + 1, xy.y);
                break;
            case UP:
                txy = new Point(xy.x, xy.y - 1);
                break;
            case DOWN:
                txy = new Point(xy.x, xy.y + 1);
                break;
            default:
                throw new RuntimeException(/* Unreachable. */);
        }

        final Point p = boardData.gridToPixel(txy);

        // Relative distance between the target cell and current position.
        final int rx = p.x - pi.location.x;
        final int ry = p.y - pi.location.y;

        // Steps towards the target.
        int dx = (rx < 0 ? -1 : 1) * min(pi.speed.x, abs(rx));
        int dy = (ry < 0 ? -1 : 1) * min(pi.speed.y, abs(ry));

        if (max(abs(rx), abs(ry)) <= boardData.cellSize)
        {
            if (!canWalkOn(pi, txy))
            {
                /*
                 * We try to perform 'easing', that is moving
                 * the player towards the cell from which he or she will
                 * be able to move further.
                 */
                final Point offset = boardData.pixelToGridOffset(pi.location); 

                final boolean easingApplied;
                switch (signal)
                {
                    case LEFT:
                        easingApplied = ease(pi, xy, offset.y, 
                            0, 1, -1, 1, Direction.DOWN, 0, -1, -1, -1, Direction.UP);
                        break;

                    case RIGHT:
                        easingApplied = ease(pi, xy, offset.y, 
                            0, 1, 1, 1, Direction.DOWN, 0, -1, 1, -1, Direction.UP);
                        break;

                    case DOWN:
                        easingApplied = ease(pi, xy, offset.x, 
                            1, 0, 1, 1, Direction.RIGHT, -1, 0, -1, 1, Direction.LEFT);
                        break;

                    case UP:
                        easingApplied = ease(pi, xy, offset.x, 
                            1, 0, 1, -1, Direction.RIGHT, -1, 0, -1, -1, Direction.LEFT);
                        break;

                    default:
                        throw new RuntimeException(/* unreachable */);
                }

                if (easingApplied) return;

                /*
                 * We can't step over a cell that has contents,
                 * no easing.
                 */
                dx = 0;
                dy = 0;
            }
        }

        pi.location.translate(dx, dy);
    }

    /**
     * A helper function that tests if we can apply easing in one 
     * of the directions. This is generalized for all the possibilities,
     * so it may be vague a bit.
     */
    private boolean ease(
        PlayerInfo pi, Point xy, int o,
        int x1, int y1, int x2, int y2, Direction d1,
        int x3, int y3, int x4, int y4, Direction d2)
    {
        final int easeMargin = boardData.cellSize / 3;

        if (o > boardData.cellSize - easeMargin
            && canWalkOn(pi, new Point(xy.x + x1, xy.y + y1)) 
            && canWalkOn(pi, new Point(xy.x + x2, xy.y + y2)))
        {
            movePlayer(pi, d1);
            return true;
        }

        if (o < easeMargin
            && canWalkOn(pi, new Point(xy.x + x3, xy.y + y3)) 
            && canWalkOn(pi, new Point(xy.x + x4, xy.y + y4)))
        {
            movePlayer(pi, d2);
            return true;
        }
        
        return false;
    }

    /**
     * Returns <code>true</code> if a player can walk on the grid's
     * given coordinates.
     */
    private boolean canWalkOn(PlayerInfo pi, Point txy)
    {
        /*
         * Leave player info as an argument, we may want to add a 'god mode' in the future so
         * that certain players (or at given period of times) can walk on bombs.
         */
        return CellType.isWalkable(board.cellAt(txy).type);        
    }

    /**
     * Assign players to their default board positions.
     */
    private void setupPlayers()
    {
        final PlayerInfo [] pi = new PlayerInfo [players.length];
        final IPlayerSprite [] pv = new IPlayerSprite [players.length];
        final Point [] defaults = board.defaultPlayerPositions;
        if (defaults.length < pi.length)
        {
            Logger.getAnonymousLogger().warning("The board has fewer positions than players: "
                + defaults.length + " < " + pi.length);
        }

        for (int i = 0; i < players.length; i++)
        {
            pi[i] = new PlayerInfo(players[i], i);
            pv[i] = pi[i];
            pi[i].location.setLocation(
                boardData.gridToPixel(defaults[i % defaults.length]));
        }

        this.playerInfos = pi;
        this.playerViews = pv;
    }

    /**
     * Advance each cell's frame number, if they contain animations of some sort (bombs,
     * explosions).
     */
    private void processBoardCells()
    {
        final Cell [][] cells = board.cells;

        /*
         * Advance animation cells.
         */
        for (int y = board.height - 1; y >= 0; y--)
        {
            for (int x = board.width - 1; x >= 0; x--)
            {
                final Cell cell = cells[x][y];
                final CellType type = cell.type;

                /*
                 * Advance counter frame on cells that use it.
                 * Clean up cells that have finished animating.
                 */
                cell.counter++;

                final int removeAt = CellType.getRemoveAtCounter(type);
                if (removeAt > 0 && cell.counter == removeAt)
                {
                    cells[x][y] = Cell.getInstance(CellType.CELL_EMPTY);
                    continue;
                }
            }
        }

        /*
         * Detect and propagate explosions.
         */
        final ArrayList<Point> crates = Lists.newArrayList();
        final ArrayList<BombCell> bombs = Lists.newArrayList();
        for (int y = board.height - 1; y >= 0; y--)
        {
            for (int x = board.width - 1; x >= 0; x--)
            {
                final Cell cell = cells[x][y];
                final CellType type = cell.type;

                if (type == CellType.CELL_BOMB)
                {
                    final BombCell bomb = (BombCell) cell;
                    if (bomb.fuseCounter-- <= 0)
                    {
                        BoardUtilities.explode(board, bombs, crates, x, y, bomb.range);
                    }
                }
            }
        }

        /*
         * Remove the crates that have been bombed out.
         */
        for (Point p : crates)
        {
            board.cells[p.x][p.y] = Cell.getInstance(CellType.CELL_CRATE_OUT);
        }
        
        /*
         * Update player bomb counters.
         */
        for (BombCell bomb : bombs)
        {
            if (bomb.player != null)
            {
                bomb.player.bombCount++;
            }
        }
    }

    /**
     * Passive wait for the next frame.
     */
    private void waitForFrame()
    {
        try
        {
            if (lastFrameTimestamp > 0)
            {
                final long nextFrameStart = lastFrameTimestamp + framePeriod;
                long now;
                while ((now = System.currentTimeMillis()) < nextFrameStart)
                {
                    Thread.sleep(nextFrameStart - now);
                }
                this.lastFrameTimestamp = nextFrameStart;
            }
            else
            {
                lastFrameTimestamp = System.currentTimeMillis();
            }
        }
        catch (InterruptedException e)
        {
            // Exit immediately.
        }
    }
}
