package org.jdyna.view.jme;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jdyna.GameEvent;
import org.jdyna.IGameEventListener;
import org.jdyna.IViewListener;
import org.jdyna.view.jme.MatchGameState.Listener;
import org.jdyna.view.jme.adapter.JDynaGameAdapter;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.jme.input.MouseInput;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;

/* 
 * 
 */
public class JMEBoardWindow implements IGameEventListener, Listener, LoadingDataState.Listener
{
    private StandardGame game = new StandardGame("JDyna3D");
    private JDynaGameAdapter gameAdapter;
    private IViewListener viewListener;

    static {
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
    
    public JMEBoardWindow()
    {
        gameAdapter = new JDynaGameAdapter();

        try
        {
            init();
        }
        catch (InterruptedException e)
        {
            gameShutdown("Game initialization failed.");
        }
    }

    public JMEBoardWindow(IViewListener listener)
    {
       this();
       viewListener = listener;
    }

    private void init() throws InterruptedException
    {
        if (game.isStarted()) {
            game.reinit();
            game.recreateGraphicalContext();
        }
        game.getSettings().setSFX(true);
        
        // TODO: get the settings from configuration menu
        // GameSettingsPanel.prompt(game.getSettings());
        game.getSettings().setSFX(false);
        game.getSettings().setMusic(false);
        game.start();

        GameTaskQueueManager.getManager().update(new Callable<Void>()
        {
            public Void call() throws Exception
            {
                new LoadingDataState(JMEBoardWindow.this).activate();
                return null;
            }
        });
    }

    @Override
    public void onMatchFinished()
    {
        gameShutdown(null);
    }

    @Override
    public void onMatchInterrupted()
    {
        gameShutdown(null);
    }

    @Override
    public void onDataLoaded()
    {
        new MatchGameState(this, gameAdapter).activate();
    }

    @Override
    public void onDataLoadFailed()
    {
        gameShutdown("Failed to load resources.");
    }


    /**
     * Shutdown the game and display error message if exists.
     * @param message - text that will be displayed.
     */
    private void gameShutdown(String message)
    {
        final String errorMessage = message;

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (errorMessage != null) JOptionPane.showMessageDialog(null,
                    errorMessage, "JDyna3D - Error", JOptionPane.ERROR_MESSAGE);
                viewListener.viewClosed();
            }
        });

        
        game.finish();
        game.shutdown();
        MouseInput.destroyIfInitalized();
        DisplaySystem.getDisplaySystem().close();
        DisplaySystem.resetSystemProvider();
    }

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        gameAdapter.onFrame(frame, events);
    }
}
