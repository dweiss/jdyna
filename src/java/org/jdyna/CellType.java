package org.jdyna;

import java.util.EnumMap;
import java.util.EnumSet;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * A cell type contains the cell's numeric code and exposes several properties, including 
 * walkability, hostility against the player, etc.
 */
public enum CellType
{
    /* Empty cell. */
    CELL_EMPTY(' '),

    /* Simple wall. */
    CELL_WALL('#'),

    /* Crates (destroyable). */
    CELL_CRATE('X'), 
    CELL_CRATE_OUT('x'),
    CELL_RANDOM_CRATE('~'),

    /* Bomb. */
    CELL_BOMB('b'),

    /* Explosions. */
    CELL_BOOM_LX('<'), 
    CELL_BOOM_RX('>'), 
    CELL_BOOM_X('-'), 
    CELL_BOOM_TY('^'), 
    CELL_BOOM_BY('v'), 
    CELL_BOOM_Y('|'), 
    CELL_BOOM_XY('+'),

    /* Bonuses */
    CELL_BONUS_BOMB('@'),
    CELL_BONUS_RANGE('*'),
    CELL_BONUS_DIARRHEA('d'),
    CELL_BONUS_IMMORTALITY('i'),
    CELL_BONUS_MAXRANGE('m'),
    CELL_BONUS_NO_BOMBS('n'),
    CELL_BONUS_SPEED('s'),
    CELL_BONUS_CRATE_WALKING('c'),
    CELL_BONUS_BOMB_WALKING('q');

    /**
     * Character code for the cell (16 bits).
     */
    public final char code;

    /* */
    private CellType(char code)
    {
        this.code = code;
    }

    /**
     * Is the given type an explosion cell?
     */
    public boolean isExplosion()
    {
        return EXPLOSION_CELLS.contains(this);
    }

    /**
     * @return Returns <code>true</code> if this cell type is lethal.
     */
    public boolean isLethal()
    {
        return LETHAL.contains(this);
    }   

    /**
     * @return Returns <code>true</code> if this cell type is empty (players can walk on
     *         it).
     */
    public boolean isWalkable()
    {
    	return WALKABLES.contains(this);
    }
    
    /**
     * Return the frame counter after which the cell should be replaced with
     * {@link #CELL_EMPTY}. If zero, the cell should not be removed from the grid at all.
     */
    int getRemoveAtCounter()
    {
        final Integer i = ANIMATING_CELLS.get(this);
        return (i == null ? 0 : i);
    }

    /**
     * @return Return an enum instance for a cell's character code.
     * @throws IllegalArgumentException If the code does not exist.
     */
    public static CellType valueOf(char code)
    {
        final CellType c = CODE_TO_CELL_TYPE[code];
        if (c == null)
        {
            throw new RuntimeException("No cell type with code: " 
                + (int) code + " ('" + code + "')");
        }

        return CODE_TO_CELL_TYPE[code];
    }

    /**
     * Convert from enum's ordinal number to enum instance.
     */
    public static CellType valueOf(int ordinal)
    {
        return ORDINAL_TO_CELL_TYPE[ordinal];
    }

    /**
     * A static mapping between codes and enum constants. 64k is not a problem with
     * today's architecture, huh? (we don't care about unicode code points).
     * 
     * @see #valueOf(char)
     */
    private final static CellType [] CODE_TO_CELL_TYPE;
    static
    {
        CODE_TO_CELL_TYPE = new CellType [Character.MAX_VALUE];
        for (CellType c : CellType.values())
        {
            if (CODE_TO_CELL_TYPE[c.code] != null)
                throw new RuntimeException("Two cells have the same code.");

            CODE_TO_CELL_TYPE[c.code] = c;
        }
    }

    /**
     * A mapping from ordinal values to {@link CellType}.
     */
    private final static CellType [] ORDINAL_TO_CELL_TYPE;
    static
    {
        int max = 0;
        for (CellType t : CellType.values()) max = Math.max(max, t.ordinal());
        ORDINAL_TO_CELL_TYPE = new CellType [max + 1];
        for (CellType t : CellType.values()) ORDINAL_TO_CELL_TYPE[t.ordinal()] = t;
    }
    
    /**
     * A static set of all cells that are explosions.
     * 
     * @see #isExplosion()
     */
    private final static EnumSet<CellType> EXPLOSION_CELLS;
    static
    {
        EXPLOSION_CELLS = EnumSet.of(
            CellType.CELL_BOOM_BY, CellType.CELL_BOOM_TY,
            CellType.CELL_BOOM_Y, CellType.CELL_BOOM_X, CellType.CELL_BOOM_LX,
            CellType.CELL_BOOM_RX, CellType.CELL_BOOM_XY);
    }

    /**
     * All lethal (killing the player) cell types.
     */
    private final static EnumSet<CellType> LETHAL;
    static
    {
        LETHAL = Sets.newEnumSet(EXPLOSION_CELLS, CellType.class);
    }

    /**
     * All cells on which the player can walk. This does not mean that they are
     * safe, explosion cells are also walkable.
     */
    private final static EnumSet<CellType> WALKABLES;
    static
    {
        WALKABLES = EnumSet.of(CellType.CELL_EMPTY, CellType.CELL_BONUS_BOMB,
            CellType.CELL_BONUS_RANGE, CellType.CELL_BONUS_DIARRHEA, CellType.CELL_BONUS_NO_BOMBS,
            CellType.CELL_BONUS_MAXRANGE, CellType.CELL_BONUS_IMMORTALITY,
            CellType.CELL_BONUS_SPEED, CellType.CELL_BONUS_CRATE_WALKING,
            CellType.CELL_BONUS_BOMB_WALKING);
        WALKABLES.addAll(EXPLOSION_CELLS);
    }

    /**
     * All cells that are animated and should be replaced with {@link CellType#CELL_EMPTY}
     * at the end of the animation sequence.
     */
    private final static EnumMap<CellType, Integer> ANIMATING_CELLS;
    static
    {
        /*
         * Number of frames * frameRate. These values should be image-independent,
         * but we hardcode the constants used in classic dyna.
         */
        final int explosionFrameCount = 7 * 2;
        final int crateFrameCount = 7 * 2;

        ANIMATING_CELLS = Maps.newEnumMap(CellType.class);
        for (CellType c : EXPLOSION_CELLS)
        {
            ANIMATING_CELLS.put(c, explosionFrameCount);
        }
        ANIMATING_CELLS.put(CellType.CELL_CRATE_OUT, crateFrameCount);
    }    
}
