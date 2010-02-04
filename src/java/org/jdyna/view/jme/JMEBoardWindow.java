package org.jdyna.view.jme;

import java.awt.Dimension;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.jdyna.Game;
import org.jdyna.GameEvent;
import org.jdyna.IGameEventListener;
import org.jdyna.IViewListener;
import org.jdyna.view.jme.adapter.JDynaGameAdapter;
import org.jdyna.view.swing.ScoreFrameFor3d;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.jme.input.MouseInput;
import com.jme.system.GameSettings;
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
    public ScoreFrameFor3d scoreFrame;

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
        this(null);
    }

    public JMEBoardWindow(IViewListener listener)
    {
       this.viewListener = listener;
       init();
    }

    private void init()
    {
        gameAdapter = new JDynaGameAdapter();

        // TODO: read and pass settings here.
        game = new StandardGame("JDyna 3D", /* settings */ null);
        GameSettings gameSettings = game.getSettings();

        // TODO: get the settings from configuration menu
        gameSettings.setSFX(false);
        gameSettings.setMusic(false);

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
        
        scoreFrame = new ScoreFrameFor3d();
        scoreFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        scoreFrame.setSize(new Dimension(300, 500));
        scoreFrame.setFocusable(false);
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
            scoreFrame.onFrame(frame, events);
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

    public void trackPlayer(String trackedPlayer)
    {
        gameAdapter.setTrackedPlayer(trackedPlayer);
    }
}
