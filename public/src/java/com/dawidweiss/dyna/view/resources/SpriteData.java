package com.dawidweiss.dyna.view.resources;

import java.awt.Point;
import java.awt.image.BufferedImage;

import com.dawidweiss.dyna.view.SpriteType;

/**
 * Extra data for {@link SpriteType}.
 */
class SpriteData implements Cloneable
{
    /** Which Sprite this information applies to. */
    public SpriteType spriteType;

    /** How many game frames to change animation frame? */
    public int frameAdvanceRate;

    /** Image infos. */
    public ImageSlice [][] slices;

    /** Prefetched frame images. */
    public BufferedImage [][] frames;

    /** Precalculated offsets. */
    public Point [][] offsets;

    /* */
    public Object clone()
    {
        SpriteData cloned = new SpriteData();
        cloned.spriteType = spriteType;
        cloned.frameAdvanceRate = frameAdvanceRate;
        
        cloned.slices = new ImageSlice [slices.length][];
        for (int i = 0; i < slices.length; i++)
        {
            cloned.slices[i] = slices[i].clone();
        }
        
        cloned.frames = new BufferedImage[frames.length][];
        for (int i = 0; i < frames.length; i++)
        {
            cloned.frames[i] = frames[i].clone();
        }
        
        cloned.offsets = new Point[offsets.length][];
        for (int i = 0; i < offsets.length; i++)
        {
            cloned.offsets[i] = offsets[i].clone();
        }

        return cloned;
    }
}