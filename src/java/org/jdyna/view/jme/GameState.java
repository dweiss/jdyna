package org.jdyna.view.jme;

import com.jme.renderer.Camera;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.system.DisplaySystem;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.GameStateManager;

public abstract class GameState
{
    private GameStateManager manager = GameStateManager.getInstance();
    protected Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();
    protected Camera camera = renderer.createCamera(renderer.getWidth(), 
        renderer.getHeight());

    private BasicGameState state = new BasicGameState("Game state")
    {
        private float absoluteTime;

        @Override
        public void update(float tpf)
        {
            absoluteTime += tpf;
            GameState.this.update(tpf, absoluteTime);
            super.update(tpf);
        }
    };

    protected Node rootNode = state.getRootNode();

    public void activate()
    {
        manager.deactivateAllChildren();
        manager.detachAllChildren();
        manager.attachChild(state);
        renderer.setCamera(camera);
        state.setActive(true);
    }

    protected abstract void update(float interval, float absoluteTime);
}
