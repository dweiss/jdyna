package com.arturklopotek.dyna.ai;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IPlayerSprite;

/**The implementation of {@link AbstractPlayerController} that uses simple (yet very effective) 
 * neighbor-scoring algorithm.
 */
public class AIPlayerController extends AbstractPlayerController
{
    private BombInfo [][] bombs;
    private boolean [][] bonuses;
    private Map<String, Integer> bombRanges = new HashMap<String, Integer>();
    private int boardWidth, boardHeight;

    private boolean initialized;

    public AIPlayerController(String playerName)
    {
        super(playerName);
    }

    @Override
    public void execute() throws InterruptedException
    {
        initialized = false;
        
        while (true)
        {
            waitForFrame();

            direction = null;
            dropsBomb = false;

            collectBoardInfo();

            Point myPos = toGrid(me().getPosition());

            direction = getBestDirection(myPos);
            dropsBomb = canDrop(myPos);
        }
    }

    /**
     * @param pos the player's position
     * @return true if it is good to place a bomb at the given position
     */
    private boolean canDrop(Point pos)
    {
        Cell [][] cells = state.getCells();
        Cell cell = cells[pos.x][pos.y];
        cells[pos.x][pos.y] = Cell.getInstance(CellType.CELL_BOMB);
        Distances dist = new Distances();
        distances(cells, bombs, opponentsAlive(), bonuses, pos.x, pos.y, dist);
        cells[pos.x][pos.y] = cell;
        
        return dist.safeCell <= 2 && isVisible(pos, opponentsAlive())
            && CellUtils.isSafe(state.getCells(), bombs, pos);
    }

    /** Evaluates what is the best direction for the player to go to from the given position. 
     * The selection is based on the neighbor cells' score. 
     * @param myPos the current player's position
     * @return the best direction
     */
    private Direction getBestDirection(Point myPos)
    {
        Direction dir = null;
        Cell [][] cells = state.getCells();
        List<Point> opponents = opponentsAlive();
        
        long curScore;
        long bestScore = score(myPos.x,myPos.y,cells,bombs,opponents,bonuses);
        
        curScore = score(myPos.x-1,myPos.y,cells,bombs,opponents,bonuses);
        if (curScore < bestScore) {
            dir = Direction.LEFT;
            bestScore = curScore;
        }
        curScore = score(myPos.x+1,myPos.y,cells,bombs,opponents,bonuses);
        if (curScore < bestScore) {
            dir = Direction.RIGHT;
            bestScore = curScore;
        }
        curScore = score(myPos.x,myPos.y-1,cells,bombs,opponents,bonuses);
        if (curScore < bestScore) {
            dir = Direction.UP;
            bestScore = curScore;
        }
        curScore = score(myPos.x,myPos.y+1,cells,bombs,opponents,bonuses);
        if (curScore < bestScore) {
            dir = Direction.DOWN;
            bestScore = curScore;
        }
        
        return dir;
    }

    /**
     * @return the score for the given cell
     */
    private static long score(int x, int y,Cell[][] cells,
        BombInfo[][] bombs,List<Point> opponents,boolean[][] bonuses)
    {
        if (!cells[x][y].type.isWalkable() || cells[x][y].type.isLethal())
            return Integer.MAX_VALUE;
        
        Distances dist = new Distances();
        distances(cells,bombs,opponents,bonuses,x,y,dist);
        
        return 1000000*dist.safeCell+dist.bonus*1000+dist.opponent;
    }

    /** Used internally by distances() */
    private static final class Node {
        public final int x,y,d;

        public Node(int x, int y, int d)
        {
            this.x = x;
            this.y = y;
            this.d = d;
        }
    }

    /** Computes the minimum distances to the different types of objects. */
    private static void distances(Cell[][] cells,BombInfo[][] bombs,
        List<Point> opponents,boolean [][] bonuses, int x,int y,
        /* out */ Distances dist)
    {
        boolean[][] visited = new boolean[cells.length][cells[0].length];
        LinkedList<Node> queue = new LinkedList<Node>();
        
        queue.add(new Node(x,y,0));
        visited[x][y] = true;
        
        while (!queue.isEmpty()) {
            Node node = queue.removeFirst();
            
            if (dist.safeCell > node.d)
                if (CellUtils.isSafe(cells, bombs, new Point(node.x,node.y)))
                    dist.safeCell = node.d;
            
            if (dist.bonus > node.d)
                if (bonuses[node.x][node.y])
                    dist.bonus = node.d;

            if (dist.opponent > node.d) {
                for (Point p : opponents) {
                    if (p.x == node.x && p.y == node.y) {
                        dist.opponent = node.d;
                        break;
                    }
                }
            }            
            
            for (Node child : new Node[] {
                new Node(node.x-1,node.y,node.d+1),new Node(node.x+1,node.y,node.d+1),
                new Node(node.x,node.y-1,node.d+1),new Node(node.x,node.y+1,node.d+1)}) {
                
                if (cells[child.x][child.y].type.isWalkable() && !cells[child.x][child.y].type.isLethal() && !visited[child.x][child.y]) {
                    visited[child.x][child.y] = true;
                    queue.add(child);
                }
            }
        }
    }

