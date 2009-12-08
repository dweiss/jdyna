package org.jdyna.view.jme;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import org.jdyna.view.resources.jme.MeshFactory;

import com.jme.scene.Text;
import com.jme.util.GameTaskQueueManager;
import com.jmex.font2d.Text2D;

public class LoadingDataState extends GameState {
	
	private Collection<Listener> listeners = new LinkedList<Listener>();

	public LoadingDataState(Listener l) {

		this();
		addListener(l);
	}
	
	void addListener(Listener l) {
		listeners.add(l);
	}
	
	boolean removeListener(Listener l) {
		return listeners.remove(l);
	}
	
	public LoadingDataState() {
		
		Text label = Text2D.createDefaultTextLabel("StatusLabel", "Loading...");
		rootNode.attachChild(label);
		label.updateRenderState();
		
		final Callable<Void> successCallback = new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				
				for (Listener l : listeners) {
					l.onDataLoaded();			
				}
				return null;
			}};		
		final Callable<Void> failureCallback = new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				
				for (Listener l : listeners) {
					l.onDataLoadFailed();			
				}
				return null;
			}};		
		
		new Thread(new Runnable(){
			@Override
			public void run() {
				
				//preload all meshes
				try {
					MeshFactory.inst();
					GameTaskQueueManager.getManager().update(successCallback);
				} catch (RuntimeException e) {
					e.printStackTrace();
					GameTaskQueueManager.getManager().update(failureCallback);
				}
				
			}}).start();
    }

	public void update(float tpf,float time) {

	}

	public interface Listener {
		
		void onDataLoaded();

		void onDataLoadFailed();
	}
}
