package org.jdyna.view.resources.jme;

import com.jme.math.FastMath;
import com.jme.math.Rectangle;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Controller;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.scene.state.BlendState.BlendEquation;
import com.jme.scene.state.BlendState.DestinationFunction;
import com.jme.scene.state.BlendState.SourceFunction;
import com.jme.scene.state.BlendState.TestFunction;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.effects.particles.ParticleFactory;
import com.jmex.effects.particles.ParticleMesh;
import com.jmex.effects.particles.ParticleSystem.EmitType;

public class ExplosionFactory
{
    private static BlendState bs;
    private static TextureState ts;
    private static ZBufferState zstate;
    
    static {
        warmup();
    }

    public static ParticleMesh createExplosion(int left,int right)
    {
        int len = right+left+1;
        
        ParticleMesh explosion = ParticleFactory.buildParticles("big", len * 30);
        
        //emiter
        explosion.setEmitType(EmitType.Rectangle);
        explosion.setEmissionDirection(new Vector3f(0.0f, 1.0f, 0.0f));
        float emitSize = 0.2f;
        Vector3f a = new Vector3f(+emitSize+right,0,+emitSize);
        Vector3f b = new Vector3f(+emitSize+right,0,-emitSize);
        Vector3f c = new Vector3f(-emitSize-left,0,+emitSize);
        explosion.setGeometry(new Rectangle(a,b,c));
        
        //emit direction
        explosion.setMinimumAngle(0);
        explosion.setMaximumAngle(FastMath.HALF_PI);
        
        //lifetime
        explosion.setMinimumLifeTime(500.0f);
        explosion.setMaximumLifeTime(800.0f);

        //misc
        explosion.setStartSize(0.2f);
        explosion.setEndSize(0.4f);
        
        explosion.setStartColor(new ColorRGBA(1.0f, 0.312f, 0.121f, 1.0f));
        explosion.setEndColor(new ColorRGBA(1.0f, 0.24313726f, 0.03137255f, 0.0f));
        
        explosion.setControlFlow(false);
        explosion.setInitialVelocity(0.0005f);
        explosion.setParticleSpinSpeed(0.0f);
        explosion.setRepeatType(Controller.RT_CLAMP);

        explosion.setRenderState(ts);
        explosion.setRenderState(bs);
        explosion.setRenderState(zstate);

        return explosion;
    }

    private static void warmup()
    {
        DisplaySystem display = DisplaySystem.getDisplaySystem();
        bs = display.getRenderer().createBlendState();
        bs.setBlendEnabled(true);
        bs.setBlendEquation(BlendEquation.Add);
        bs.setSourceFunction(SourceFunction.SourceAlpha);
        bs.setDestinationFunction(DestinationFunction.One);
        bs.setTestEnabled(false);
        bs.setTestFunction(TestFunction.GreaterThan);

        ts = display.getRenderer().createTextureState();
        ts.setTexture(TextureManager.loadTexture(ExplosionFactory.class.getClassLoader()
            .getResource("jmetest/data/texture/flaresmall.jpg")));

        zstate = display.getRenderer().createZBufferState();
        zstate.setWritable(false);
        zstate.setEnabled(true);
    }
}
