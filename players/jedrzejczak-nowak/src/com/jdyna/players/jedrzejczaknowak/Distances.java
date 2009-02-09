package com.jdyna.players.jedrzejczaknowak;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.dawidweiss.dyna.BoardUtilities;
import com.dawidweiss.dyna.IPlayerController.Direction;
import com.jdyna.players.jedrzejczaknowak.state.GameState;
import com.jdyna.players.jedrzejczaknowak.state.Player;
import com.jdyna.players.jedrzejczaknowak.state.Player.PlayerState;
import com.jdyna.players.jedrzejczaknowak.state.Space;
import com.jdyna.players.jedrzejczaknowak.state.Space.SpaceType;

/**
 * Contains distances to the interesting spaces on the board computed for the
 * specified starting point.
 * <p>
 * All fields of type <code>Integer</code> can be <code>null</code>. This means
 * that there is no enemy/bonus/etc... on board.
 */
public class Distances {
    /**
     * Starting point.
     */
    private final Point start;

    /**
     * Distances from {@link #start} in grid dimension.
     */
    private final int[][] distances;
    private final int width;
    private final int height;

    /**
     * Distance to the nearest killable enemy.
     */
    private Integer distToEnemy;

    /**
     * Distance to the nearest bonus.
     */
    private Integer distToBonus;

    /**
     * Distance to the nearest crate in range of our bomb.
     */
    private Integer distToCrate;

    /**
     * Distance to the nearest safe space on the board.
     */
    private Integer distToSafe;

    /**
     * Distance to the nearest safe space on the board assuming we put a bomb.
     */
    private Integer distToSafeIfBombPlaced;

    /**
     * The number of walkable directions from {@link #start}.
     */
    private int escapeRoutesCount;

    private final Player me;
    private final GameState state;

    /*
     * 
     */
    public Distances(Point start, GameState state, Player me) {
        this.me = me;
        this.state = state;
        this.start = start;
        width = state.getBoardWidth();
        height = state.getBoardHeight();
        distances = new int[width][height];
        for (int i = 0; i < width; i++) {
            Arrays.fill(distances[i], Integer.MAX_VALUE);
        }
        int x = start.x;
        int y = start.y;

        distances[x][y] = 0;
        calculateDistances();
        findClosestEnemy();
        setDistancesToInterestingSpaces(width, height);

    }

    /**
     * Finds the closest killable enemy.
     */
    private void findClosestEnemy() {
        List<Player> players = state.getPlayers();
        if (players == null || players.size() == 0) {
            distToEnemy = null;
        } else {
            distToEnemy = Integer.MAX_VALUE;

            for (Player player : players) {
                if (me.equals(player)
                        || player.getState() != PlayerState.PWNING) {
                    continue;
                }
                Point p = state.getPlayerSpace(player).toPoint();

                int distance = distances[p.x][p.y];
                if (distance < distToEnemy) {
                    distToEnemy = distance;
                }
            }

            // all enemies are immortal
            if (distToEnemy == Integer.MAX_VALUE) {
                distToEnemy = null;
            }
        }

    }

