package org.jdyna.view.resources.jme;

import org.jdyna.view.jme.adapter.AbstractGameAdapter.DynaCell;


@SuppressWarnings("serial")
public class DynaWall extends DynaObject
{
    
    public DynaWall(int i, int j)
    {
        super(i,j);
        attachChild(mf.createMesh(DynaCell.WALL));
    }
}
