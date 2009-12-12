package org.jdyna.view.jme.resources;

import com.jme.scene.Node;

@SuppressWarnings("serial")
public abstract class DynaObject extends Node
{
    protected final MeshFactory mf;
    protected int i,j;

    public DynaObject(int i, int j) {
        setLocalTranslation(i, 0, j);
        this.i = i;
        this.j = j;
        mf = MeshFactory.inst();
    }
}
