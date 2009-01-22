package com.dawidweiss.dyna.view.resources;

import java.awt.image.BufferedImage;

import com.dawidweiss.dyna.CellType;

/**
 * Extra data for {@link CellType}.
 */
class CellData
{
    /** Which cell type this information applies to. */
    public CellType cellType;

    /** How many game frames to change animation frame? */
    public int frameAdvanceRate;

    /** Image infos. */
    public ImageSlice [] slices;

    /** Prefetched frame images. */
    public BufferedImage [] frames;

    /**
     * This method clones the superficial structures leaving images and image slices the
     * same to conserve memory.
     * 
     * @return shallow copy of this instance
     */
    public CellData shallowClone()
    {
        // make new instance
        CellData cloned = new CellData();

        // copy all fields values
        cloned.frameAdvanceRate = frameAdvanceRate;
        cloned.cellType = cellType;

        // clone arrays
        cloned.frames = frames.clone();
        cloned.slices = slices.clone();

        return cloned;
    }
}