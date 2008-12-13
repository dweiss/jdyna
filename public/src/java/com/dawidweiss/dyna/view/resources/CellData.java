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

    /*
     * This is not a deep clone, but we clone the superficial structures,
     * leaving image and image slices the same to conserve memory. 
     */
    public Object clone()
    {
        try
        {
        	final CellData _ret = (CellData) super.clone();
			_ret.slices = slices.clone();
			_ret.frames = frames.clone();
            return _ret;
        }
        catch (CloneNotSupportedException e)   
        {
            throw new RuntimeException(e);
        }
    }
}