package org.jdyna;

import java.awt.Point;
import java.io.Serializable;

/**
 * Metadata concerning bomb explosion.
 */
public final class ExplosionMetadata implements Serializable
{
    /**
     * @see GameEvent#serialVersionUID
     */
    private static final long serialVersionUID = 0x200912130232L;

    /** Explosion epicenter (bomb location). */
    private Point position;
    
    /** The bomb that exploded. */
    private transient BombCell bombCell; 

    /** Nominal range of the explosion in all directions. */
    private int range;

    // TODO: should we serialize actual explosion distance in all directions?

    public ExplosionMetadata(int x, int y, BombCell c)
    {
        this.position = new Point(x, y);
        this.bombCell = c;
        this.range = c.range;
    }

    /**
     * @return Explosion epicenter (bomb location). 
     */
    public Point getPosition()
    {
        return position;
    }

    /**
     * @return The bomb that exploded or <code>null</code> for remote clients
     * (not serialized).
     */
    public BombCell getBombCell()
    {
        return bombCell;
    }
    
    /**
     * @return Nominal explosion range in all directions.
     */
    public int getRange()
    {
        return range;
    }
}
