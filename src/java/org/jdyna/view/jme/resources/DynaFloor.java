package org.jdyna.view.jme.resources;

import org.jdyna.CellType;

@SuppressWarnings("serial")
public class DynaFloor extends DynaObject
{
    public DynaFloor(int i, int j)
    {
        super(i, j);
        attachChild(mf.createMesh(CellType.CELL_EMPTY));
    }

}
