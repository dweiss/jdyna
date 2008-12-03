package com.dawidweiss.dyna;

/**
 * A tile is a slice of an image.
 */
public class TileInfo
{
    /**
     * Resource name of the tiles image this tile is contained in.
     */
    public final String imageName;

    /*
     * Tile coordinates on {@link imageName} (in pixels).
     */
    public final int x, y, w, h;

    public TileInfo(String imageName, int x, int y, int w, int h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.imageName = imageName;
    }
}
