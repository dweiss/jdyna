package com.krzysztofkazmierczyk.dyna;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jdyna.*;

import com.krzysztofkazmierczyk.dyna.client.game.Utilities;

/**
 * This class extends {@code Board} class for everything useful for ai algorithm such as
 * time of explosion etc. Note: this class may not extend class {@code Board} because it
 * is final class.
 */
public class GameStateEventUpdater implements Cloneable
{

    private final static Logger logger = Logger.getLogger("GameStateEventAdapter");

    /**
     * This method is only used to cloning in GameStateEventUpdater so I will not move it
     * to {@link Utilities} clazz
     */
    private static List<PlayerInfo> clone(List<PlayerInfo> pi)
        throws CloneNotSupportedException
    {
        List<PlayerInfo> result = new ArrayList<PlayerInfo>(pi.size());

        for (PlayerInfo playerInfo : pi)
        {
            result.add((PlayerInfo) playerInfo.clone());
        }

        return result;
    }

    private final BoardInfo boardInfo;

    private Cell [][] cells;

    /** Number of last frame on the board */
    private int frameNO;

    private List<PlayerInfo> players;

    private GameStateEventUpdater(BoardInfo boardInfo, Cell [][] cells, int frameNO,
        List<PlayerInfo> players)
    {
        this.boardInfo = boardInfo;
        this.frameNO = frameNO;
        this.players = players;
        this.cells = cells;
    }

