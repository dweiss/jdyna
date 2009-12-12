package org.jdyna.view.jme.resources;

import org.jdyna.view.jme.adapter.AbstractGameAdapter.BonusType;
import org.jdyna.view.jme.adapter.AbstractGameAdapter.DynaCell;
import org.jdyna.view.jme.controller.BonusController;
import org.jdyna.view.jme.controller.DestroyableController;

import com.jme.scene.Spatial;

@SuppressWarnings("serial")
public class DynaBonus extends DynaObject
{
    private Spatial mesh;
    private BonusController controller;
    private DestroyableController destroyable;

    public DynaBonus(int i, int j, BonusType type)
    {
        super(i, j);
        
        DynaCell dc = null; 
        
        if (type==BonusType.EXTRA_RANGE)
        	dc = DynaCell.BONUS_RANGE;
        else if (type==BonusType.EXTRA_BOMB)
        	dc = DynaCell.BONUS_BOMB;
        else if (type==BonusType.OTHER_BONUS)
        	dc = DynaCell.OTHER_CELL;
        
        mesh = mf.createMesh(dc);
        attachChild(mesh);

        controller = new BonusController(mesh);
        addController(controller);
        destroyable = new DestroyableController(this);
        addController(destroyable);    }
    
    public void take() {
        destroyable.destroy();
    }
}
