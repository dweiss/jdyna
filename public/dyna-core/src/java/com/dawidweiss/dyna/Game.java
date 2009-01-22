package com.dawidweiss.dyna;

import static java.lang.Math.*;

import java.awt.Point;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.IPlayerController.Direction;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Game controller. The controller and <b>all the objects involved in the game</b> are
 * single-threaded and should be accessed from within the game loop only.
 */
public final class Game
{
    private final static Logger logger = LoggerFactory.getLogger(Game.class); 

    /**
     * The game board (playfield cells).
     */
    public final Board board;
    
    /**
     * Game modes control how the game progresses, when the game terminates and
     * what are the conditions to bring some players back to life.
     */
    public static enum Mode
    {
        /**
         * In last man standing mode ("classic" Dyna Blaster), the purpose of the game
         * is to kill all the enemies. The last player to survive wins the game. All players
         * are ranked after the game is over.
         * 
         * @see GameResult
         * @see Standing
         */
        LAST_MAN_STANDING,
        
        /**
         * In the death match mode, players compete by bombing each other. When player A
         * kills player B, then player A gets a reward. It does not matter whose bomb
         * initiated the explosion, the origin (bomb) of any 'flame' that kills player B
         * earns player A a credit. In case of overlapping flames, the credit is given to
         * all players that contributed to overlapping flame.
         */
        DEATHMATCH
    }

    /**
     * Static player information. 
     */
    private Player [] players;

    /**
     * Dynamic information about players involved in the game.
     */
    private List<PlayerInfo> playerInfos;

    /** Game listeners. */
    private final ArrayList<IGameEventListener> listeners = Lists.newArrayList();

    /** Board dimensions. */
    private BoardInfo boardData;

    /**
     * How many frames to 'linger' after the game is over.
     */
    private int lingerFrames = Globals.DEFAULT_LINGER_FRAMES;
    
    /**
     * If periodic bonuses are placed on the board, then this is 
     * the period after which a new bonus should be placed on the board.
     * Bonuses will be placed at random board positions.
     */
    private int bonusPeriod = Globals.DEFAULT_BONUS_PERIOD;

    /**
     * Last time a bonus was added to the board.
     */
    private int lastBonusFrame;

    /**
     * Bonus cells assigned every {@link #bonusPeriod}.
     */
    private final static List<CellType> BONUSES = 
        Arrays.asList(CellType.CELL_BONUS_BOMB, CellType.CELL_BONUS_RANGE);

    /**
     * Reusable array of events dispatched in each frame.
     * 
     * @see #run()
     */
    private final ArrayList<GameEvent> events = Lists.newArrayList();

    /**
     * Random number generator (bonuses etc).
     */
    private final Random random = new Random();

    /**
     * Game timer.
     */
    private final GameTimer timer = new GameTimer(Globals.DEFAULT_FRAME_RATE);

    /**
     * Game mode of the current competition.
     */
    private Mode mode;

    /**
     * Frame limit for the game.
     */
    private int frameLimit;

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
     * Starts the game, does not return until the game ends. The game can be interrupted
     * by setting the running thread's interrupted flag.
     */
    public GameResult run(Mode mode)
    {
        this.mode = mode;

        setupPlayers();
        lastBonusFrame = bonusPeriod;

        int frame = 0;
        GameResult result = null;
        events.add(new GameStartEvent(boardData));
        do
        {
            if (Thread.currentThread().isInterrupted()
                || (result == null && frameLimit > 0 && frame > frameLimit))
            {
                Thread.currentThread().interrupt();
                break;
            }

            timer.waitForFrame();

            processBoardCells();
            processPlayers(frame);
            processBonuses(frame);

            events.add(new GameStateEvent(board.cells, playerInfos));
            fireFrameEvent(frame);
            frame++;

            /*
             * The game may be finished, but there are still
             * lingering frames we must replay.
             */
            if (result == null)
            {
                result = checkGameOver();
            }

            events.clear();
        } while (result == null || lingerFrames-- > 0);

        /*
         * Check interrupted state and clear it.
         */
        if (Thread.interrupted())
        {
            if (result == null) result = new GameResult(mode, getPlayerStats());
            result.gameInterrupted = true;
        }

        /*
         * Dispatch game over.
         */
        events.add(new GameOverEvent());
        fireFrameEvent(frame);

        return result;
    }

    /**
     * Check if new bonuses should be placed on the board.
     */
    private void processBonuses(int frame)
    {
        if (lastBonusFrame < frame)
        {
            /* 
             * We pick the bonus location at random, avoiding
             * cells where players are. We could try to make it smarter
             * by placing bonuses in equal or at least maximum distance from all
             * players, but it did not work that well in practice (I tried).
             */
            final HashSet<Point> banned = Sets.newHashSet();
            for (PlayerInfo pi : playerInfos)
            {
                if (!pi.isDead()) banned.add(pi.location);
            }

            final ArrayList<Point> positions = 
                Lists.newArrayListWithExpectedSize(board.width * board.height / 2);
            for (int y = board.height - 1; y >= 0; y--)
            {
                for (int x = board.width - 1; x >= 0; x--)
                {
                    final Point p = new Point(x, y);
                    if (!banned.contains(p) 
                        && board.cellAt(p).type == CellType.CELL_EMPTY)
                    {
                        positions.add(p);
                    }
                }
            }

            final int size = positions.size(); 
            if (size > 0)
            {
                final CellType bonus = BONUSES.get(random.nextInt(BONUSES.size()));
                final Point p = positions.get(random.nextInt(size));
                board.cellAt(p, Cell.getInstance(bonus));
            }

            this.lastBonusFrame = frame + bonusPeriod;
        }
    }

