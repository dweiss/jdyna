package org.jdyna.view.resources;

import java.awt.Point;
import java.awt.image.BufferedImage;

import org.jdyna.ISprite;


/**
 * Extra data for {@link ISprite.Type}.
 */
class SpriteData
{
    /** Which Sprite this information applies to. */
    public ISprite.Type spriteType;

    /** How many game frames to change animation frame? */
    public int frameAdvanceRate;

    /** Image infos. */
    public ImageSlice [][] slices;

    /** Prefetched frame images. */
    public BufferedImage [][] frames;

    /** Precalculated offsets. */
    public Point [][] offsets;

    /**
     * This method clones the superficial structures leaving images, offsets and image
     * slices the same to conserve memory.
     * 
     * @return Shallow copy of this instance.
     */
    public SpriteData shallowClone()
    {
        // make new instance
        SpriteData cloned = new SpriteData();

        // copy all fields values
        cloned.frameAdvanceRate = frameAdvanceRate;
        cloned.spriteType = spriteType;

        // clone arrays
        cloned.frames = frames.clone();
        cloned.offsets = offsets.clone();
        cloned.slices = slices.clone();

        //
        // clone all inner arrays
        //
        final Object [][][] arrays = new Object [] [] [] {
            cloned.slices, cloned.offsets, cloned.frames
        };
        for (Object [][] array : arrays)
        {
            for (int i = 0; i < array.length; i++)
            {
                array[i] = array[i].clone();
            }
        }
        return cloned;
    }
}