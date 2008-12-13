package com.dawidweiss.dyna.view.resources;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;

import org.junit.Test;

import com.dawidweiss.dyna.CellType;

/*
 * 
 */
public class CellDataTest
{
    @Test
    public void testClone()
    {
        final CellData cd = new CellData();
        cd.cellType = CellType.CELL_BOMB;
        cd.frameAdvanceRate = 10;
        cd.frames = new BufferedImage [10];
        cd.slices = new ImageSlice [10];
        
        final CellData other = (CellData) cd.clone();
        assertNotSame(cd.frames, other.frames);
        assertNotSame(cd.slices, other.slices);
        assertSame(cd.cellType, other.cellType);
        assertSame(cd.frameAdvanceRate, other.frameAdvanceRate);
    }
}