    /**
     * Check game over conditions.
     */
    private GameResult checkGameOver()
    {
        int alive = 0;
        for (PlayerInfo pi : playerInfos)
        {
            if (!pi.isStoneDead()) alive++;
        }

        /*
         * At least two players left in the battle.
         */
        if (alive > 1) return null;

        /*
         * Finito, basta, tutti morti. 
         */
        return new GameResult(mode, getPlayerStats());
    }

    /**
     * @return Return current player statistics.
     */
    private Collection<PlayerStatus> getPlayerStats()
    {
        final ArrayList<PlayerStatus> stats = Lists.newArrayList();
        for (PlayerInfo pi : playerInfos)
        {
            stats.add(pi.getStatus());
        }
        
        return stats;
    }

    /**
     * Set the frame rate. Zero means no delays.
     */
    public void setFrameRate(double framesPerSecond)
    {
        timer.setFrameRate(framesPerSecond);
    }

    /**
     * Sets the frame limit for the game. The game will be interrupted if this
     * limit is reached. A limit of zero means no limit. 
     */
    public void setFrameLimit(int framesLimit)
    {
        assert framesLimit >= 0;
    
        this.frameLimit = framesLimit;
    }

    /*
     * 
     */
    public void addListener(IGameEventListener listener)
    {
        if (listeners.contains(listener))
        {
            throw new RuntimeException("It is an error to add the same listener more than once: "
                + listener);
        }
        listeners.add(listener);
    }

