package com.dawidweiss.dyna.view.resources;

import static org.junit.Assert.*;

import java.awt.Point;
import java.awt.image.BufferedImage;

import org.junit.Test;

import com.dawidweiss.dyna.view.SpriteType;

public class SpriteDataTest
{
    private SpriteData sd;
    private final int fakeStatesQty = 5;
    private final int fakeFramesPerStateQty = 10;

    @org.junit.Before
    public void before()
    {
        sd = new SpriteData();

        sd.frameAdvanceRate = 5;
        sd.spriteType = SpriteType.PLAYER_1;

        sd.frames = new BufferedImage [fakeStatesQty] [fakeFramesPerStateQty];
        sd.offsets = new Point [fakeStatesQty] [fakeFramesPerStateQty];
        sd.slices = new ImageSlice [fakeStatesQty] [fakeFramesPerStateQty];

        for (int stateOrdinal = 0; stateOrdinal < fakeStatesQty; stateOrdinal++)
        {
            for (int frameId = 0; frameId < fakeFramesPerStateQty; frameId++)
            {
                sd.frames[stateOrdinal][frameId] = new BufferedImage(10, 10,
                    BufferedImage.TYPE_INT_RGB);
                sd.offsets[stateOrdinal][frameId] = new Point(100, 100);
                sd.slices[stateOrdinal][frameId] = new ImageSlice("foo", 20, 20, 10, 10);
            }
        }
    }

    @Test
    public void testShallowClone()
    {
        final SpriteData copy = sd.shallowClone();

        // check if fields have correct values
        assertSame(sd.frameAdvanceRate, copy.frameAdvanceRate);
        assertSame(sd.spriteType, copy.spriteType);

        // check if we have different array instances
        assertNotSame(sd.frames, copy.frames);
        assertNotSame(sd.slices, copy.slices);
        assertNotSame(sd.offsets, copy.offsets);

        for (int stateOrdinal = 0; stateOrdinal < fakeStatesQty; stateOrdinal++)
        {
            // check if we have different inner array instances
            assertNotSame(sd.frames[stateOrdinal], copy.frames[stateOrdinal]);
            assertNotSame(sd.slices[stateOrdinal], copy.slices[stateOrdinal]);
            assertNotSame(sd.offsets[stateOrdinal], copy.offsets[stateOrdinal]);

            for (int frameId = 0; frameId < fakeFramesPerStateQty; frameId++)
            {
                assertSame("These should be the same due to memory conservation",
                    sd.frames[stateOrdinal][frameId], copy.frames[stateOrdinal][frameId]);
                assertSame("These should be the same due to memory conservation",
                    sd.slices[stateOrdinal][frameId], copy.slices[stateOrdinal][frameId]);
                assertSame("These should be the same due to memory conservation",
                    sd.offsets[stateOrdinal][frameId],
                    copy.offsets[stateOrdinal][frameId]);
            }
        }
    }
}
