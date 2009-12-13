package org.jdyna.view.jme.adapter;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jdyna.BoardInfo;
import org.jdyna.Cell;
import org.jdyna.CellType;
import org.jdyna.GameEvent;
import org.jdyna.GameStartEvent;
import org.jdyna.GameStateEvent;
import org.jdyna.IGameEventListener;
import org.jdyna.IPlayerSprite;

public class JDynaGameAdapter implements IGameEventListener
{
    private boolean gameStarted;
    private final BlockingQueue<GameStateEvent> eventQueue = new LinkedBlockingQueue<GameStateEvent>(
        3);
    private int boardWidth;
    private int boardHeight;
    private Cell [][] cells;
    private Map<String, Boolean> playersAlive;
    private BoardInfo boardInfo;

    public JDynaGameAdapter()
    {
    }

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        for (GameEvent evt : events)
        {
            if (evt instanceof GameStartEvent)
            {
                GameStartEvent start = (GameStartEvent) evt;
                boardInfo = start.getBoardInfo();
            }
            else if (evt instanceof GameStateEvent)
            {
                GameStateEvent state = (GameStateEvent) evt;

                try
                {
                    eventQueue.put(state);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                /*
                 * boolean overflow = !eventQueue.offer(state); if (overflow)
                 * Logger.getLogger("com.arturklopotek.jdyna") .log(Level.WARNING,
                 * "Event queue overflow: event dropped");
                 */
            }
        }
    }

