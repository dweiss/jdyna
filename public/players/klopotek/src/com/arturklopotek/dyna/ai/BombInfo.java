package com.arturklopotek.dyna.ai;

import java.awt.Point;

/** A "structure" class storing bomb information such as fuse counter, 
 * its range and position on the board. 
 */
public class BombInfo
{
    public BombInfo(int plantFrame, int range,Point pos)
    {
        this.plantFrame = plantFrame;
        this.range = range;
        this.pos = pos;
    }

    public int plantFrame;
    public int range;
    public Point pos;
}