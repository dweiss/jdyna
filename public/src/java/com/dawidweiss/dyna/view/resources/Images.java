package com.dawidweiss.dyna.view.resources;

import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.view.SpriteType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Images and image-related resources.
 */
public final class Images
{
    /** Cel width and height. */
    private final int cellSize;

    /** Cell data. */
    private final EnumMap<CellType, CellData> cells;

    /** Sprite data */
    private final EnumMap<SpriteType, SpriteData> sprites;

    /*
     * 
     */
    Images(int cellSize, Collection<CellData> cellData, Collection<SpriteData> spriteData)
    {
        this.cellSize = cellSize;

        cells = Maps.newEnumMap(CellType.class);
        for (CellData cd : cellData) cells.put(cd.cellType, cd);

        sprites = Maps.newEnumMap(SpriteType.class);
        for (SpriteData sd : spriteData) sprites.put(sd.spriteType, sd);
        
    }

    /*
     * 
     */
    public BufferedImage [] getCellImage(CellType cell)
    {
        final CellData cellData = cells.get(cell); 
        return cellData == null ? null : cellData.frames;
    }

    /*
     * 
     */
    public int getCellAdvanceCounter(CellType cell)
    {
        final CellData cellData = cells.get(cell); 
        return cellData == null ? 0 : cellData.frameAdvanceRate;
    }
    
    /*
     * 
     */
    public int getCellSize()
    {
        return cellSize;
    }

    /*
     * 
     */
    public BufferedImage getSpriteImage(SpriteType type, int state, int frame)
    {
        final SpriteData data = sprites.get(type);
        if (data == null)
            return null;

        if (state >= data.frames.length || data.frames[state].length == 0)
            return null;

        final int frames = data.frames[state].length;
        return data.frames[state][frame % frames];
    }

    /*
     * 
     */
    public Point getSpriteOffset(SpriteType type, int state, int frameCounter)
    {
        final SpriteData data = sprites.get(type);
        if (data == null)
            return null;

        if (state >= data.frames.length || data.frames[state].length == 0)
            return null;

        final int frame = calculateFrame(frameCounter, data);
        final int frames = data.frames[state].length;
        return data.offsets[state][frame % frames];
    }

    /**
     * Return the frame number that is available for a given sprite in the given state. 
     */
    public int getMaxSpriteImageFrame(SpriteType type, int state)
    {
        final SpriteData data = sprites.get(type);
        if (data == null)
            return 0;

        if (state >= data.frames.length || data.frames[state].length == 0)
            return 0;

        return data.frames[state].length * data.frameAdvanceRate;
    }

    /*
     * 
     */
    private int calculateFrame(int frameCounter, SpriteData data)
    {
        final int frame;
        if (frameCounter == 0)
        {
            frame = 0;
        }
        else
        {
            frame = (frameCounter - 1) / data.frameAdvanceRate;
        }
        return frame;
    }

    /**
     * Return a new set of buffered images, compatible with the given image device.
     */
    public Images createCompatible(GraphicsConfiguration conf)
    {
        final List<CellData> cellData = Lists.newArrayList();
        for (CellData cd : this.cells.values())
        {
            final CellData c = cd.shallowClone();
            for (int i = 0; i < c.frames.length; i++)
            {
                c.frames[i] = ImageUtilities.convert(c.frames[i], conf);
            }
            cellData.add(c);
        }

        final List<SpriteData> spriteData = Lists.newArrayList();
        for (SpriteData sd : this.sprites.values())
        {
            final SpriteData c = sd.shallowClone();
            for (int i = 0; i < c.frames.length; i++)
            {
                for (int f = 0; f < c.frames[i].length; f++)
                {
                    c.frames[i][f] = ImageUtilities.convert(c.frames[i][f], conf);
                }
            }
            spriteData.add(c);
        }
        
        return new Images(cellSize, cellData, spriteData);
    }

    /**
     * Returns the number of frames it takes to advance one animation frame
     * for a given sprite type. 
     */
    public int getSpriteAdvanceRate(SpriteType type)
    {
        return sprites.get(type).frameAdvanceRate;
    }
}
