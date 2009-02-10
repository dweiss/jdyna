package com.krzysztofkazmierczyk.dyna;

import java.awt.Point;

/** This class adds to Point Frame NO */
@SuppressWarnings("serial")
public class CellWithTime extends Point
{
    private int frameNO;

    public CellWithTime(int x, int y, int frameNO)
    {
        super(x,y);
        this.frameNO = frameNO;
    }

    public int getFrameNO()
    {
        return frameNO;
    }

    public void setFrameNO(int frameNO)
    {
        this.frameNO = frameNO;
    }
    
}
