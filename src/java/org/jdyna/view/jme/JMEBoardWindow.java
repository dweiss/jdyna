package org.jdyna.view.jme;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jdyna.Game;
import org.jdyna.GameEvent;
import org.jdyna.IGameEventListener;
import org.jdyna.IPlayerSprite;
import org.jdyna.IViewListener;
import org.jdyna.frontend.swing.Configuration;
import org.jdyna.view.jme.adapter.JDynaGameAdapter;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.jme.input.MouseInput;
import com.jme.util.GameTaskQueueManager;

/**
 * TODO: Javadoc.
 */
public class JMEBoardWindow implements IGameEventListener
{
    /**
     * Static 
     */
    static
    {
        /*
         * Replace all default JDK logging handlers with SLF4J bridge.
         */
        final LogManager lm = LogManager.getLogManager();
        final Logger root = lm.getLogger("");
        for (Handler h : lm.getLogger("").getHandlers())
            root.removeHandler(h);
        root.addHandler(new SLF4JBridgeHandler());
        Logger.getLogger("com.jme.scene.Node").setLevel(Level.WARNING);
        Logger.getLogger("com.jme.scene.TriMesh").setLevel(Level.WARNING);

        /*
         * TODO: What does this thing do?
         */
        System.setProperty("jme.stats", "set");

        /*
         * TODO: Do we need to do it in a static block? Why not in the game init routine?
         */
        MouseInput.setProvider(MouseInput.INPUT_AWT);
    }

    private StandardGame game;
    private JDynaGameAdapter gameAdapter;
    private IViewListener viewListener;
    private final Configuration config;
    /**
     * Data loading callback.
     */
    private final LoadingDataState.Listener loadingListener = new LoadingDataState.Listener()
    {
        public void onDataLoaded()
        {
            new MatchGameState(gameAdapter).activate();
        }

        public void onDataLoadFailed()
        {
            dispose();
        }
    };

    public JMEBoardWindow()
    {
        this(null, null);
    }

    public JMEBoardWindow(IViewListener listener)
    {
       this(listener, null);
    }
    
    public JMEBoardWindow(Configuration config)
    {
        this(null, config);
    }
    
    public JMEBoardWindow(IViewListener listener, Configuration config)
    {
        this.viewListener = listener;
        this.config = config;
        init();
    }

    private void init()
    {
        gameAdapter = new JDynaGameAdapter();

        game = new StandardGame("JDyna 3D", config.getJMESettings());

        // Start the OpenGL loop. 
        game.start();

        GameTaskQueueManager.getManager().update(new Callable<Void>()
        {
            public Void call() throws Exception
            {
                new LoadingDataState(loadingListener).activate();
                return null;
            }
        });
    }

    /**
     * Close the view and any associated resources. This method may be invoked
     * by both {@link Game} and OpenGL thread from {@link StandardGame}.
     */
    public void dispose()
    {
        // Request the game thread to finish (and wait for it).
        game.shutdown();
        try
        {
            game.gameThread.join();
        }
        catch (InterruptedException e)
        {
            // Ignore, can't do much.
        }
    }

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        if (game.gameThread.isAlive())
        {
            gameAdapter.onFrame(frame, events);
        }
        else
        {
            if (this.viewListener != null)
            {
                this.viewListener.viewClosed();
                this.viewListener = null;
            }
        }
    }

    /**
     * Select a single player to display its status
     */
    public void trackPlayer(String trackedPlayer)
    {
        gameAdapter.addTrackedPlayer(trackedPlayer);
    }

    /**
     * Select all players to display theirs status
     */
    public void trackPlayers(IPlayerSprite... trackedPlayers)
    {
        for (IPlayerSprite player : trackedPlayers)
        {
            gameAdapter.addTrackedPlayer(player.getName());  
        } 
    }
}