    public GameStateEventUpdater(BoardInfo boardInfo, GameStateEvent gameStateEvent,
        int frame)
    {
        this.boardInfo = boardInfo;
        this.frameNO = frame;

        this.cells = new Cell [gameStateEvent.getCells().length] [];

        for (int i = 0; i < gameStateEvent.getCells().length; i++)
        {

            this.cells[i] = new Cell [gameStateEvent.getCells()[i].length];

            for (int j = 0; j < gameStateEvent.getCells()[i].length; j++)
            {
                Cell currentCell = gameStateEvent.getCells()[i][j];

                if (currentCell.type == CellType.CELL_BOMB)
                {
                    // CELL_BOMB differs from other cell types.
                    this.cells[i][j] = new BombCell(frame);
                }
                else
                {
                    this.cells[i][j] = Cell.getInstance(currentCell.type);
                }
            }
        }

        this.players = new ArrayList<PlayerInfo>();

        for (IPlayerSprite sprite : gameStateEvent.getPlayers())
        {
            players.add(new PlayerInfo(sprite));
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        GameStateEventUpdater gseu = new GameStateEventUpdater(boardInfo, cells.clone(),
            frameNO, clone(players));
        return gseu;
    }

    public BoardInfo getBoardInfo()
    {
        return boardInfo;
    }

    public Cell [][] getCells()
    {
        return cells;
    }

    public int getFrameNO()
    {
        return frameNO;
    }

    public List<PlayerInfo> getPlayers()
    {
        return players;
    }

    public void setFrameNO(int frameNO)
    {
        this.frameNO = frameNO;
    }

    public synchronized void update(GameStateEvent gameStateEvent, int frameNO)
    {
        if (this.frameNO < frameNO)
        {
            // We got the newest frame. Search new bombs destroy which have just been
            // exploded and update player positions

            updateToNewPositions(gameStateEvent, frameNO);

            for (int i = 0; i < gameStateEvent.getPlayers().size(); i++)
            {
                IPlayerSprite sprite = gameStateEvent.getPlayers().get(i);
                if (sprite != null)
                {
                    players.get(i).update(sprite);
                }
                else
                {
                    // New player appeared on the board. 
                    players.add(new PlayerInfo(sprite));
                }
            }

            this.frameNO = frameNO;
        }
        else
        {
            // we got older frame. We can update times of putting bomb.
            updateDroppingBombTimes(gameStateEvent, frameNO);
        }
    }

    /**
     * Sets frameNO in BombCell on the board to older if on older frame is Bomb and in
     * Cell
     */
    private void updateDroppingBombTimes(GameStateEvent gameStateEvent, int oldFrameNO)
    {
        for (int i = 0; i < gameStateEvent.getCells().length; i++)
        {
            for (int j = 0; j < gameStateEvent.getCells()[i].length; j++)
            {
                final CellType previousCellType = gameStateEvent.getCells()[i][j].type;
                final CellType currentCellType = cells[i][j].type;

                if (currentCellType == CellType.CELL_BOMB
                    && previousCellType == CellType.CELL_BOMB
                    && ((BombCell) cells[i][j]).getFrameNO() > oldFrameNO)
                {
                    assert (cells[i][j] instanceof BombCell);
                    ((BombCell) cells[i][j]).setFrameNO(oldFrameNO);
                }

            }
        }
    }

    /** Sets everything what is new on board */
    private void updateToNewPositions(GameStateEvent gameStateEvent, int newFrameNO)
    {
        for (int i = 0; i < gameStateEvent.getCells().length; i++)
        {
            for (int j = 0; j < gameStateEvent.getCells()[i].length; j++)
            {
                final CellType newCellType = gameStateEvent.getCells()[i][j].type;
                final CellType oldCellType = cells[i][j].type;

                final Integer playerIDOnCell = Utilities.playerIDOnTheCell(boardInfo,
                    new Point(i, j), gameStateEvent.getPlayers());
                final PlayerInfo playerOnCell = playerIDOnCell != null ? players
                    .get(playerIDOnCell) : null;

                if (newCellType.equals(oldCellType))
                {
                    // If nothing has changed, do nothing
                    continue;
                }

                if (newCellType == CellType.CELL_BONUS_BOMB
                    || newCellType == CellType.CELL_BONUS_RANGE)
                {
                    // If new bonus cell appeared, add it to cells
                    cells[i][j] = Cell.getInstance(newCellType);
                    continue;
                }

                if (newCellType == CellType.CELL_BOMB)
                {
                    // One of players dropped bomb here
                    final int bombRange = playerIDOnCell != null ? playerOnCell
                        .getBombRange() : Globals.DEFAULT_BOMB_RANGE;

                    if (playerOnCell != null)
                    {
                        playerOnCell.droppedBomb();
                    }

                    cells[i][j] = new BombCell(playerIDOnCell, newFrameNO, bombRange);

                    continue;
                }

                if (oldCellType == CellType.CELL_BOMB && newCellType.isExplosion())
                {
                    // bomb has dropped

                    final Cell cell = cells[i][j];

                    assert (cell instanceof BombCell);

                    final BombCell bombCell = (BombCell) cell;

                    final Integer dropperID = bombCell.getPlayerId();
                    if (dropperID != null)
                    {
                        players.get(dropperID).explodedBomb();
                    }

                    bombCell.setExplosionFrameNO(Math.min(frameNO, bombCell
                        .getExplosionFrameNO()));

                    // cells[i][j] = Cell.getInstance(newCellType);
                    continue;
                }

                if (oldCellType == CellType.CELL_BONUS_RANGE)
                {
                    // someone collected bonus
                    if (playerOnCell != null)
                    {
                        playerOnCell.collectedBonusRange();
                    }
                    cells[i][j] = Cell.getInstance(newCellType);
                    continue;
                }

                if (oldCellType == CellType.CELL_BONUS_BOMB)
                {
                    // someone collected bonus
                    if (playerOnCell != null)
                    {
                        playerOnCell.collectedBonusBomb();
                    }
                    cells[i][j] = Cell.getInstance(newCellType);
                    continue;
                }

                if (newCellType.isExplosion())
                {
                    final Cell cell = cells[i][j];

                    assert (cell instanceof BombCell);

                    if (cell instanceof BombCell)
                    {
                        final BombCell bombCell = (BombCell) cell;
                        bombCell.setExplosionFrameNO(Math.min(frameNO, bombCell
                            .getExplosionFrameNO()));

                    }

                    continue;
                }

                // finally if one of this action happened, set new cell type:
                cells[i][j] = Cell.getInstance(newCellType);
            }
        }
    }
}
