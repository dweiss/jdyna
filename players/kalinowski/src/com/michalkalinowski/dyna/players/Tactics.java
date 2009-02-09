package com.michalkalinowski.dyna.players;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerSprite;
import com.dawidweiss.dyna.IPlayerController.Direction;

final class Tactics
{
    private static class TacticsCell
    {
        public static enum Type
        {
            EMPTY, WALL, CRATE, BOMB, EXPLOSION, BONUS_RANGE, BONUS_BOMB
        }

        public static class Bomb
        {
            public int frame;
            public int range;
        }

        public final Point position;
        public Type type;
        public Bomb bomb;
        public TacticsPlayer tacticsPlayer;

        public TacticsCell(Point position)
        {
            this.position = position;
        }
    }

    private static class TacticsPlayer
    {
        public final String name;
        public Point pixelPosition;
        public Point gridPosition;
        public boolean isDead;
        public boolean isImmortal;
        public int bombRange = Globals.DEFAULT_BOMB_RANGE;
        public int bombCount = Globals.DEFAULT_BOMB_COUNT;

        public TacticsPlayer(String name)
        {
            this.name = name;
        }
    }

    private static class AStarPoint
    {
        public Point position;
        public transient int f;
        public transient int g;
        public transient int h;
        public AStarPoint comeFrom;

        public AStarPoint()
        {
        }

        public AStarPoint(Point p)
        {
            this.position = new Point(p);
        }

        public AStarPoint(int x, int y)
        {
            this.position = new Point(x, y);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this.position == null) return false;
            if (obj instanceof AStarPoint)
            {
                final AStarPoint p = (AStarPoint) obj;
                return this.position.equals(p.position);
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode()
        {
            return this.position.hashCode();
        }
    }

    public Direction direction;
    public boolean dropsBomb;

    private static final double ATTACK_DISTANCE_FACTOR = 1.1;
    private static final double SAFETY_FACTOR = 0.8;
    private static final double BOMB_SEFETY_LOW_LEVEL = 0.6;
    private static final double BOMB_SEFETY_HIGH_LEVEL = 0.8;

    private final String myName;
    private final BoardInfo boardInfo;
    private int frame;
    private Map<String, TacticsPlayer> tacticsPlayersMap;
    private TacticsPlayer myTacticsPlayer;
    private final TacticsCell [][] tacticsCells;

    private List<Point> bonuses = new ArrayList<Point>();
    private List<Point> enemies = new ArrayList<Point>();
    private List<Point> targets = new ArrayList<Point>();

    private final Random random = new Random();

    public Tactics(String name, BoardInfo boardInfo)
    {
        this.myName = name;
        this.boardInfo = boardInfo;
        this.tacticsPlayersMap = new HashMap<String, TacticsPlayer>();
        this.tacticsCells = new TacticsCell [boardInfo.gridSize.width] [boardInfo.gridSize.height];
        for (int x = 0; x < boardInfo.gridSize.width; x++)
            for (int y = 0; y < boardInfo.gridSize.height; y++)
                this.tacticsCells[x][y] = new TacticsCell(new Point(x, y));
    }

    public void updateState(int frame, Cell [][] cells,
        List<? extends IPlayerSprite> players)
    {
        this.frame = frame;
        preUpdateState();
        updateTacticsPlayers(players);
        updateTacticsCells(cells);
        postUpdateState();
    }

    private void preUpdateState()
    {
        for (int x = 0; x < this.boardInfo.gridSize.width; x++)
            for (int y = 0; y < this.boardInfo.gridSize.height; y++)
                this.tacticsCells[x][y].tacticsPlayer = null;
        this.bonuses.clear();
        this.enemies.clear();
        this.targets.clear();
    }

    private void updateTacticsPlayers(List<? extends IPlayerSprite> players)
    {
        for (IPlayerSprite player : players)
        {
            final TacticsPlayer tacticsPlayer = getTacticsPlayer(player.getName());

            if (this.myName.equals(tacticsPlayer.name)) this.myTacticsPlayer = tacticsPlayer;

            tacticsPlayer.pixelPosition = player.getPosition();
            tacticsPlayer.gridPosition = this.boardInfo
                .pixelToGrid(tacticsPlayer.pixelPosition);
            tacticsPlayer.isDead = player.isDead();
            tacticsPlayer.isImmortal = player.isImmortal();

            this.tacticsCells[tacticsPlayer.gridPosition.x][tacticsPlayer.gridPosition.y].tacticsPlayer = tacticsPlayer;

            if (!tacticsPlayer.isDead && tacticsPlayer != this.myTacticsPlayer)
            {
                this.enemies.add(tacticsPlayer.gridPosition);
            }
        }
    }

    private TacticsPlayer getTacticsPlayer(String name)
    {
        if (!this.tacticsPlayersMap.containsKey(name))
        {
            this.tacticsPlayersMap.put(name, new TacticsPlayer(name));
        }
        return this.tacticsPlayersMap.get(name);
    }