    /*
     * 
     */
    public void removeListener(IGameEventListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * Dispatch frame events to listeners.
     */
    private void fireFrameEvent(int frame)
    {
        final List<GameEvent> e = Collections.unmodifiableList(events);
        for (IGameEventListener gl : listeners)
        {
            gl.onFrame(frame, e);
        }
    }

    /**
     * Move players according to their controller signals, drop bombs,
     * check collisions.
     */
    private void processPlayers(int frame)
    {
        final ArrayList<PlayerInfo> killed = Lists.newArrayList();

        /*
         * Process controller direction signals, drop bombs, check collisions
         * and bring the dead back to life.  
         */
        for (int i = 0; i < players.length; i++)
        {
            final PlayerInfo pi = playerInfos.get(i);
            final IPlayerController c = players[i].controller;

            final IPlayerController.Direction signal = c.getCurrent();
            pi.nextFrameUpdate(signal);

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

            if (c.dropsBomb() && !pi.isImmortal())
            {
                dropBombAttempt(frame, pi);
            }

            /*
             * check collisions against bombs and other active cells.
             */
            checkCollisions(frame, killed, pi);
        }
        
        /*
         * Add sound effect to the queue, if any.
         */
        if (killed.size() > 0)
        {
            events.add(new SoundEffectEvent(SoundEffect.DYING, killed.size()));
        }

        if (isDeathMatch())
        {
            /*
             * Bring the dead back to life, if their time has come.
             */
            for (PlayerInfo pi : playerInfos)
            {
                if (pi.isDead() && pi.shouldResurrect())
                {
                    pi.location.setLocation(getRandomLocation());
                    pi.resurrect();
                    
                    logger.debug("Resurrected: " + pi.getStatus());
                }
            }
        }
    }

    /**
     * Check collisions against bombs and other active cells.
     */
    private void checkCollisions(int frame, List<PlayerInfo> kills, PlayerInfo pi)
    {
        /*
         * Immortals have privileges, but cannot collect bonuses.
         */
        if (pi.isImmortal()) return;

        /*
         * Check collisions against grid cells. We only care about the cell directly 
         * under the player.
         */
        final Point xy = boardData.pixelToGrid(pi.location);
        final Cell c = board.cellAt(xy);
        
        // For whom the bell tolls...
        if (c.type.isLethal())
        {
            logger.debug("Killed: " + pi.getName());
            pi.kill(frame);
            kills.add(pi);

            /*
             * If the cell below is an explosion update attributions for this fatality.
             */
            if (c.type.isExplosion())
            {
                final ExplosionCell e = (ExplosionCell) c;
                for (PlayerInfo sniper : e.flamesBy)
                {
                    // No points for killing yourself.
                    logger.debug(sniper.getName() + " killed " + pi.getName());
                    if (pi != sniper)
                    {
                        sniper.collectKill();
                    }
                }
            }
        }

        /*
         * Process bonuses. The bonus-assignment is not entirely fair, because if
         * two players touch the bonus at once, the player with lower index will collect
         * the bonus. With randomized player order, however, this should be of no 
         * practical importance.
         */
        boolean bonusCollected = false;
        if (c.type == CellType.CELL_BONUS_BOMB)
        {
            pi.bombCount++;
            bonusCollected = true;
        }

        if (c.type == CellType.CELL_BONUS_RANGE)
        {
            pi.bombRange++;
            bonusCollected = true;
        }

        if (bonusCollected)
        {
            board.cellAt(xy, Cell.getInstance(CellType.CELL_EMPTY));
            events.add(new SoundEffectEvent(SoundEffect.BONUS, 1));
        }
    }

    /**
     * Attempt to drop a bomb at the given location (if the player has any bombs left
     * and the cell under its feet is empty).
     */
    private void dropBombAttempt(int frame, PlayerInfo pi)
    {
        final Point xy = boardData.pixelToGrid(pi.location);

        final boolean canPlaceBomb = board.cellAt(xy).type == CellType.CELL_EMPTY;
        final boolean hasBombs = pi.bombCount > 0;
        final boolean dropDelay = (pi.lastBombFrame + Globals.BOMB_DROP_DELAY > frame);

        if (canPlaceBomb && hasBombs && !dropDelay)
        {
            pi.bombCount--;
            pi.lastBombFrame = frame;

            final BombCell bomb = (BombCell) Cell.getInstance(CellType.CELL_BOMB);
            bomb.player = pi;
            bomb.range = pi.bombRange;
            board.cellAt(xy, bomb);
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
         * Players in immortality mode can walk over bombs, but not anything else.
         */
        CellType t = board.cellAt(txy).type;
        return t.isWalkable() || (pi.isImmortal() && t == CellType.CELL_BOMB);
    }

    /**
     * Assign players to their default board positions.
     */
    private void setupPlayers()
    {
        this.playerInfos = Lists.newArrayList();

        final Point [] defaults = board.defaultPlayerPositions;
        if (defaults.length < players.length)
        {
            logger.warn("The board has fewer positions than players: "
                + defaults.length + " < " + players.length);
        }

        final int initialLives = (mode == Mode.LAST_MAN_STANDING ? 1 : Globals.DEFAULT_LIVES);
        for (int i = 0; i < players.length; i++)
        {
            final Player p = players[i];
            if (p.controller instanceof IGameEventListener)
            {
                addListener((IGameEventListener) p.controller);
            }

            final PlayerInfo pi = new PlayerInfo(p, i, initialLives);
            pi.location.setLocation(getDefaultLocation(i));

            playerInfos.add(pi);
        }
    }

    /**
     * Pick default location for player <code>i</code>.
     */
    private Point getDefaultLocation(int i)
    {
        final Point [] defaults = board.defaultPlayerPositions;        
        return boardData.gridToPixel(defaults[i % defaults.length]);
    }

    /**
     * Pick a new random location for a given player.
     */
    private Point getRandomLocation()
    {
        final Point [] defaults = board.defaultPlayerPositions;
        return boardData.gridToPixel(defaults[random.nextInt(defaults.length)]);
    }

    /**
     * Advance each cell's frame number, if they contain animations of some sort (bombs,
     * explosions).
     */
    private void processBoardCells()
    {
        /*
         * Advance animation cells.
         */
        for (int x = board.width - 1; x >= 0; x--)
        {
            for (int y = board.height - 1; y >= 0; y--)
            {
                final Cell cell = board.cellAt(x, y);
                final CellType type = cell.type;

                /*
                 * Advance counter frame on cells that use it.
                 * Clean up cells that have finished animating.
                 */
                cell.counter++;

                final int removeAt = type.getRemoveAtCounter();
                if (removeAt > 0 && cell.counter == removeAt)
                {
                    board.cellAt(x, y, Cell.getInstance(CellType.CELL_EMPTY));
                    continue;
                }
            }
        }

        /*
         * Detect and propagate explosions.
         */
        final ArrayList<Point> crates = Lists.newArrayList();
        final ArrayList<BombCell> bombs = Lists.newArrayList();
        for (int x = board.width - 1; x >= 0; x--)
        {
            for (int y = board.height - 1; y >= 0; y--)
            {
                final Cell cell =  board.cellAt(x, y);
                final CellType type = cell.type;

                if (type == CellType.CELL_BOMB)
                {
                    final BombCell bomb = (BombCell) cell;
                    if (bomb.fuseCounter-- <= 0)
                    {
                        BoardUtilities.explode(board, bombs, crates, x, y);
                    }
                }
            }
        }

        /*
         * Add sound events to the queue.
         */
        if (bombs.size() > 0)
        {
            events.add(new SoundEffectEvent(SoundEffect.BOMB, bombs.size()));
        }

        /*
         * Remove the crates that have been bombed out.
         */
        for (Point p : crates)
        {
            board.cellAt(p, Cell.getInstance(CellType.CELL_CRATE_OUT));
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
     * Returns <code>true</code> if this game is in death match mode.
     */
    private boolean isDeathMatch()
    {
        return mode == Mode.DEATHMATCH;
    }
}
