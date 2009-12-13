package org.jdyna.view.jme.resources;

import org.jdyna.CellType;
import org.jdyna.view.jme.controller.BonusController;
import org.jdyna.view.jme.controller.DestroyableController;

import com.jme.scene.Spatial;

@SuppressWarnings("serial")
public class DynaBonus extends DynaObject
{
    private Spatial mesh;
    private BonusController controller;
    private DestroyableController destroyable;

    public DynaBonus(int i, int j, CellType type)
    {
        super(i, j);

        if (type != null) mesh = mf.createMesh(type);
        else mesh = mf.getUnknownBonus();

        attachChild(mesh);

        controller = new BonusController(mesh);
        addController(controller);
        destroyable = new DestroyableController(this);
        addController(destroyable);
    }

    public void take()
    {
        destroyable.destroy();
    }
}
