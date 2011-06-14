package org.jdyna.view.resources.jme;

import org.jdyna.view.jme.adapter.AbstractGameAdapter.DynaCell;

@SuppressWarnings("serial")
public class DynaFloor extends DynaObject
{
    
    public DynaFloor(int i, int j)
    {
        super(i,j);
        attachChild(mf.createMesh(DynaCell.EMPTY));
    }

}
