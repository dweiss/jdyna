package org.jdyna.view.jme.controller;

import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Controller;
import com.jme.scene.Spatial;
import com.jme.scene.state.BlendState;
import com.jme.system.DisplaySystem;

@SuppressWarnings("serial")
public class DestroyableController extends Controller
{
    boolean destroyed = false;
    float opacity = 1.0f;
    private Spatial spatial;
    private BlendState state;

    public DestroyableController(Spatial spatial)
    {
        this.spatial = spatial;
        state = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
        state.setBlendEnabled(true);
        state.setEnabled(true);
        state.setSourceFunction(BlendState.SourceFunction.ConstantAlpha);
        state
            .setDestinationFunction(BlendState.DestinationFunction.OneMinusConstantAlpha);
        state.setConstantColor(new ColorRGBA(0, 0, 0, 1));
    }

    @Override
    public void update(float time)
    {
        if (!destroyed) return;
        opacity -= time * (2 * getSpeed()); // 1/2sec. by default
        if (opacity < 0)
        {
            spatial.removeFromParent();
        }
        else
        {
            state.setConstantColor(new ColorRGBA(0, 0, 0, opacity));
            spatial.updateRenderState();
        }
    }

    public void destroy()
    {
        spatial.setRenderState(state);
        spatial.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
        spatial.updateRenderState();

        destroyed = true;
    }
}
