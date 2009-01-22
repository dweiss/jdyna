package com.dawidweiss.dyna.view.resources;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.awt.image.BufferedImage;

import org.junit.Test;

import com.dawidweiss.dyna.CellType;

/*
 * 
 */
public class CellDataTest
{
    private CellData cd;
    private final int fakeFramesPerStateQty = 10;

    @org.junit.Before
    public void before()
    {
        cd = new CellData();

        cd.frameAdvanceRate = 5;
        cd.cellType = CellType.CELL_BOMB;

        cd.frames = new BufferedImage [fakeFramesPerStateQty];
        cd.slices = new ImageSlice [fakeFramesPerStateQty];

        for (int frameId = 0; frameId < fakeFramesPerStateQty; frameId++)
        {
            cd.frames[frameId] = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            cd.slices[frameId] = new ImageSlice("foo", 20, 20, 10, 10);
        }
    }

    @Test
    public void testShallowClone()
    {
        final CellData copy = cd.shallowClone();

        // Check if fields have correct values.
        assertSame(cd.cellType, copy.cellType);
        assertSame(cd.frameAdvanceRate, copy.frameAdvanceRate);

        // Check if we have different array instances.
        assertNotSame(cd.frames, copy.frames);
        assertNotSame(cd.slices, copy.slices);

        for (int frameId = 0; frameId < fakeFramesPerStateQty; frameId++)
        {
            assertSame("These should be the same due to memory conservation",
                cd.frames[frameId], copy.frames[frameId]);

            assertSame("These should be the same due to memory conservation",
                cd.slices[frameId], copy.slices[frameId]);
        }
    }
}
