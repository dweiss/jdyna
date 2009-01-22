package com.dawidweiss.dyna.view.resources;

/**
 * Specification of a slice of a source image.
 */
final class ImageSlice
{
    /**
     * Resource name of the tiles image this tile is contained in.
     */
    public final String imageName;

    /*
     * Tile coordinates on {@link imageName} (in pixels).
     */
    public final int x, y, w, h;

    /*
     * 
     */
    public ImageSlice(String imageName, int x, int y, int w, int h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.imageName = imageName;
    }
}
