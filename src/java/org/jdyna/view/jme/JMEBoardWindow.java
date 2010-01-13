package org.jdyna.view.jme;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jdyna.GameEvent;
import org.jdyna.IGameEventListener;
import org.jdyna.IViewListener;
import org.jdyna.view.jme.adapter.JDynaGameAdapter;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.jme.input.MouseInput;
import com.jme.system.GameSettings;
import com.jme.util.GameTaskQueueManager;

/* 
 * 
 */
public class JMEBoardWindow implements IGameEventListener, LoadingDataState.Listener
{
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

    private GameSettings gameSettings;
    private StandardGame game;
    private JDynaGameAdapter gameAdapter;
    private MatchGameState match;
    private IViewListener viewListener;
    
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

        game = new StandardGame("JDyna 3D");
        gameSettings = game.getSettings();

        // TODO: get the settings from configuration menu
        gameSettings.setSFX(false);
        gameSettings.setMusic(false);
        
        // Don't call this, it starts a thread we cannot control.
        // game.start();
        try
        {
            Field f = game.getClass().getDeclaredField("gameThread");
            f.setAccessible(true);
            
            gameThread = new Thread() {
                public void run()
                {
                    game.run();

                    if (!disposed)
                    {
                        if (viewListener != null)
                            viewListener.viewClosed();
                        dispose();
                    }
                }
            };
            f.set(game, gameThread);
            gameThread.start();

            while (!(game.isStarted()))
                Thread.sleep(100);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        GameTaskQueueManager.getManager().update(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                new LoadingDataState(JMEBoardWindow.this).activate();
                return null;
            }
        });
    }

    private Thread gameThread;
    private volatile boolean disposed = false;
    public void dispose()
    {
        if (disposed) return;
        disposed = true;

        if (gameThread != null)
        {
            try
            {
                game.finish();
                if (gameThread != Thread.currentThread())
                    gameThread.join();
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onDataLoaded()
    {
        match = new MatchGameState(gameAdapter);
        match.activate();
    }

    @Override
    public void onDataLoadFailed()
    {
        dispose();
    }

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        gameAdapter.onFrame(frame, events);
    }
}
