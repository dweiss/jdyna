package org.jdyna.players.stalker;

import java.awt.Point;
import org.jdyna.IPlayerController.Direction;

@SuppressWarnings("serial")
class PointS extends Point
{
    public Direction move = null;
    public int x;
    public int y;
    public int depth = 0;
    public boolean mayBeDeadEnd = false;

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        final PointS other = (PointS) obj;
        if (x != other.x) return false;
        if (y != other.y) return false;
        return true;
    }

    public PointS(Point p)
    {
        super(p.x, p.y);
        x = p.x;
        y = p.y;
    }

    public PointS(PointS p)
    {
        super(p.x, p.y);
        this.x = p.x;
        this.y = p.y;
        this.move = p.move;
        this.depth = p.depth;
        this.mayBeDeadEnd = p.mayBeDeadEnd;
    }

    public PointS(int x, int y, Direction move, int depth)
    {
        super(x, y);
        this.x = x;
        this.y = y;
        this.move = move;
        this.depth = depth;
    }

    public PointS(int x, int y, Direction move, int depth, boolean mayBeDeadEnd)
    {
        super(x, y);
        this.x = x;
        this.y = y;
        this.move = move;
        this.depth = depth;
        this.mayBeDeadEnd = mayBeDeadEnd;
    }
}
