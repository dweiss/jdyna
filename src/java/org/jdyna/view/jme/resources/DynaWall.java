package org.jdyna.view.jme.resources;

import org.jdyna.CellType;


@SuppressWarnings("serial")
public class DynaWall extends DynaObject
{
    
    public DynaWall(int i, int j)
    {
        super(i,j);
        attachChild(mf.createMesh(CellType.CELL_WALL));
    }
}
