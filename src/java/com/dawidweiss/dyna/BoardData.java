package com.dawidweiss.dyna;

import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;

import com.google.common.collect.Maps;

/**
 * Resources required visualization, collision checks, etc.
 */
public final class BoardData
{
    /**
     * Target visualization device configuration.
     */
    public final GraphicsConfiguration conf;

    /**
     * Tile images pre-rendered for the graphic device currently used. The first dimension
     * is the cell's index (type), the second dimension contains frame data.
     */
    public final EnumMap<CellType, BufferedImage []> cell_images = new EnumMap<CellType, BufferedImage []>(
        CellType.class);

    /**
     * Information about cells, their shapes and images.
     */
    public final EnumMap<CellType, CellInfo> cell_infos;

    /**
     * Each cell's width and height.
     */
    public final int gridSize;

    /**
     * Player images.
     */
    public final PlayerImageData [] player_images;

    /**
     * Temporary image cache.
     */
    private HashMap<String, BufferedImage> cache = Maps.newHashMap();

    /*
     *
     */
    public BoardData(GraphicsConfiguration conf, EnumMap<CellType, CellInfo> cellInfos,
        int gridSize, EnumMap<Player.State, TileInfo []> [] playerImageData)
        throws IOException
    {
        this.conf = conf;
        this.cell_infos = cellInfos;
        this.gridSize = gridSize;

        /*
         * Prebuffer images for cells.
         */
        for (CellType c : cellInfos.keySet())
        {
            final TileInfo [] ti = cellInfos.get(c).tiles;
            final BufferedImage [] tileImages = new BufferedImage [ti.length];
            for (int i = 0; i < ti.length; i++)
            {
                final TileInfo t = ti[i];
                tileImages[i] = ImageUtilities.cell(conf, getCached(t.imageName), 
                    t.w, t.h, t.x, t.y);
            }

            cell_images.put(c, tileImages);
        }

        /*
         * Prebuffer images for players.
         */
        player_images = new PlayerImageData [playerImageData.length];
        for (int i = 0; i < playerImageData.length; i++)
        {
            final EnumMap<Player.State, BufferedImage []> images = 
                Maps.newEnumMap(Player.State.class);

            for (Player.State d : Player.State.values()) 
            {
                final TileInfo [] ti = playerImageData[i].get(d);
                final BufferedImage [] tileImages = new BufferedImage [ti.length];
                for (int j = 0; j < ti.length; j++)
                {
                    final TileInfo t = ti[j];
                    tileImages[j] = ImageUtilities.cell(conf, 
                        getCached(t.imageName), t.w, t.h, t.x, t.y);
                }

                images.put(d, tileImages);
            }
            player_images[i] = new PlayerImageData(images);
        }

        this.cache = null;
    }

    /*
     * Cache intermediate images from which tiles are cut out.
     */
    private BufferedImage getCached(String imageName) throws IOException
    {
        if (!cache.containsKey(imageName))
        {
            cache.put(imageName, ImageUtilities.loadResourceImage(imageName, conf));
        }
        return cache.get(imageName);
    }
}
