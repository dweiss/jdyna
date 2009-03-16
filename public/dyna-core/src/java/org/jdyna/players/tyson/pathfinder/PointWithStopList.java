package org.jdyna.players.tyson.pathfinder;

import java.util.List;

import com.google.common.collect.Lists;
import org.jdyna.players.tyson.emulator.gamestate.PointCoord;

/**
 * Class representing a point with additional list of points where we stopped on the way
 * to get there.
 * 
 * @author Bartosz Wesołowski
 */
@SuppressWarnings("serial")
class PointWithStopList extends PointCoord
{
    public final List<PointCoord> stopPoints;

    public PointWithStopList(final PointCoord point)
    {
        super(point);
        stopPoints = Lists.newLinkedList();
    }

    public PointWithStopList(final Neighbor point, final List<PointCoord> stopPoints)
    {
        super(point);
        this.stopPoints = Lists.newLinkedList(stopPoints);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        PointWithStopList other = (PointWithStopList) obj;
        if (stopPoints == null)
        {
            if (other.stopPoints != null) return false;
        }
        else if (!stopPoints.equals(other.stopPoints)) return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "[" + x + ", " + y + "]";
    }

    public boolean stoppedAt(final PointCoord point)
    {
        return stopPoints.contains(point);
    }

}
