package com.dawidweiss.dyna;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Everything required to render the game's playfield during a single frame.
 */
public final class GameStateEvent extends GameEvent
{
    /**
     * @see GameEvent#serialVersionUID
     */
    private static final long serialVersionUID = 0x200812241355L;

    /**
     * Board cells in the frame in which this event was dispatched.
     */
    private transient Cell [][] cells;
    
    /**
     * Player positions in the frame in which this event was dispatched.
     */
    private transient List<? extends IPlayerSprite> players;

    /*
     * 
     */
    public GameStateEvent(Cell [][] cells, List<? extends IPlayerSprite> players)
    {
        super(GameEvent.Type.GAME_STATE);

        this.cells = cells;
        this.players = players;
    }
    
    /*
     * 
     */
    public Cell [][] getCells()
    {
        return cells;
    }
    
    /*
     * 
     */
    public List<? extends IPlayerSprite> getPlayers()
    {
        return players;
    }

    /**
     * Custom deserialization code.
     */
    private void readObject(java.io.ObjectInputStream stream) 
        throws IOException, ClassNotFoundException
    {
        /*
         * Read all non transient fields.
         */
        stream.defaultReadObject();

        /*
         * Read cells.
         */
        final int w = stream.readShort();
        final int h = stream.readShort();

        final short [] ca = (short []) stream.readObject();
        final Cell [][] cells = new Cell [w][];
        for (int c = 0; c < w; c++)
        {
            cells[c] = new Cell [h];
        }

        for (int r = 0; r < h; r++)
        {
            for (int c = 0; c < w; c++)
            {
                final short v = ca[c + r * w];
                final int type = v & 0x7f;
                final Cell cell = Cell.getInstance(CellType.valueOf(type));
                cell.counter = v >>> 7;
                cells[c][r] = cell; 
            }
        }
        this.cells = cells;

        /*
         * Read players data.
         */
        final IPlayerSprite [] structs = (IPlayerSprite []) stream.readObject();
        this.players = Arrays.asList(structs);
    }

    // TODO: refactor timer code.

    /**
     * Custom serialization code.
     */
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException
    {
        /*
         * Write all non transient fields.
         */
        stream.defaultWriteObject();

        /*
         * Write cell data.
         */
        final int w = cells.length;
        final int h = cells[0].length;

        stream.writeShort(w);
        stream.writeShort(h);

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
        stream.writeObject(ca);

        /*
         * Write players and their positions.
         */
        final IPlayerSprite [] structs = new IPlayerSprite [players.size()];
        int index = 0;
        for (IPlayerSprite player : players)
        {
            PlayerSpriteImpl p = new PlayerSpriteImpl(player.getType(), player.getName());
            p.position.setLocation(player.getPosition());
            p.animationFrame = player.getAnimationFrame();
            p.animationState = player.getAnimationState();

            structs[index] = p;
            index++;
        }
        stream.writeObject(structs);
    }
}
