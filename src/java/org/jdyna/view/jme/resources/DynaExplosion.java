package org.jdyna.view.jme.resources;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.TransformMatrix;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jmex.effects.particles.ParticleMesh;

@SuppressWarnings("serial")
public class DynaExplosion extends DynaObject
{
    private ParticleMesh hExplosion;
    private ParticleMesh vExplosion;
    private ExplosionController controller;
    
    public DynaExplosion(int i, int j, int left, int right, int up, int down)
    {
        super(i,j);
        
        //horizontal
        hExplosion = ExplosionFactory.createExplosion(left,right);
        
        TransformMatrix t1 = new TransformMatrix();
        t1.setTranslation(i, 0, j);
        hExplosion.setEmitterTransform(t1);
        hExplosion.setLocalTranslation(i,0,j);
        
        Node node = new Node();
        node.setLocalTranslation(0, 0, 0);
        node.attachChild(hExplosion);
        attachChild(node);
        hExplosion.forceRespawn();
        
        //vertical
        vExplosion = ExplosionFactory.createExplosion(up,down);
        
        TransformMatrix t2 = new TransformMatrix();
        t2.setTranslation(i, 0, j);
        t2.setRotationQuaternion(new Quaternion(new float[]{0,-FastMath.HALF_PI,0}));
        vExplosion.setEmitterTransform(t2);
        
        attachChild(vExplosion);
        vExplosion.forceRespawn();
        
        controller = new ExplosionController();
        addController(controller);
    }
    
    public class ExplosionController extends Controller
    {
        public ExplosionController()
        {
        }

        @Override
        public void update(float time)
        {
            if (!hExplosion.isActive())
            {
                removeFromParent();
                System.out.println("explosion removed");
            }
        }
    }
}
