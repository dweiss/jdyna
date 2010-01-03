package org.jdyna.view.jme.resources;

import org.jdyna.Game;

import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.math.FastMath;
import com.jme.math.Rectangle;
import com.jme.math.Ring;
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
    private static TextureState tsSmoke;
    private static ZBufferState zstate;

    static
    {
        warmup();
    }

    /*
     * Returns the particle mesh of the explosion, called by {@link DynaExplosion}
     * - when a bomb explodes -
     * lifetime is around 600ms, which corresponds the lifetime of explosion in
     * jdyna 2d.
     * Optimization note: the best bet of improving the game speed with many explosions
     * is to decrease the particles number x in the ParticleFactory.buildParticles("boom",len*x)
     * however lower numbers produce poor explosions effects. 
     */
    public static ParticleMesh createExplosion(int left, int right)
    {
        int len = right + left + 1;

        ParticleMesh explosion = ParticleFactory.buildParticles("boom", len*15);

        // emiter explosion
        explosion.setEmitType(EmitType.Rectangle);
        explosion.setEmissionDirection(new Vector3f(0.0f, 1.0f, 0.0f));
        float emitSize = 0.01f;
        Vector3f a = new Vector3f(+emitSize + right, 0, +emitSize);
        Vector3f b = new Vector3f(+emitSize + right, 0, -emitSize);
        Vector3f c = new Vector3f(-emitSize - left, 0, +emitSize);
        explosion.setGeometry(new Rectangle(a, b, c));
        
        // emit direction explosion
        explosion.setMinimumAngle(0);
        explosion.setMaximumAngle(FastMath.HALF_PI);

        // lifetime explosion
        explosion.setMinimumLifeTime(500.0f);
        explosion.setMaximumLifeTime(600.0f);

        // misc
        explosion.setStartSize(0.4f);
        explosion.setEndSize(0.3f);

        explosion.setStartColor(new ColorRGBA(1.0f, 0.312f, 0.121f, 1.0f));
        explosion.setEndColor(new ColorRGBA(1.0f, 0.24313726f, 0.03137255f, 0.0f));        

        explosion.setControlFlow(false);
        explosion.setInitialVelocity(0.0004f);
        explosion.setParticleSpinSpeed(0.0f);
        explosion.setRepeatType(Controller.RT_CLAMP);        

        // render
        explosion.setRenderState(ts);
        explosion.setRenderState(bs);
        explosion.setRenderState(zstate);

        return explosion;
    }
    
    /*
     * The function returns a ParticleMesh of smoke that leaves behind an explosion 
     * for around 2 seconds. Called by {@link DynaExplosion} along with createExplosion,
     * structure of this particleMesh is the same as Dyna Explosion with exception of the
     * emitter shape, which is a ring for producing a nice smoke around the explosion area.
     */
    public static ParticleMesh createSmoke()
    {
    	ParticleMesh explosionSmoke = ParticleFactory.buildParticles("smoke", 40);
    	
    	// emiter smoke
        explosionSmoke.setEmitType(EmitType.Ring);
        explosionSmoke.setEmissionDirection(new Vector3f(0.0f,1.0f,0.0f));
        Vector3f c = new Vector3f(0f, 0f, 0f);
        Vector3f o = new Vector3f(0f, 1f, 0f);
        explosionSmoke.setGeometry(new Ring(c,o,0.5f,0.5f));
        
        // emit direction smoke
        explosionSmoke.setMinimumAngle(0);
        explosionSmoke.setMaximumAngle(FastMath.HALF_PI);
        
        // lifetime smoke
        explosionSmoke.setMinimumLifeTime(1500.0f);
        explosionSmoke.setMaximumLifeTime(2500.0f);
        
        //settings
        explosionSmoke.setStartSize(0.25f);
        explosionSmoke.setEndSize(0.35f);
        
        // misc
        explosionSmoke.setStartColor(new ColorRGBA(0.3f, 0.3f, 0.3f, 0.8f));
        explosionSmoke.setEndColor(new ColorRGBA(0.2f, 0.2f, 0.2f, 0.0f));
        
        explosionSmoke.setControlFlow(false);
        explosionSmoke.setInitialVelocity(0.0002f);
        explosionSmoke.setParticleSpinSpeed(0.0f);
        explosionSmoke.setRepeatType(Controller.RT_CLAMP);
        
        // render
        explosionSmoke.setRenderState(tsSmoke);
        explosionSmoke.setRenderState(bs);
        explosionSmoke.setRenderState(zstate);
        
        return explosionSmoke;
    }
    
    /*
     * This function is used to set the render properties of Blend State, 
     * Texture State and Zbuffer State for explosion and smoke.
     * The presence of texture on the particles does not affect the performance
     */
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
        ts.setTexture(TextureManager.loadTexture("src/graphics/jme/explosion.jpg",MinificationFilter.Trilinear,MagnificationFilter.Bilinear));
        
        tsSmoke = display.getRenderer().createTextureState();
        tsSmoke.setTexture(TextureManager.loadTexture("src/graphics/jme/smoke.jpg",MinificationFilter.Trilinear,MagnificationFilter.Bilinear));
        
        zstate = display.getRenderer().createZBufferState();
        zstate.setWritable(false);
        zstate.setEnabled(true);
    }
}
