package com.krzysztofkazmierczyk.dyna.client.game.wayFinder;

import java.awt.Point;

/**
 * This class contain information about cell such as time of arrive on the cell
 * and from which point.
 */
public class WayCell 
{

    private final Point cell;
    
    /** Predicted number of frame when we arrive the cell or infinity when unreachable */
    private Integer timeOfArrive = Integer.MAX_VALUE;
    
    /** Cell from which we can reach the fastest this cell */
    private Point cellOfArrive;

    public WayCell(Point cell)
    {
        super();
        this.cell = cell;
    }

    public Point getCell()
    {
        return cell;
    }

    public Point getCellOfArrive()
    {
        return cellOfArrive;
    }

    public Integer getTimeOfArrive()
    {
        return timeOfArrive;
    }

    /*package*/ void setCellOfArrive(Point cellOfArrive)
    {
        this.cellOfArrive = cellOfArrive;
    }

    /*package*/ void setTimeOfArrive(Integer timeOfArrive)
    {
        this.timeOfArrive = timeOfArrive;
    }

    public boolean isReachable()
    {
        return (timeOfArrive < Integer.MAX_VALUE);
    }
    
    @Override
    public String toString()
    {
        return timeOfArrive.toString();
    }

}