    private void updateTacticsCells(Cell [][] cells)
    {
        for (int x = 0; x < this.boardInfo.gridSize.width; x++)
            for (int y = 0; y < this.boardInfo.gridSize.height; y++)
            {
                final Cell cell = cells[x][y];
                final TacticsCell tacticsCell = this.tacticsCells[x][y];

                if (tacticsCell.type == TacticsCell.Type.BONUS_RANGE
                    && cell.type != CellType.CELL_BONUS_RANGE)
                {
                    if (tacticsCell.tacticsPlayer != null) tacticsCell.tacticsPlayer.bombRange++;
                }
                if (tacticsCell.type == TacticsCell.Type.BONUS_BOMB
                    && cell.type != CellType.CELL_BONUS_BOMB)
                {
                    if (tacticsCell.tacticsPlayer != null) tacticsCell.tacticsPlayer.bombCount++;
                }
                if (tacticsCell.type == TacticsCell.Type.BOMB
                    && cell.type != CellType.CELL_BOMB)
                {
                    tacticsCell.bomb = null;
                }
                if (cell.type == CellType.CELL_BOMB
                    && tacticsCell.type != TacticsCell.Type.BONUS_BOMB)
                {
                    final TacticsCell.Bomb bomb = new TacticsCell.Bomb();
                    bomb.frame = this.frame - 1; // minus one frame for sure
                    bomb.range = (tacticsCell.tacticsPlayer != null) ? tacticsCell.tacticsPlayer.bombRange
                        : Globals.DEFAULT_BOMB_RANGE;
                    tacticsCell.bomb = bomb;
                }

                if (cell.type.isExplosion()) tacticsCell.type = TacticsCell.Type.EXPLOSION;
                else if (cell.type == CellType.CELL_BOMB) tacticsCell.type = TacticsCell.Type.BOMB;
                else if (cell.type == CellType.CELL_BONUS_BOMB) tacticsCell.type = TacticsCell.Type.BONUS_BOMB;
                else if (cell.type == CellType.CELL_BONUS_RANGE) tacticsCell.type = TacticsCell.Type.BONUS_RANGE;
                else if (cell.type == CellType.CELL_CRATE
                    || cell.type == CellType.CELL_CRATE_OUT) tacticsCell.type = TacticsCell.Type.CRATE;
                else if (cell.type == CellType.CELL_EMPTY) tacticsCell.type = TacticsCell.Type.EMPTY;
                else if (cell.type == CellType.CELL_WALL) tacticsCell.type = TacticsCell.Type.WALL;

                if (tacticsCell.type == TacticsCell.Type.BONUS_BOMB
                    || tacticsCell.type == TacticsCell.Type.BONUS_RANGE)
                {
                    this.bonuses.add(tacticsCell.position);
                }
            }
    }

    private void postUpdateState()
    {
        // do nothing... (for now)
    }

    public void process()
    {
        if (this.myTacticsPlayer != null && this.myTacticsPlayer.isDead)
        {
            this.direction = null;
            this.dropsBomb = false;
            return;
        }

        updateTargets();
        updateState();
    }

    private void updateTargets()
    {
        if (!this.bonuses.isEmpty())
        {
            this.targets.addAll(this.bonuses);
        }
        else if (!this.enemies.isEmpty())
        {
            this.targets.addAll(this.enemies);
        }
        else
        {
            // random target
            targets.add(new Point(random.nextInt(boardInfo.gridSize.width),
                boardInfo.gridSize.height));
        }
    }

    private void updateState()
    {
        this.direction = null;
        if (this.targets.isEmpty()) return;

        final Point source = this.myTacticsPlayer.gridPosition;
        final List<Point> targets = this.targets;

        final List<Point> path = calcAStarPath(source, targets);
        if (path.isEmpty()) direction = null;
        else updateDirection(source, path.get(0));

        this.dropsBomb = (calcHeuristicDistance(source, this.enemies) < ATTACK_DISTANCE_FACTOR
            * myTacticsPlayer.bombRange);
    }

