package com.dawidweiss.dyna.corba;

import java.awt.Dimension;
import java.awt.Point;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.IController;
import com.dawidweiss.dyna.IController.Direction;
import com.dawidweiss.dyna.corba.bindings.CBoardInfo;
import com.dawidweiss.dyna.corba.bindings.CBoardSnapshot;
import com.dawidweiss.dyna.corba.bindings.CControllerState;
import com.dawidweiss.dyna.corba.bindings.CDimension;
import com.dawidweiss.dyna.corba.bindings.CDirection;
import com.dawidweiss.dyna.corba.bindings.CPlayer;
import com.dawidweiss.dyna.corba.bindings.CPlayerState;
import com.dawidweiss.dyna.corba.bindings.CPoint;
import com.dawidweiss.dyna.view.BoardInfo;
import com.dawidweiss.dyna.view.IBoardSnapshot;
import com.dawidweiss.dyna.view.IPlayerSprite;

/**
 * Adapters between Corba and Java game structures.
 */
public final class Adapters
{
    Adapters()
    {
        // no instances.
    }

    public static CBoardInfo adapt(BoardInfo boardInfo)
    {
        return new CBoardInfo(adapt(boardInfo.gridSize), boardInfo.cellSize,
            adapt(boardInfo.pixelSize));
    }

    public static BoardInfo adapt(CBoardInfo boardInfo)
    {
        return new BoardInfo(adapt(boardInfo.gridSize), boardInfo.cellSize);
    }

    public static Dimension adapt(CDimension d)
    {
        return new Dimension(d.width, d.height);
    }

    public static CDimension adapt(Dimension d)
    {
        return new CDimension(d.width, d.height);
    }

    public static IController.Direction adapt(CDirection direction)
    {
        if (direction == null) return null;

        switch (direction.value())
        {
            case CDirection._NONE:
                return null;
            case CDirection._DOWN:
                return IController.Direction.DOWN;
            case CDirection._UP:
                return IController.Direction.UP;
            case CDirection._LEFT:
                return IController.Direction.LEFT;
            case CDirection._RIGHT:
                return IController.Direction.RIGHT;
        }

        throw new RuntimeException(/* unreachable */);
    }

    public static CBoardSnapshot adapt(IBoardSnapshot in)
    {
        final IPlayerSprite [] p = in.getPlayers();
        final CPlayerState [] players = new CPlayerState [p.length];
        for (int i = 0; i < p.length; i++)
        {
            players[i] = adapt(p[i]);
        }

        final short [] cells = adapt(in.getCells());

        return new CBoardSnapshot(cells, players);
    }

    public static IBoardSnapshot adapt(CBoardSnapshot snapshot, CBoardInfo info, CPlayer [] pNames)
    {
        final CPlayerState [] cplayers = snapshot.players;
        final IPlayerSprite [] players = new IPlayerSprite [cplayers.length];
        for (int i = 0; i < players.length; i++)
        {
            final PlayerSpriteImpl np = new PlayerSpriteImpl(i, pNames[i].name);
            np.position = adapt(cplayers[i].position);
            np.animationFrame = cplayers[i].animationFrame;
            np.animationState = cplayers[i].animationState;
            players[i] = np;
        }
        
        final int h = info.gridSize.height;
        final int w = info.gridSize.width;
        final short [] cdata = snapshot.cells;
        final Cell [][] cells = new Cell [w][];
        for (int r = 0; r < w; r++)
        {
            cells[r] = new Cell [h];
        }
        
        for (int r = 0; r < h; r++)
        {
            for (int c = 0; c < w; c++)
            {
                final short v = cdata[c + r * w];
                final int type = v & 0x7f;
                final Cell cell = Cell.getInstance(CellType.valueOf(type));
                cell.counter = v >>> 7;
                cells[c][r] = cell; 
            }
        }

        return new IBoardSnapshot()
        {
            public Cell [][] getCells()
            {
                return cells;
            }

            public IPlayerSprite [] getPlayers()
            {
                return players;
            }
        };
    }

    public static short [] adapt(Cell [][] cells)
    {
        final int w = cells.length;
        final int h = cells[0].length;
        final short [] ca = new short [w * h];
        for (int c = 0; c < w; c++)
        {
            for (int r = 0; r < h; r++)
            {
                final Cell cell = cells[c][r];
                final short v = (short) ((cell.counter << 7) | (cell.type.ordinal()));
                ca[c + r * w] = v;
            }
        }
        return ca;
    }

    public static CPlayerState adapt(IPlayerSprite player)
    {
        return new CPlayerState(player.getAnimationFrame(), player.getAnimationState(),
            adapt(player.getPosition()));
    }

    public static CPoint adapt(Point position)
    {
        return new CPoint(position.x, position.y);
    }

    public static Point adapt(CPoint position)
    {
        return new Point(position.x, position.y);
    }

    public static CControllerState adapt(IController controller)
    {
        final boolean dropsBomb = controller.dropsBomb();
        final IController.Direction direction = controller.getCurrent();
        return new CControllerState(adapt(direction), dropsBomb);
    }

    public static CDirection adapt(Direction direction)
    {
        if (direction == null) return CDirection.NONE;
        switch (direction)
        {
            case LEFT:
                return CDirection.LEFT;
            case RIGHT:
                return CDirection.RIGHT;
            case UP:
                return CDirection.UP;
            case DOWN:
                return CDirection.DOWN;
        }
        throw new RuntimeException();
    }
}
