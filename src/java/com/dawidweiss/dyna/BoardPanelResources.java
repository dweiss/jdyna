package com.dawidweiss.dyna;

import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;

import com.google.common.collect.Maps;

/**
 * Resources required for {@link BoardPanel} visualization. These resources are separate
 * because they can be reused.
 */
public final class BoardPanelResources
{
    /**
     * Target visualization device configuration.
     */
    public final GraphicsConfiguration conf;

    /**
     * Tile images pre-rendered for the graphic device currently used. The first dimension
     * is the cell's index (type), the second dimension contains frame data.
     */
    public final EnumMap<Cell, BufferedImage []> cell_images = 
        new EnumMap<Cell, BufferedImage []>(Cell.class);

    /**
     * Information about cells, their shapes and images.
     */
    public final EnumMap<Cell, CellInfo> cell_infos;

    /**
     * Each cell's width and height.
     */
    public final int gridSize;

    /*
     *
     */
    public BoardPanelResources(GraphicsConfiguration conf, EnumMap<Cell, CellInfo> mapping, int gridSize)
        throws IOException
    {
        this.conf = conf;
        this.cell_infos = mapping;
        this.gridSize = gridSize;

        /*
         * Prebuffer images for cells.
         */
        final HashMap<String, BufferedImage> cache = Maps.newHashMap();
        for (Cell c : mapping.keySet())
        {
            final TileInfo [] ti = mapping.get(c).tiles;
            final BufferedImage [] tileImages = new BufferedImage [ti.length];

            for (int i = 0; i < ti.length; i++)
            {
                final TileInfo t = ti[i];

                if (!cache.containsKey(t.imageName))
                {
                    cache.put(t.imageName, ImageUtilities.loadResourceImage("05.png", conf));
                }
                final BufferedImage image = cache.get(t.imageName);

                tileImages[i] = ImageUtilities.cell(conf, image, t.w, t.h, t.x, t.y);
            }

            cell_images.put(c, tileImages);
        }
    }
}
