package com.jdyna.players.jedrzejczaknowak;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.jdyna.*;
import org.jdyna.GameEvent.Type;

import com.jdyna.players.jedrzejczaknowak.state.GameState;
import com.jdyna.players.jedrzejczaknowak.state.Player;
import com.jdyna.players.jedrzejczaknowak.state.Space;
import com.jdyna.players.jedrzejczaknowak.state.Player.PlayerState;
import com.jdyna.players.jedrzejczaknowak.state.Space.SpaceType;

/**
 * Artificial dynablaster player called n00b.
 */
public class NoobPlayer implements IPlayerController, IGameEventListener {
    /**
     * Max value of bomb timer in {@link GameState}.
     */
    private final static int TIMER_MAX_VALUE = Globals.DEFAULT_FUSE_FRAMES + 1;

    /**
     * n00b will place a bomb in range of another bomb if this bomb won't
     * explode in number of fromes specified by this parameter
     */
    private final static int BOMB_SAFETY_TRESHOLD = TIMER_MAX_VALUE
            - Globals.DEFAULT_CELL_SIZE / 2;

    /**
     * n00b will walk into bomb explosion range if it won't explode in number of
     * frames specified by this parameter.
     */
    private final static int WALK_SAFETY_TRESHOLD = 1 + 3 * (Globals.DEFAULT_CELL_SIZE / 2);

    /**
     * In-game n00b's name.
     */
    private final String name;

    /**
     * Reference to n00b on the players list.
     */
    private Player me;

    /**
     * Reference to the space currently occupied by n00b.
     */
    private Space mySpace;

    /**
     * Reference to the current state of the game.
     */
    private GameState state;

    /**
     * Current bomb placing state.
     */
    private boolean drops;

    /**
     * Current n00b direction.
     */
    private Direction direction;

    public NoobPlayer(String name) {
        this.name = name;
        drops = false;
        direction = null;
    }

    @Override
    public boolean dropsBomb() {
        return drops;
    }

    @Override
    public Direction getCurrent() {
        return direction;
    }

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events) {
        for (GameEvent event : events) {
            if (event.type == Type.GAME_START) {
                state = new GameState((GameStartEvent) event);
            } else if (event.type == Type.GAME_STATE) {
                state.update(frame, (GameStateEvent) event);
                me = state.getPlayer(name);
                if (me != null && me.getState() != PlayerState.PWNED) {
                    think();
                }

            } else if (event.type == Type.GAME_OVER) {
                state = null;
            }
        }
    }

    /**
     * Decides where n00b should go next and whether he should plant a bomb.
     */
    private void think() {
        drops = false;
        mySpace = state.getPlayerSpace(me);
        List<Direction> possibleMoves = getWalkableDirections(mySpace);
        int best = Integer.MAX_VALUE;
        int bestIndex = -1;
        possibleMoves.add(null);
        Point toGo = null;
        Distances dist = null;

        for (int i = 0; i < possibleMoves.size(); i++) {
            Point dest = getNeighbourPointByDirection(mySpace, possibleMoves
                    .get(i));
            Distances d = new Distances(dest, state, me);
            int val = rateMove(dest, d);
            if (val < best) {
                best = val;
                bestIndex = i;
                toGo = dest;
                dist = d;
            }
        }

        if ((!mySpace.isHot() && mySpace.isSafe()
                && dist.getDistToBonus() == null && dist.getDistToEnemy() == null)
                || bestIndex == -1) {
            direction = null;
        } else {
            drops = canDropBomb(toGo, dist);
            direction = possibleMoves.get(bestIndex);
        }

    }

    /**
     * Rates the move to the given point on the board.
     * <p>
     * Rating depends on distances to the interesting points on the board as
     * well as on bombs and their timers.
     */
    private int rateMove(Point dest, Distances d) {
        int ret = 0;
        int bonusWeight = 20;
        int enemyWeight = 10;
        final int safeWeight = 1000;
        final int timerWeight = 40;
        final int tooCloseWeight = 30;
        final int escapeRoutesCountWeight = 1;

        if (me.getState() == PlayerState.CHEATING) {
            if (d.getDistToBonus() != null && d.getDistToBonus() == 0) {
                return Integer.MIN_VALUE;
            }
            bonusWeight = 1000;
        }

        ret += escapeRoutesCountWeight * (4 - d.getEscapeRoutesCount());
        if (!state.spaceAt(dest).isSafe()) {
            return Integer.MAX_VALUE;
        }
        if (mySpace.getType() == SpaceType.BOMB) {
            bonusWeight = enemyWeight = 0;
        }

        if (!state.spaceAt(dest).willBeSafe(0, WALK_SAFETY_TRESHOLD)
                || mySpace.getType() == SpaceType.BOMB) {
            if (d.getDistToSafe() != null) {
                ret += safeWeight * d.getDistToSafe();
            } else {
                return Integer.MAX_VALUE;
            }
            Integer t = state.spaceAt(dest).getNextExplosion();
            if (t != null) {
                ret += timerWeight * (TIMER_MAX_VALUE - t);
            }

        }

        if (d.getDistToEnemy() != null) {
            ret += enemyWeight * d.getDistToEnemy();
            if (d.getDistToEnemy() < 2) {
                ret += tooCloseWeight * (2 - d.getDistToEnemy());
            }
        }
        if (d.getDistToBonus() != null) {
            ret += bonusWeight * d.getDistToBonus();
        }
        return ret;
    }

    /**
     * Checks whether n00b should plant a bomb.
     */
    private boolean canDropBomb(Point dest, Distances distances) {
        if (!mySpace.willBeSafe(0, BOMB_SAFETY_TRESHOLD)
                || !state.spaceAt(dest).willBeSafe(0, BOMB_SAFETY_TRESHOLD)
                || me.getState() == PlayerState.CHEATING) {
            return false;
        }
        Integer safeDist = distances.getDistToSafeIfBombPlaced();
        if (safeDist == null || safeDist > 3) {
            return false;
        }

        Integer dist = distances.getDistToEnemy();
        if (dist != null && dist < me.getRange()) {
            return true;
        }
        Integer d = distances.getDistToCrate();

        if (d != null) {
            if (d < me.getRange()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of walkable directions.
     */
    private List<Direction> getWalkableDirections(Space p) {
        List<Direction> ret = new ArrayList<Direction>();
        for (Direction direction : Direction.values()) {
            Point pp = getNeighbourPointByDirection(p, direction);
            if (state.isPointOutsideBoard(pp)
                    || !state.spaceAt(pp).isWalkable()
                    || !state.spaceAt(pp).isSafe()) {
                continue;
            }
            ret.add(direction);
        }

        return ret;
    }

    /**
     * @see Distances#getNeighbourPointByDirection(Point,
     *      org.jdyna.IPlayerController.Direction)
     */
    private Point getNeighbourPointByDirection(Space p, Direction direction) {
        return Distances.getNeighbourPointByDirection(p.toPoint(), direction);
    }

}
