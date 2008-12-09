package com.dawidweiss.dyna.view.resources;

import java.util.ArrayList;

import com.google.common.collect.Lists;

/**
 * Helper class for building sequences of {@link ImageSlice}s.
 */
final class ImageSliceBuilder
{
    private final String imageName;
    private final int gridSize;
    public int w;
    public int h;

    public ImageSliceBuilder(String imageName, int gridSize)
    {
        this.imageName = imageName;
        this.gridSize = gridSize;
        this.w = gridSize;
        this.h = gridSize;
    }

    public ImageSlice [] tile(int x, int y)
    {
        return tile(new int [] []
        {
            {
                x, y
            }
        });
    }

    public ImageSlice [] tile(int []... frames)
    {
        final ArrayList<ImageSlice> tiles = Lists.newArrayList();
        for (int [] frame : frames)
        {
            assert frame.length == 2;
            tiles.add(new ImageSlice(imageName, frame[0] * gridSize, frame[1] * gridSize,
                w, h));
        }

        return tiles.toArray(new ImageSlice [tiles.size()]);
    }
}
