package com.dawidweiss.dyna.view.resources;

import java.awt.image.BufferedImage;

import com.dawidweiss.dyna.CellType;

/**
 * Extra data for {@link CellType}.
 */
class CellData implements Cloneable
{
    /** Which cell type this information applies to. */
    public CellType cellType;

    /** How many game frames to change animation frame? */
    public int frameAdvanceRate;

    /** Image infos. */
    public ImageSlice [] slices;

    /** Prefetched frame images. */
    public BufferedImage [] frames;

    /* */
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)   
        {
            throw new RuntimeException(e);
        }
    }
}