    private void initializeBoardInfo()
    {
        Cell [][] cells = state.getCells();
        boardWidth = cells.length;
        boardHeight = cells[0].length;
        bombs = new BombInfo [boardWidth] [boardHeight];
        bonuses = new boolean [boardWidth] [boardHeight];
        for (IPlayerSprite sprite : state.getPlayers())
        {
            bombRanges.put(sprite.getName(), Globals.DEFAULT_BOMB_RANGE);
        }
    }

    /**This is probably the most important part of the algorithm - it collects real-time
     * game information such as bonuses collected by opponents, the ranges of the bombs, etc.
     */
    private void collectBoardInfo()
    {
        if (!initialized) {
            initializeBoardInfo();
            initialized = true;
        }

        Cell [][] cells = state.getCells();

        // collect player position information
        Map<Point, String> playerAt = new HashMap<Point, String>();
        int maxBombRange = 0;
        for (IPlayerSprite sprite : state.getPlayers())
        {
            Point pos = toGrid(sprite.getPosition());
            String playerName = sprite.getName();
            playerAt.put(pos, playerName);

            if (!bombRanges.containsKey(playerName)) {
                bombRanges.put(playerName, Globals.DEFAULT_BOMB_RANGE);
            }
            int range = bombRanges.get(playerName);
            if (range > maxBombRange) maxBombRange = range;

            if (sprite.isDead())
            {
                // restore bomb range when player dies
                bombRanges.put(playerName, Globals.DEFAULT_BOMB_RANGE);
            }
        }

        // reusable point
        Point pos = new Point();

        // collect bomb information
        for (int y = 0; y < boardHeight; y++)
        {
            for (int x = 0; x < boardWidth; x++)
            {
                pos.setLocation(x, y);

                if (cells[x][y].type == CellType.CELL_BOMB)
                {
                    if (bombs[x][y] == null)
                    {
                        // a new bomb has just been planted
                        String bombOwner = playerAt.get(pos);
                        int range = bombOwner != null ? bombRanges.get(bombOwner)
                            : maxBombRange;
                        bombs[x][y] = new BombInfo(frame, range,new Point(pos));
                    }
                }
                else if (cells[x][y].type.isExplosion())
                {
                    // the cell exploded, mark the cell as bomb-free
                    bombs[x][y] = null;
                }
                else if (cells[x][y].type == CellType.CELL_BONUS_RANGE)
                {
                    bonuses[x][y] = true;
                }
                else if (bonuses[pos.x][pos.y])
                {
                    // if if the player stepped onto a bonus cell
                    // increase his range and remove the bonus
                    bonuses[pos.x][pos.y] = false;
                    String playerName = playerAt.get(pos);
                    if (playerName != null) {
                        int newRange = bombRanges.get(playerName) + 1;
                        bombRanges.put(playerName, newRange);
                    }
                }
            }
        }

        propagateFuseCounters();
    }

    /**Evaluate the real bomb detonation times. */
    private void propagateFuseCounters()
    {
        List<BombInfo> bombList = new ArrayList<BombInfo>();
        
        for (int y = 0; y < boardHeight; y++)
        {
            for (int x = 0; x < boardWidth; x++)
            {
                if (bombs[x][y] != null)
                    bombList.add(bombs[x][y]);
            }
        }
        Collections.sort(bombList,new Comparator<BombInfo>(){
            @Override
            public int compare(BombInfo o1, BombInfo o2)
            {
                return o1.plantFrame - o2.plantFrame;
            }});
        
        for (BombInfo bomb : bombList) {
            for (BombInfo otherBomb : bombList) {
                if (bomb.plantFrame < otherBomb.plantFrame)
                    if (CellUtils.isVisible(state.getCells(), bomb.pos, otherBomb.pos, bomb.range)) {
                        otherBomb.plantFrame = bomb.plantFrame;
                    }
            }
        }
    }

    private List<Point> opponentsAlive()
    {
        List<Point> opponents = new ArrayList<Point>();
        for (IPlayerSprite p : state.getPlayers()) {
            if (p.isDead() || p.isImmortal())
                continue;
            if (p.getName().equals(playerName))
                continue;
            opponents.add(toGrid(p.getPosition()));            
        }
        return opponents;
    }

    private boolean isVisible(Point me, List<Point> opponents)
    {
        int myRange = bombRanges.get(playerName);
        for (Point dst : opponents)
        {
            if (CellUtils.isVisible(state.getCells(), me, dst,myRange)) return true;
        }
        return false;
    }
    
    /** A structure that stores minimum distances to the specific types of objects */
    private static class Distances {
        public long bonus = 1000;
        public long opponent = 1000;
        public long safeCell = 1000;
    }
}
