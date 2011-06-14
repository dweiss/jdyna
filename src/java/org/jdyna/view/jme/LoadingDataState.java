package org.jdyna.view.jme;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import org.jdyna.view.jme.resources.MeshFactory;
import org.jdyna.view.jme.resources.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme.scene.Text;
import com.jme.util.GameTaskQueueManager;
import com.jmex.font2d.Text2D;

public class LoadingDataState extends GameState
{
    private final static Logger logger = LoggerFactory.getLogger(LoadingDataState.class);

    private Collection<Listener> listeners = new LinkedList<Listener>();

    public LoadingDataState(Listener l)
    {
        this();
        addListener(l);
    }

    void addListener(Listener l)
    {
        listeners.add(l);
    }

    boolean removeListener(Listener l)
    {
        return listeners.remove(l);
    }

    public LoadingDataState()
    {
        final Text label = Text2D.createDefaultTextLabel("StatusLabel", "Loading...");
        rootNode.attachChild(label);
        label.updateRenderState();

        final Callable<Void> successCallback = new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {

                for (Listener l : listeners)
                {
                    l.onDataLoaded();
                }
                return null;
            }
        };

        final Callable<Void> failureCallback = new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                for (Listener l : listeners)
                {
                    l.onDataLoadFailed();
                }
                return null;
            }
        };

        class StatusCallback implements Callable<Void>
        {
            private final String status;
            
            public StatusCallback(String status)
            {
                this.status = status;
            }

            @Override
            public Void call() throws Exception
            {
                label.print(status);
                return null;
            }
        };

        new Thread()
        {
            @Override
            public void run()
            {
                // preload all meshes
                try
                {
                    MeshFactory.initializeSingleton(new ProgressListener()
                    {
                        public void update(String status)
                        {
                            GameTaskQueueManager.getManager().update(
                                new StatusCallback(status));
                        }
                    });
                    GameTaskQueueManager.getManager().update(successCallback);
                }
                catch (Throwable e)
                {
                    logger.error("Failed pre-loading meshes: " + e.getMessage());
                    GameTaskQueueManager.getManager().update(failureCallback);
                }
            }
        }.start();
    }

    /*
     * LoadingDataState is extending GameState, it has to implement update method.
     * But, in fact, we don't actually need anything to update in this class.
     * @see org.jdyna.view.jme.GameState#update(float, float)
     */
    public void update(float tpf, float time)
    {
    }

    public interface Listener
    {
        void onDataLoaded();

        void onDataLoadFailed();
    }
}
