package org.jdyna.view.jme.resources;

import org.jdyna.CellType;
import org.jdyna.view.jme.controller.DestroyableController;

import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Spatial;

@SuppressWarnings("serial")
public class DynaBomb extends DynaObject
{
    private Spatial mesh;
    private BombController controller;
    private DestroyableController destroyable;

    public DynaBomb(int i, int j)
    {
        super(i, j);
        mesh = mf.createMesh(CellType.CELL_BOMB);
        attachChild(mesh);

        controller = new BombController();
        addController(controller);

        destroyable = new DestroyableController(this);
        destroyable.setSpeed(2);
        addController(destroyable);
    }

    public void explode()
    {
        controller.explode();
        destroyable.destroy();
    }

    public class BombController extends Controller
    {
        private boolean exploding;
        private double phase;
        private float scale = 0.0f;

        public BombController()
        {
            mesh.setLocalScale(scale);
        }

        @Override
        public void update(float time)
        {
            if (!isActive()) return;

            if (!exploding)
            {
                scale += time * 3; // 1/5 sec. initial animation
                if (scale > 1) scale = 1;

                double speed = 0.7 / getSpeed(); // animation period in sec.
                phase += time * 2 * Math.PI / speed;
                double amount = 0.1;
                Vector3f scaleVec = new Vector3f();
                scaleVec.x = (float) ((1 - amount) + amount
                    * Math.sin(phase + Math.PI / 3));
                scaleVec.y = (float) ((1 - amount) + amount * Math.sin(phase));
                scaleVec.z = (float) ((1 - amount) + amount * Math.sin(phase));
                mesh.setLocalScale(scaleVec.mult(scale));
            }
        }

        public void explode()
        {
            exploding = true;
        }
    }
}
