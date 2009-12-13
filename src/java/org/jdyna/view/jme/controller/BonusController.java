/**
 * 
 */
package org.jdyna.view.jme.controller;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.scene.Controller;
import com.jme.scene.Spatial;

@SuppressWarnings("serial")
public class BonusController extends Controller
{
    private Spatial mesh;
    private float phase;

    public BonusController(Spatial mesh)
    {
        this.mesh = mesh;
    }

    @Override
    public void update(float time)
    {
        if (!isActive()) return;
        float speed = 0.7f / getSpeed(); // animation period in sec.
        phase += time * 2 * Math.PI / speed;
        float rot = FastMath.QUARTER_PI * FastMath.sin(phase / 3);
        float alt = .2f + .2f * FastMath.sin(phase);
        mesh.setLocalTranslation(0, alt, 0);
        mesh.setLocalRotation(new Quaternion(new float []
        {
            -FastMath.QUARTER_PI, rot, 0
        }));
    }
}