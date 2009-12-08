package org.jdyna.view.resources.jme;

import org.jdyna.view.jme.adapter.AbstractGameAdapter.DynaCell;
import org.jdyna.view.jme.controller.DestroyableController;

@SuppressWarnings("serial")
public class DynaCrate extends DynaObject
{

    /*
     * what is it for?
     * private BlendState state; 
     */
    private DestroyableController destroyable;

    public DynaCrate(int i, int j)
    {
        super(i,j);
        attachChild(mf.createMesh(DynaCell.CRATE));
        
        destroyable = new DestroyableController(this);
        addController(destroyable);
    }

    public void destroy()
    {
        destroyable.destroy();
    }
}