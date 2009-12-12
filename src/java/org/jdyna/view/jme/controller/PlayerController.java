package org.jdyna.view.jme.controller;

import org.jdyna.view.jme.resources.DynaPlayer;

import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Controller;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.BlendState.BlendEquation;
import com.jme.scene.state.BlendState.SourceFunction;
import com.jme.system.DisplaySystem;

@SuppressWarnings("serial")
public class PlayerController extends Controller
{

    boolean killed = false;
    float scale = 1.0f;
    private DynaPlayer spatial;

    public PlayerController(DynaPlayer spatial)
    {
        this.spatial = spatial;
    }

    private float frameNum = 0;
	private boolean walk;
    
    @Override
    public void update(float time)
    {
        if (!killed) {
        	
        	if (walk)
        		frameNum += getSpeed() * (time * 25); 
        		
        	int iFrame = (int)frameNum % spatial.meshes.length;
        	
        	spatial.detachAllChildren();
        	spatial.attachChild(spatial.meshes[iFrame]);
        	
        	spatial.meshes[iFrame].updateRenderState();
        	
        } else {
        	
	    	frameNum += getSpeed() * (time * 25) / 2;
	    	int iFrame = (int)frameNum % spatial.meshes.length;
	    	
	    	spatial.detachAllChildren();
	    	spatial.attachChild(spatial.dyingMeshes[iFrame]);
	    	
	    	spatial.dyingMeshes[iFrame].updateRenderState();
        }
    }

    public void kill()
    {
        BlendState bs = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
        bs.setEnabled(true);
        bs.setBlendEnabled(true);
        
        bs.setSourceFunctionAlpha(SourceFunction.One);
        bs.setSourceFunctionRGB(SourceFunction.ConstantColor);
        bs.setConstantColor(ColorRGBA.darkGray.clone());

        bs.setBlendEquation(BlendEquation.Add);
        
        spatial.setRenderState(bs);
        spatial.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
        spatial.updateRenderState();
        
        killed = true;
        frameNum = 0;
    }

	public void walk(boolean walk) {
		
		this.walk = walk;
		
		if (!walk)
			frameNum = 0;
	}
}
