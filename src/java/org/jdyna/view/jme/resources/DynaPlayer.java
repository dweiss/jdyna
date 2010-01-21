package org.jdyna.view.jme.resources;

import java.awt.Font;

import org.jdyna.view.jme.controller.DestroyableController;
import org.jdyna.view.jme.controller.PlayerController;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Controller;
import com.jme.scene.Spatial;
import com.jme.scene.state.BlendState;
import com.jme.system.DisplaySystem;
import com.jmex.font3d.Font3D;
import com.jmex.font3d.Text3D;

@SuppressWarnings("serial")
public class DynaPlayer extends DynaObject
{
    public Spatial meshes[];
    public Spatial dyingMeshes[];
    private final PlayerController controller;
    private final DestroyableController destroyableController;
    private final ImmortalityController immortalityController;
    private final String playerName;
    public Text3D text3d;

    public DynaPlayer(int i, int j, String playerName)
    {
        super(i, j);
        this.playerName = playerName;
        meshes = mf.createPlayer(MeshFactory.PLAYER_MESHES);
        dyingMeshes = mf.createPlayer(MeshFactory.PLAYER_DYING_MESHES);
        attachChild(meshes[0]);

        controller = new PlayerController(this);
        addController(controller);
        destroyableController = new DestroyableController(this);
        addController(destroyableController);
        immortalityController = new ImmortalityController();
        immortalityController.setActive(false);
        addController(immortalityController);

        controller.setSpeed(2);
        
        Font3D font3d = null;
        try {
            Font font = new Font("Arial", Font.BOLD, 16);
            font3d = new Font3D(font, 0.01f, true, true, true);
        } catch (Exception e) {
            // ignore
        }
        
        text3d = font3d.createText(this.playerName, 0.5f, 0);
        text3d.setFontColor(ColorRGBA.red.clone());
        text3d.setLocalTranslation(0, 1, 0);
        text3d.setLocalScale(new Vector3f(0.4f, 0.4f, 0.01f));
        text3d.setLocalRotation(new Quaternion(new float[]{-FastMath.PI/3, 0, 0}));
        
        attachChild(text3d);
    }

    public void update(float x, float y, boolean immortal)
    {
        Vector3f oldVec = getLocalTranslation();
        float dx = oldVec.x - x;
        float dy = oldVec.z - y;

        immortalityController.setActive(immortal);

        if (dx != 0 || dy != 0)
        {
            float angle = FastMath.atan2(dy, dx) + FastMath.HALF_PI;
            setLocalRotation(new Quaternion(new float []
            {
                0, -angle, 0
            }));
            text3d.setLocalRotation(new Quaternion(new float []
            {
                -FastMath.PI/3, angle, 0
            }));
            controller.walk(true);
        }
        else
        {
            controller.walk(false);
        }

        setLocalTranslation(x, 0, y);
    }

    public void kill(String name)
    {
        controller.kill();
        destroyableController.destroy();
    }

    private class ImmortalityController extends Controller
    {
        private ColorRGBA color = new ColorRGBA(0, 0, 0, 1);
        private BlendState state = DisplaySystem.getDisplaySystem().getRenderer()
            .createBlendState();
        private float phase;
        {
            state.setBlendEnabled(true);
            state.setEnabled(true);
            state.setSourceFunction(BlendState.SourceFunction.ConstantAlpha);
            state
                .setDestinationFunction(BlendState.DestinationFunction.OneMinusConstantAlpha);
            state.setConstantColor(new ColorRGBA(0, 0, 0, 1));
            state.setEnabled(false);
            setRenderState(state);
        }

        @Override
        public void update(float time)
        {
            phase += 2 * time * (2 * FastMath.PI); // 2 cycles per second
            color.a = 0.4f + 0.3f * (1 + FastMath.sin(phase));
            state.setConstantColor(color);
            updateRenderState();
        }

        @Override
        public void setActive(boolean active)
        {
            state.setEnabled(active);

            setRenderQueueMode(active ? Renderer.QUEUE_TRANSPARENT
                : Renderer.QUEUE_INHERIT);

            super.setActive(active);
        }
    }
}
