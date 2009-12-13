package org.jdyna.view.jme;

import java.awt.Point;
import java.util.Collection;
import java.util.LinkedList;

import org.jdyna.CellType;

import org.jdyna.view.jme.adapter.GameListener;
import org.jdyna.view.jme.adapter.JDynaGameAdapter;
import org.jdyna.view.jme.resources.DynaBomb;
import org.jdyna.view.jme.resources.DynaBonus;
import org.jdyna.view.jme.resources.DynaCrate;
import org.jdyna.view.jme.resources.DynaExplosion;
import org.jdyna.view.jme.resources.DynaPlayer;

import com.jme.input.FirstPersonHandler;
import com.jme.input.KeyInput;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.LightState;

public class MatchGameState extends GameState implements GameListener {

	private Collection<Listener> listeners = new LinkedList<Listener>();
	private FirstPersonHandler cameraHandler;
	private JDynaGameAdapter adapter;
	private BoardData boardData;

	public MatchGameState(Listener l, JDynaGameAdapter adapter) {

		this();
		addListener(l);
		this.adapter = adapter;
	}
	
	void addListener(Listener l) {
		listeners.add(l);
	}
	
	boolean removeListener(Listener l) {
		return listeners.remove(l);
	}
	
	public MatchGameState() {
		
		//setup lights and camera
		LightState ls = renderer.createLightState();
		PointLight light = new PointLight();
        light.setDiffuse( new ColorRGBA( 0.75f, 0.75f, 0.75f, 0.75f ) );
        light.setAmbient( new ColorRGBA( 0.5f, 0.5f, 0.5f, 1.0f ) );
        light.setLocation( new Vector3f( 100, 100, 100 ) );
        light.setEnabled( true );
        ls.attach(light);
		ls.setEnabled(true);
		rootNode.setRenderState(ls);
		rootNode.updateRenderState();
		
		camera.setFrustumPerspective( 45.0f, (float) renderer.getWidth()
                / (float) renderer.getHeight(), 1, 1000 );
        camera.setParallelProjection( false );
        camera.setLocation(new Vector3f(0,10,10));
        camera.lookAt(new Vector3f(),Vector3f.UNIT_Y);
        camera.update();
        
        cameraHandler = new FirstPersonHandler(camera, 50, 1);
        cameraHandler.setEnabled(false);
	}

	@Override
	public void update(float tpf,float time) {
		
		KeyInput kb = KeyInput.get();
		
		if (kb.isKeyDown(KeyInput.KEY_ESCAPE)) {
			
			for (Listener l : listeners) {
				l.onMatchInterrupted();
			}
			kb.clearKey(KeyInput.KEY_ESCAPE);
		}
		
		adapter.dispatchEvents(this, false);
		
		cameraHandler.update(tpf);
	}
	
	public interface Listener {
		
		void onMatchFinished();
		void onMatchInterrupted();
	}

	@Override
	public void gameStarted(CellType[][] cells, int w, int h) {
		
		boardData = DynaUtils.createBoard(cells);
		
		//scale the board to make it fit in the viewport
		float scale = 10f / w;
		boardData.boardNode.setLocalScale(scale);
		
		//center the camera
		float cx = scale * w / 2 - scale / 2;
		float cy = scale * h / 2 - scale / 2;
		camera.setLocation(new Vector3f(cx,10,2*cy));
		camera.lookAt(new Vector3f(cx,0,cy),Vector3f.UNIT_Y);
		
		//attach the board to the root node
		rootNode.attachChild(boardData.boardNode);
		boardData.boardNode.updateRenderState();
	}
    @Override
    public void bombExploded(int i, int j,int left,int right,int up,int down)
    {
        DynaBomb bomb = boardData.bombs.get(new Point(i,j));
        bomb.explode();
        boardData.bombs.remove(bomb);
        
        DynaExplosion dynaExplosion = new DynaExplosion(i,j,left,right,up,down);
        boardData.boardNode.attachChild(dynaExplosion);
        dynaExplosion.updateRenderState();
    }

    @Override
    public void bombPlanted(int i, int j)
    {
        DynaBomb bomb = new DynaBomb(i,j);
        boardData.bombs.put(new Point(i,j),bomb);
        boardData.boardNode.attachChild(bomb);
        bomb.updateRenderState();
    }

    @Override
    public void crateCreated(int i, int j) {
        DynaCrate crate = new DynaCrate(i,j);
        boardData.crates.put(new Point(i,j),crate);
        boardData.boardNode.attachChild(crate);
        crate.updateRenderState();
    }
    
    @Override
    public void crateDestroyed(int i, int j)
    {
        DynaCrate crate = boardData.crates.get(new Point(i,j));
        if (crate!=null)
        	crate.destroy();
    }

    @Override
    public void playerSpawned(String name, int i, int j, boolean joined)
    {
        DynaPlayer player = new DynaPlayer(i,j);
        boardData.players.put(name,player);
        boardData.boardNode.attachChild(player);
        player.updateRenderState();
    }

    @Override
    public void playerMoved(String name, float x, float y, boolean immortal)
    {
        DynaPlayer player = boardData.players.get(name);
        player.update(x,y,immortal);
    }

    @Override
    public void playerDied(String name)
    {
        DynaPlayer player = boardData.players.get(name);
        player.kill(name);
    }

    @Override
    public void bonusSpawned(int i, int j, CellType bonusType)
    {
        DynaBonus bonus = new DynaBonus(i,j,bonusType);
        boardData.bonuses.put(new Point(i,j),bonus);
        boardData.boardNode.attachChild(bonus);
        boardData.boardNode.updateRenderState();
    }
    
    @Override
    public void bonusTaken(int i, int j)
    {
        DynaBonus bonus = boardData.bonuses.get(new Point(i,j));
        bonus.take();
    }
}
