package org.jdyna.view.jme.adapter;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdyna.BoardInfo;
import org.jdyna.Cell;
import org.jdyna.CellType;
import org.jdyna.GameEvent;
import org.jdyna.GameStartEvent;
import org.jdyna.GameStateEvent;
import org.jdyna.IGameEventListener;
import org.jdyna.IPlayerSprite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class JDynaGameAdapter implements IGameEventListener
{
    private final static Logger logger = LoggerFactory.getLogger(JDynaGameAdapter.class);

    private boolean gameStarted;
    private final ArrayDeque<GameStateEvent> eventQueue = new ArrayDeque<GameStateEvent>();
    private int boardWidth;
    private int boardHeight;
    private Cell [][] cells;
    private Cell [][] newCells;
    private final List<IPlayerSprite> statePlayers = Lists.newArrayList();
    private Map<String, Boolean> playersAlive;
    private BoardInfo boardInfo;

    /**
     * Supported bonuses in this view.
     */
    private final EnumSet<CellType> supportedBonuses = EnumSet.of(
        CellType.CELL_BONUS_BOMB, 
        CellType.CELL_BONUS_RANGE,
        CellType.CELL_BONUS_NO_BOMBS,
        CellType.CELL_BONUS_IMMORTALITY,
        CellType.CELL_BONUS_CRATE_WALKING,
        CellType.CELL_BONUS_CONTROLLER_REVERSE,
        CellType.CELL_BONUS_AHMED,
        CellType.CELL_BONUS_MAXRANGE, 
        CellType.CELL_BONUS_SPEED_UP, 
        CellType.CELL_BONUS_DIARRHEA,
        CellType.CELL_BONUS_SLOW_DOWN,
        CellType.CELL_BONUS_BOMB_WALKING,
        CellType.CELL_BONUS_SURPRISE
    );
    
    /**
     * Not supported bonuses in this view (i.e. they doesn't have models/textures) 
     */
    //private final EnumSet<CellType> otherBonuses = EnumSet<CellType>();


    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        for (GameEvent evt : events)
        {
            if (evt instanceof GameStartEvent)
            {
                GameStartEvent start = (GameStartEvent) evt;
                boardInfo = start.getBoardInfo();
                boardWidth = boardInfo.gridSize.width;
                boardHeight = boardInfo.gridSize.height;
                newCells = new Cell [boardWidth][boardHeight];
            }
            else if (evt instanceof GameStateEvent)
            {
                GameStateEvent state = (GameStateEvent) evt;
                synchronized (eventQueue)
                {
                    if (!eventQueue.isEmpty())                    
                    {
                        // Re-enable this if you want information about dropped events.
                        // logger.info("Dropping events: " + eventQueue.size());
                        eventQueue.clear();
                    }

                    // FIXME: storing this reference directly is a bug. Make a shallow
                    // (static) copy of all the data required for processing and then
                    // place it on the queue.
                    eventQueue.add(state);
                }
            }
        }
    }

    public void dispatchEvents(GameListener l)
    {
        GameStateEvent state;
        synchronized (eventQueue)
        {
            if (eventQueue.isEmpty())
                return;
            state = eventQueue.pop();

            for (int x = 0; x < boardWidth; x++)
                for (int y = 0; y < boardHeight; y++)
                    newCells[x][y] = state.getCells()[x][y];

            statePlayers.clear();
            for (IPlayerSprite player : state.getPlayers())
            {
                statePlayers.add(player.clone());
            }
        }

        if (gameStarted)
        {
            // dispatch in-game events
            generateEvents(state, l);
        }
        else
        {
            // dispatch gameStarted event
            cells = state.getCells();
            CellType[][] adapted = adaptCells(cells);
            l.gameStarted(adapted, boardWidth, boardHeight);
            gameStarted = true;
            playersAlive = new HashMap<String, Boolean>();
            logger.debug("Game started");
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
                    case CELL_CRATE:
                    case CELL_CRATE_OUT:
                        cell = CellType.CELL_CRATE;
                        break;
                    case CELL_WALL:
                    case CELL_EMPTY:
                    case CELL_BOMB:
                    case CELL_BONUS_BOMB:
                    case CELL_BONUS_RANGE:
                    case CELL_BONUS_NO_BOMBS:
                    case CELL_BONUS_IMMORTALITY:
                    case CELL_BONUS_CRATE_WALKING:
                    case CELL_BONUS_CONTROLLER_REVERSE:
                    case CELL_BONUS_AHMED:
                    case CELL_BONUS_MAXRANGE: 
                    case CELL_BONUS_SPEED_UP: 
                    case CELL_BONUS_DIARRHEA:
                    case CELL_BONUS_SLOW_DOWN:
                    case CELL_BONUS_BOMB_WALKING:
                    case CELL_BONUS_SURPRISE:
                        cell = cells[i][j].type;
                        break;
                    default:
                        logger.error("Unknown cell type: " + cells[i][j].type);
                        cell = null;
                        //throw new RuntimeException("Unknown cell type: "+cells[i][j].type);
                }
                adapted[i][j] = cell;
            }
        }
        return adapted;
    }

    /**
     * Determine if supported bonus has been spawned on the specified cell.
     * 
     * @param current Current state of the cell
     * @param previous Previous state of the cell
     * @return Returns type of spawned supported bonus, otherwise <code>null</code>.
     */
    private CellType bonusSpawned(Cell current, Cell previous)
    {
        if (supportedBonuses.contains(current.type) && previous.type != current.type) 
            return current.type;
        return null;
    }

    /**
     * Determine if bonus has been taken from the specified cell.
     * 
     * @param current Current state of the cell
     * @param previous Previous state of the cell
     * @return Type of taken bonus or <code>null</code> if not taken.
     */
    private CellType bonusTaken(Cell current, Cell previous)
    {
        if ((supportedBonuses.contains(previous.type) /*|| otherBonuses
            .contains(previous.type)*/) && current.type != previous.type) 
            return previous.type;
        return null;
    }

    /**
     * Determine if crate has been destroyed on the specified cell.
     * 
     * @param current Current state of the cell
     * @param previous Previous state of the cell
     * @return Returns <code>true</code> if crate has been destroyed.
     */
    private boolean isCrateDestroyedEvent(Cell current, Cell previous)
    {
        if ((current.type != CellType.CELL_CRATE && previous.type == CellType.CELL_CRATE)
            || (current.type != CellType.CELL_CRATE_OUT && previous.type == CellType.CELL_CRATE_OUT)) 
            return true;
        return false;
    }

    /**
     * Determine if bomb has been planted on the specified cell.
     * 
     * @param current Current state of the cell
     * @param previous Previous state of the cell
     * @return Returns <code>true</code> if bomb has been planted.
     */
    private boolean isBombPlantedEvent(Cell current, Cell previous)
    {
        if (current.type == CellType.CELL_BOMB && previous.type != CellType.CELL_BOMB) 
            return true;
        return false;
    }

    /**
     * Determine if bomb has exploded on the specified cell.
     * 
     * @param current Current state of the cell
     * @param previous Previous state of the cell
     * @return Returns <code>true</code> if bomb has exploded.
     */
    private boolean isBombExplodedEvent(Cell current, Cell previous)
    {
        if (current.type != CellType.CELL_BOMB && previous.type == CellType.CELL_BOMB) 
            return true;
        return false;
    }

    /**
     * Determine if other bonus has been spawned on the specified cell.
     * 
     * @param current Current state of the cell
     * @param previous Previous state of the cell
     * @return Returns type of spawned other bonus, otherwise <code>null</code>.
     */
    /*private CellType otherBonusSpawned(Cell current, Cell previous)
    {
        if (otherBonuses.contains(current.type) && previous.type != current.type) 
            return current.type;
        return null;
    }*/

    /**
     * Determine if crate has appeared on the specified cell.
     * 
     * @param current Current state of the cell
     * @param previous Previous state of the cell
     * @return Returns <code>true</code> if crate has appeared.
     */
    private boolean isCrateCreatedEvent(Cell current, Cell previous)
    {
        if (current.type == CellType.CELL_CRATE && previous.type != CellType.CELL_CRATE) 
            return true;
        return false;
    }
    
    private void generateEvents(GameStateEvent state, GameListener l)
    {
        Point pos = new Point();
        
        for (int i = 0; i < boardWidth; i++)
        {
            for (int j = 0; j < boardHeight; j++)
            {
                Cell prev = cells[i][j];
                Cell cur = newCells[i][j];
                pos.setLocation(i, j);

                CellType bonusCell;
                if ((bonusCell = bonusSpawned(cur, prev)) != null)
                {
                    l.bonusSpawned(i, j, bonusCell);
                    logger.debug("Bonus spawned: " + bonusCell.toString());
                }
                else if ((bonusCell = bonusTaken(cur, prev)) != null)
                {
                    l.bonusTaken(i, j);
                    logger.debug("Bonus taken: " + bonusCell.toString());
                }
                else if (isCrateDestroyedEvent(cur, prev))
                {
                    logger.debug("Crate destroyed " + pos);
                    l.crateDestroyed(i, j);
                }
                else if (isBombExplodedEvent(cur, prev))
                {
                    int range[] = explosionRange(newCells, i, j);
                    l.bombExploded(i, j, range[0], range[1], range[2], range[3]);
                    logger.debug("Bomb exploded");
                }
                /*
                 * else if ((bonusCell = otherBonusSpawned(cur, prev)) != null) {
                 * l.bonusSpawned(i, j, null); logger.debug("Other bonus spawned: " +
                 * bonusCell.toString()); }
                 */
                else if (isCrateCreatedEvent(cur, prev))
                {
                    l.crateCreated(i, j);
                    logger.debug("Crate created " + pos);
                }
                if (isBombPlantedEvent(cur, prev))
                {
                    l.bombPlanted(i, j);
                    logger.debug("Bomb added");
                }
            }
        }

        for (IPlayerSprite player : statePlayers)
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
                    logger.debug("player spawned");
                }
            }
            else
            {
                if (Boolean.TRUE.equals(wasAlive)) {
                    l.playerDied(name);
                    logger.debug("Player died");
                }
            }

            playersAlive.put(name, !player.isDead());
        }
        cells = newCells.clone();        
        for (int i=0; i<boardWidth;i++)
            cells[i] = newCells[i].clone();
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