    private List<Point> calcAStarPath(Point sourcePoint, List<Point> targetPoints)
    {
        final AStarPoint source = new AStarPoint(sourcePoint);
        final Set<AStarPoint> closedSet = new HashSet<AStarPoint>();
        final Set<AStarPoint> openSet = new HashSet<AStarPoint>();
        openSet.add(source);
        while (!openSet.isEmpty())
        {
            final AStarPoint x;
            {
                AStarPoint bestPoint = null;
                int bestF = Integer.MAX_VALUE;
                for (AStarPoint point : openSet)
                {
                    if (point.f < bestF)
                    {
                        bestPoint = point;
                        bestF = point.f;
                    }
                }
                x = bestPoint;
            }
            if (targetPoints.contains(x.position))
            {
                return reconstructPath(source, x);
            }
            openSet.remove(x);
            closedSet.add(x);
            final Set<AStarPoint> neighboirs = new HashSet<AStarPoint>();
            {
                final Point [] candidates = new Point []
                {
                    new Point(x.position.x - 1, x.position.y),
                    new Point(x.position.x + 1, x.position.y),
                    new Point(x.position.x, x.position.y - 1),
                    new Point(x.position.x, x.position.y + 1)
                };
                for (Point p : candidates)
                {
                    if (!boardInfo.isOnBoard(p)) continue;
                    final TacticsCell tacticsCell = tacticsCells[p.x][p.y];
                    if (tacticsCell.type == TacticsCell.Type.WALL
                        || tacticsCell.type == TacticsCell.Type.CRATE
                        || tacticsCell.type == TacticsCell.Type.EXPLOSION
                        || tacticsCell.type == TacticsCell.Type.BOMB) continue;
                    if (estimateSafetyLevel(p) < SAFETY_FACTOR) continue;
                    neighboirs.add(new AStarPoint(p));
                }
            }

            for (AStarPoint y : neighboirs)
            {
                if (closedSet.contains(y)) continue;
                int tempG = x.g + 1;
                boolean tempIsBetter = false;
                if (!openSet.contains(y))
                {
                    openSet.add(y);
                    y.h = calcHeuristicDistance(sourcePoint, targetPoints);
                    tempIsBetter = true;
                }
                else if (tempG < y.g)
                {
                    tempIsBetter = true;
                }
                if (tempIsBetter)
                {
                    y.comeFrom = x;
                    y.g = tempG;
                    y.f = y.g + y.h;
                }
            }
        }
        return new ArrayList<Point>();
    }

    private List<Point> reconstructPath(AStarPoint comeFrom, AStarPoint currentNode)
    {
        if (currentNode.comeFrom == null) return new ArrayList<Point>();
        final List<Point> path = reconstructPath(comeFrom, currentNode.comeFrom);
        path.add(currentNode.position);
        return path;
    }

    private int calcHeuristicDistance(Point sourcePoint, Point targetPoint)
    {
        return Math.abs(sourcePoint.x - targetPoint.x)
            + Math.abs(sourcePoint.y - targetPoint.y);
    }

    private int calcHeuristicDistance(Point sourcePoint, List<Point> targetPoints)
    {
        int bestDistance = Integer.MAX_VALUE;
        for (Point point : targetPoints)
        {
            final int distance = Math.abs(sourcePoint.x - point.x)
                + Math.abs(sourcePoint.y - point.y);
            if (distance < bestDistance) bestDistance = distance;
        }
        return bestDistance;
    }

    private void updateDirection(Point curr, Point next)
    {
        this.direction = null;
        if (next == null) return;

        if (next.x < curr.x) this.direction = IPlayerController.Direction.LEFT;
        else if (next.x > curr.x) this.direction = IPlayerController.Direction.RIGHT;
        else if (next.y < curr.y) this.direction = IPlayerController.Direction.UP;
        else if (next.y > curr.y) this.direction = IPlayerController.Direction.DOWN;
    }

    private double estimateSafetyLevel(Point position)
    {
        double column = Math.min(estimateLineSafetyLevel(position, new Point(position.x,
            position.y - 1), new Point(0, -1)), estimateLineSafetyLevel(position,
            new Point(position.x, position.y + 1), new Point(0, 1)));
        double row = Math.min(estimateLineSafetyLevel(position, new Point(position.x - 1,
            position.y), new Point(-1, 0)), estimateLineSafetyLevel(position, new Point(
            position.x + 1, position.y), new Point(1, 0)));
        return Math.min(column, row);
    }

    private double estimateLineSafetyLevel(Point origin, Point p, Point d)
    {
        if (!boardInfo.isOnBoard(p)) return 1.0;
        final TacticsCell tacticsCell = tacticsCells[p.x][p.y];
        if (tacticsCell.type == TacticsCell.Type.WALL
            || tacticsCell.type == TacticsCell.Type.CRATE) return 1.0;

        double result = 1.0;
        if (tacticsCell.type == TacticsCell.Type.BOMB)
        {
            final TacticsCell.Bomb bomb = tacticsCell.bomb;
            if (bomb.range >= calcHeuristicDistance(origin, p))
            {
                final int framesToExplosion = Globals.DEFAULT_FUSE_FRAMES
                    - (this.frame - bomb.frame);
                final double countDownProgress = (double) framesToExplosion
                    / Globals.DEFAULT_FUSE_FRAMES;

                if (countDownProgress < BOMB_SEFETY_LOW_LEVEL) return 0.0;
                else if (countDownProgress > BOMB_SEFETY_HIGH_LEVEL) result = 1.0;
                else result = (countDownProgress - BOMB_SEFETY_LOW_LEVEL)
                    / (BOMB_SEFETY_HIGH_LEVEL - BOMB_SEFETY_LOW_LEVEL);
            }
        }

        final Point nextPoint = new Point(p);
        nextPoint.translate(d.x, d.y);
        double anotherResult = estimateLineSafetyLevel(origin, nextPoint, d);

        return Math.min(result, anotherResult);
    }
}