    /**
     * Computes distances to the interesting spaces on the board.
     */
    private void setDistancesToInterestingSpaces(int width, int height) {
        distToBonus = null;
        distToCrate = null;
        distToSafe = null;
        distToSafeIfBombPlaced = null;
        escapeRoutesCount = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Space s = (state.spaceAt(x, y));
                if (distances[x][y] == Integer.MAX_VALUE) {
                    // crate
                    if (s.getType() == SpaceType.CRATE
                            && noBarrierBetween(s, state.getPlayerSpace(me))) {
                        int dist = BoardUtilities.manhattanDistance(state
                                .getPlayerSpace(me).toPoint(), s.toPoint());
                        if (distToCrate == null || distToCrate > dist) {
                            distToCrate = dist;
                        }
                    }
                    continue;
                }
                if (distances[x][y] == 1) {
                    escapeRoutesCount++;
                }

                // bonus
                if (s.getType() == SpaceType.BONUS_BOMB
                        || s.getType() == SpaceType.BONUS_RANGE) {
                    if (distToBonus == null || distToBonus > distances[x][y]) {
                        distToBonus = distances[x][y];
                    }
                }

                // safe
                if (s.getType() != SpaceType.CRATE
                        && s.getType() != SpaceType.WALL && !s.isHot()
                        && s.isSafe()) {
                    if (distToSafe == null || distToSafe > distances[x][y]) {
                        distToSafe = distances[x][y];
                    }

                    // check if not within range of possibly placed bomb
                    boolean flag = true;
                    if (distances[x][y] <= me.getRange()) {
                        if (x == start.x || y == start.y) {
                            flag = false;
                        }
                    }
                    if (flag) {

                        if (distToSafeIfBombPlaced == null
                                || distToSafeIfBombPlaced > distances[x][y]) {
                            distToSafeIfBombPlaced = distances[x][y];
                        }
                    }
                }
            }
        }

    }

    /**
     * Calculates distances using the BFS algorithm.
     */
    private void calculateDistances() {
        Queue<Point> queue = new LinkedBlockingQueue<Point>();
        queue.addAll(getNeighbours(start));
        int count;
        int d = 0;
        while (!queue.isEmpty()) {
            d++;
            count = queue.size();
            for (int i = 0; i < count; i++) {
                Point point = queue.remove();
                if (distances[point.x][point.y] > d) {
                    distances[point.x][point.y] = d;
                    queue.addAll(getNeighbours(point));
                }
            }
        }

    }

    /**
     * Returns a list of walkable neighbours of the given point.
     */
    private List<Point> getNeighbours(Point p) {
        ArrayList<Point> ret = new ArrayList<Point>();
        for (Direction direction : Direction.values()) {
            Point pp = getNeighbourPointByDirection(p, direction);
            if (state.isPointOutsideBoard(pp)
                    || !state.spaceAt(pp).isWalkable()) {
                continue;
            }
            ret.add(pp);
        }
        return ret;
    }

    /**
     * Returns the neighbour reachable from the given point by walking one space
     * in the given direction.
     */
    public static Point getNeighbourPointByDirection(Point p,
            Direction direction) {
        if (direction == null) {
            return p;
        }
        switch (direction) {
        case DOWN:
            return new Point(p.x, p.y + 1);
        case LEFT:
            return new Point(p.x - 1, p.y);
        case UP:
            return new Point(p.x, p.y - 1);
        default:
            return new Point(p.x + 1, p.y);

        }
    }

    /**
     * Checks if there is no barrier between <code>Space a</code> and
     * <code>b</code>.
     * 
     * @return <code>true</code> if there is no barrier between given spaces,
     *         <code>false</code> otherwise.
     */
    private boolean noBarrierBetween(Space a, Space b) {
        if (a.equals(b)) {
            return true;
        }
        if (a.x == b.x) {
            int x = a.x;
            int from = a.y;
            int to = b.y;
            if (from > to) {
                int t = from;
                from = to;
                to = t;
            }
            if (to - from == 1) {
                return true;
            }
            for (int i = from; i < to; i++) {
                SpaceType type = state.spaceAt(x, i).getType();
                if (type == SpaceType.WALL || type == SpaceType.CRATE
                        || type == SpaceType.BOMB) {
                    return false;
                }
            }
            return true;
        } else if (a.y == b.y) {
            int y = a.y;
            int from = a.x;
            int to = b.x;
            if (from > to) {
                int t = from;
                from = to;
                to = t;
            }
            if (to - from == 1) {
                return true;
            }
            for (int i = from; i < to; i++) {
                SpaceType type = state.spaceAt(i, y).getType();
                if (type == SpaceType.WALL || type == SpaceType.CRATE
                        || type == SpaceType.BOMB) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder('\n');
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (distances[i][j] == Integer.MAX_VALUE) {
                    builder.append("XX");

                } else {
                    builder.append(String.format(Locale.US, "%2d",
                            distances[i][j]));
                }
                builder.append(" ");
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    public Integer getDistToEnemy() {
        return distToEnemy;
    }

    public Integer getDistToBonus() {
        return distToBonus;
    }

    public Integer getDistToCrate() {
        return distToCrate;
    }

    public Integer getDistToSafe() {
        return distToSafe;
    }

    public Integer getDistToSafeIfBombPlaced() {
        return distToSafeIfBombPlaced;
    }

    public int getEscapeRoutesCount() {
        return escapeRoutesCount;
    }
}