    protected GameStateEvent nextEvent()
    {
        try
        {
            return eventQueue.take();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void dispatchEvents(GameListener l,boolean wait)
    {
        if (!wait && eventQueue.isEmpty()) return;

        GameStateEvent state = nextEvent();

        if (gameStarted)
        {
            // dispatch in-game events
            generateEvents(state, l);
        }
        else
        {
            // dispatch gameStarted event
            cells = state.getCells();
            
            boardWidth = cells.length;
            boardHeight = cells[0].length;
            CellType[][] adapted = adaptCells(cells);
            l.gameStarted(adapted, boardWidth, boardHeight);
            gameStarted = true;
            playersAlive = new HashMap<String, Boolean>();
            System.out.println("game started");
        }
    }

    private static CellType [][] adaptCells(Cell [][] cells)
    {
        int w = cells.length;
        int h = cells[0].length;
        
        CellType adapted[][] = new CellType[w][h];
        
        for (int i = 0; i < w; i++)
        {
            for (int j = 0; j < h; j++)
            {
                CellType cell;
                switch (cells[i][j].type) {
                    case CELL_EMPTY:
                        cell = CellType.CELL_EMPTY;
                        break;
                    case CELL_BOMB:
                        cell = CellType.CELL_BOMB;
                        break;
                    case CELL_CRATE:
                    case CELL_CRATE_OUT:
                        cell = CellType.CELL_CRATE;
                        break;
                    case CELL_WALL:
                        cell = CellType.CELL_WALL;
                        break;
                    case CELL_BONUS_BOMB:
                        cell = cells[i][j].type;
                        break;
                    default:
                    	System.err.println("Unknown cell type: "+cells[i][j].type);
                    	cell = null;
                        //throw new RuntimeException("Unknown cell type: "+cells[i][j].type);
                }
                adapted[i][j] = cell;
            }
        }
        return adapted;
    }

    private void generateEvents(GameStateEvent state, GameListener l)
    {
        Cell [][] newCells = new Cell[boardWidth][];
        for (int i = 0; i < boardWidth; i++)
            	newCells[i] = state.getCells()[i].clone();
        
        Point pos = new Point();
        
        for (int i = 0; i < boardWidth; i++)
        {
            for (int j = 0; j < boardHeight; j++)
            {
                Cell prev = cells[i][j];
                Cell cur = newCells[i][j];
                pos.setLocation(i, j);

                if (cur.type == CellType.CELL_BONUS_RANGE
                    && prev.type != CellType.CELL_BONUS_RANGE)
                {
                    l.bonusSpawned(i, j, CellType.CELL_BONUS_RANGE);
                    System.out.println("bonus spawned (extra range)");
                } 
                else if (cur.type == CellType.CELL_BONUS_BOMB
                    && prev.type != CellType.CELL_BONUS_BOMB)
                {
                    l.bonusSpawned(i, j, CellType.CELL_BONUS_BOMB);
                    System.out.println("bonus spawned (extra bomb)");
                }
                else if (cur.type != prev.type
                    && (prev.type == CellType.CELL_BONUS_RANGE
                    		|| prev.type == CellType.CELL_BONUS_BOMB
                    		|| prev.type == CellType.CELL_BONUS_BOMB_WALKING
                    		|| prev.type == CellType.CELL_BONUS_CONTROLLER_REVERSE
                    		|| prev.type == CellType.CELL_BONUS_CRATE_WALKING
                    		|| prev.type == CellType.CELL_BONUS_DIARRHEA
                    		|| prev.type == CellType.CELL_BONUS_IMMORTALITY
                    		|| prev.type == CellType.CELL_BONUS_MAXRANGE
                    		|| prev.type == CellType.CELL_BONUS_NO_BOMBS
                    		|| prev.type == CellType.CELL_BONUS_RANGE
                    		|| prev.type == CellType.CELL_BONUS_SLOW_DOWN
                    		|| prev.type == CellType.CELL_BONUS_SPEED_UP
                    	)
                )
                {
                    l.bonusTaken(i, j);
                    System.out.println("bonus taken");
                }
                else if ((cur.type != CellType.CELL_CRATE
                		&& prev.type == CellType.CELL_CRATE)
                    || (cur.type != CellType.CELL_CRATE_OUT
                    		&& prev.type == CellType.CELL_CRATE_OUT))
                {
                    System.out.println("create destroyed "+pos);
                    l.crateDestroyed(i, j);
                }
                else if (cur.type == CellType.CELL_BOMB && prev.type != CellType.CELL_BOMB)
                {
                    l.bombPlanted(i, j);
                    System.out.println("bomb added");
                }
                else if (cur.type != CellType.CELL_BOMB
                    && prev.type == CellType.CELL_BOMB)
                {
                    int range[] = explosionRange(newCells,i,j);
                    l.bombExploded(i, j, range[0],range[1],range[2],range[3]);
                    debug("bomb exploded");
                }
                else if ((cur.type == CellType.CELL_BONUS_BOMB_WALKING
                			&& prev.type != CellType.CELL_BONUS_BOMB_WALKING)
                		|| (cur.type == CellType.CELL_BONUS_CONTROLLER_REVERSE
                        	&& prev.type != CellType.CELL_BONUS_CONTROLLER_REVERSE)
                        || (cur.type == CellType.CELL_BONUS_CRATE_WALKING
                        	&& prev.type != CellType.CELL_BONUS_CRATE_WALKING)
                        || (cur.type == CellType.CELL_BONUS_DIARRHEA
                        	&& prev.type != CellType.CELL_BONUS_DIARRHEA)
                        || (cur.type == CellType.CELL_BONUS_IMMORTALITY
                        	&& prev.type != CellType.CELL_BONUS_IMMORTALITY)
                        || (cur.type == CellType.CELL_BONUS_MAXRANGE
                        	&& prev.type != CellType.CELL_BONUS_MAXRANGE)
                        || (cur.type == CellType.CELL_BONUS_NO_BOMBS
                        	&& prev.type != CellType.CELL_BONUS_NO_BOMBS)
                        || (cur.type == CellType.CELL_BONUS_SLOW_DOWN
                        	&& prev.type != CellType.CELL_BONUS_SLOW_DOWN)
                        || (cur.type == CellType.CELL_BONUS_SPEED_UP
                        	&& prev.type != CellType.CELL_BONUS_SPEED_UP)
                        || (cur.type == CellType.CELL_BONUS_AHMED
                        	&& prev.type != CellType.CELL_BONUS_AHMED)
                )
                {
                	l.bonusSpawned(i, j, null);
                    System.out.println("other bonus spawned");
                }
                else if (cur.type == CellType.CELL_CRATE
                        && prev.type != CellType.CELL_CRATE)
                {
                	l.crateCreated(i, j);
                 	System.out.println("create create "+pos);
                }
            }
        }

        for (IPlayerSprite player : state.getPlayers())
        {

            String name = player.getName();
            Point p = player.getPosition();
            float px = (float) p.x / boardInfo.cellSize - .5f;
            float py = (float) p.y / boardInfo.cellSize - .5f;

            Boolean wasAlive = playersAlive.get(name);
            boolean isAlive = !player.isDead();

            if (isAlive)
            {
                if (Boolean.TRUE.equals(wasAlive))
                {
                    l.playerMoved(name, px, py, player.isImmortal());
                }
                else
                {
                    l.playerSpawned(name, p.x, p.y, wasAlive == null);
                    debug("player spawned");
                }
            }
            else
            {
                if (Boolean.TRUE.equals(wasAlive)) {
                    l.playerDied(name);
                    debug("player died");
                }
            }

            playersAlive.put(name, !player.isDead());
        }

        cells = newCells;
    }

    private static void debug(String string)
    {
    	System.out.println(string);
    }

    private static int[] explosionRange(Cell [][] cells, int i, int j)
    {
        int range[] = new int[4];
        for (int x=i-1;cells[x][j].type.isExplosion();range[0]++,x--);
        for (int x=i+1;cells[x][j].type.isExplosion();range[1]++,x++);
        for (int y=j-1;cells[i][y].type.isExplosion();range[2]++,y--);
        for (int y=j+1;cells[i][y].type.isExplosion();range[3]++,y++);
        return range;
    }
}
