package org.jdyna;

import java.util.*;

import com.google.common.collect.*;

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
    /** Bonus that adds a bomb to the player's arsenal */
    CELL_BONUS_BOMB('@'),
    
    /** Bonus that increases range of player's bombs by one tile */
    CELL_BONUS_RANGE('*'),
    
    /** Disease that causes the player to drop bombs uncontrollably, as fast as possible */
    CELL_BONUS_DIARRHEA('d'),
    
    /** Bonus that grants the player temporary immunity to explosions
     * (and prevents him from picking up other bonuses) */
    CELL_BONUS_IMMORTALITY('i'),
    
    /** Bonus that temporarily increases range of player's bombs to infinity */
    CELL_BONUS_MAXRANGE('m'),
    
    /** Dissease that prevents the player from dropping bombs. */
    CELL_BONUS_NO_BOMBS('n'),
    
    /** Bonus that temporarily increases the player's speed */
    CELL_BONUS_SPEED_UP('u'),
    
    /** Disease that temporarily decreases the player's speed */
    CELL_BONUS_SLOW_DOWN('s'),
    
    /** Bonus that gives the player temporal ability to walk through crates */
    CELL_BONUS_CRATE_WALKING('c'),
    
    /** Bonus that gives the player temporal ability to walk through bombs */
    CELL_BONUS_BOMB_WALKING('q'),
    
    /** Disease that reverses the player's movement direction */
    CELL_BONUS_CONTROLLER_REVERSE('r'),
    
    /** Bonus that causes the next bomb dropped by the player to explode immediately,
     *  killing only other players */
    CELL_BONUS_AHMED('a'),
    
    /** A bonus that gives the player a randomly selected bonus or disease */
    CELL_BONUS_SURPRISE('e');

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
            CellType.CELL_BONUS_SPEED_UP, CellType.CELL_BONUS_SLOW_DOWN, 
            CellType.CELL_BONUS_CRATE_WALKING, CellType.CELL_BONUS_BOMB_WALKING,
            CellType.CELL_BONUS_CONTROLLER_REVERSE, CellType.CELL_BONUS_AHMED,
            CELL_BONUS_SURPRISE);
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
    
    /**
     * Bonus cells and their weights for calculating probability of selection
     * for random placement of bonuses.
     */
    private final static EnumMap<CellType, Integer> BONUS_WEIGHTS;
    private final static List<CellType> BONUSES_NO_SURPRISE;
    private final static int sumOfWeights;
    private final static Random rnd = new Random();

    static
    {
        BONUS_WEIGHTS = Maps.newEnumMap(CellType.class);

        /*
         * We divide bonus probabilities into three categories for now.
         * I believe some of the bonuses may be outside of the default three
         * categories and may be assigned more/ less frequently (e.g., immortality). 
         */

        for (CellType ct : EnumSet.of(
            CELL_BONUS_BOMB, 
            CELL_BONUS_RANGE))
        {
            BONUS_WEIGHTS.put(ct, Constants.FREQUENT_BONUS_WEIGHT);
        }

        for (CellType ct : EnumSet.of(
            CELL_BONUS_MAXRANGE, 
            CELL_BONUS_SPEED_UP, 
            CELL_BONUS_CRATE_WALKING, 
            CELL_BONUS_AHMED))
        {
            BONUS_WEIGHTS.put(ct, Constants.COMMON_BONUS_WEIGHT);
        }

        for (CellType ct : EnumSet.of(
            CELL_BONUS_DIARRHEA, 
            CELL_BONUS_NO_BOMBS,
            CELL_BONUS_IMMORTALITY,
            CELL_BONUS_SLOW_DOWN,
            CELL_BONUS_BOMB_WALKING,
            CELL_BONUS_CONTROLLER_REVERSE,
            CELL_BONUS_SURPRISE))
        {
            BONUS_WEIGHTS.put(ct, Constants.COMMON_BONUS_WEIGHT);
        }

        // Static, random-access view of all possible bonuses.
        BONUSES_NO_SURPRISE = Lists.newArrayList(BONUS_WEIGHTS.keySet());
        BONUSES_NO_SURPRISE.remove(CELL_BONUS_SURPRISE);

        // Sum of all weights for assignment probability distribution.
        int sum = 0;
        for (CellType ct : BONUS_WEIGHTS.keySet())
        {
            sum += BONUS_WEIGHTS.get(ct);
        }
        sumOfWeights = sum;
    }

    /**
     * Return a random surprise bonus (random bonus, but not {@link #CELL_BONUS_SURPRISE}). 
     */
    public static CellType surpriseBonus()
    {
        return BONUSES_NO_SURPRISE.get(rnd.nextInt(BONUSES_NO_SURPRISE.size()));
    }
    
    /**
     * Return a random bonus, according to probability distribution given by weights
     * of each cell type. 
     */
    public static CellType randomBonus()
    {
        final int t = rnd.nextInt(sumOfWeights);
        int s = 0;
        for (CellType ct : BONUS_WEIGHTS.keySet())
        {
            final int v = BONUS_WEIGHTS.get(ct);
            if (t >= s && t < s + v)
                return ct;
            s += v;
        }
        throw new RuntimeException("Unreachable block: "
            + t + ", " + s);
    }
}