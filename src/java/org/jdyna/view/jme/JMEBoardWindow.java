package org.jdyna.view.jme;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jdyna.GameEvent;
import org.jdyna.IGameEventListener;
import org.jdyna.view.jme.MatchGameState.Listener;
import org.jdyna.view.jme.adapter.JDynaGameAdapter;

import com.jme.input.MouseInput;
import com.jme.util.GameTaskQueueManager;
import com.jmex.editors.swing.settings.GameSettingsPanel;
import com.jmex.game.StandardGame;

/* 
 * @author Artur Kłopotek
 */
public class JMEBoardWindow implements IGameEventListener, Listener, LoadingDataState.Listener
{
    private final StandardGame game = new StandardGame("JDyna3D");
    private JDynaGameAdapter gameAdapter;

    public JMEBoardWindow()
    {
        System.setProperty("jme.stats", "set");
        Logger.getLogger("com.jme").setLevel(Level.WARNING);
        MouseInput.setProvider(MouseInput.INPUT_AWT);

        gameAdapter = new JDynaGameAdapter();

        try
        {
            init();
        }
        catch (InterruptedException e)
        {
            game.shutdown();
        }
    }

    private void init() throws InterruptedException
    {
        game.getSettings().setSFX(true);
        if (GameSettingsPanel.prompt(game.getSettings()))
        {
            game.getSettings().setSFX(false);
            game.getSettings().setMusic(false);
            game.start();

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
    }

    @Override
    public void onMatchFinished()
    {
        game.shutdown();
    }

    @Override
    public void onMatchInterrupted()
    {
        game.shutdown();
    }

    @Override
    public void onDataLoaded()
    {
        new MatchGameState(this, gameAdapter).activate();
    }

    @Override
    public void onDataLoadFailed()
    {
        game.shutdown();

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                JOptionPane.showMessageDialog(null, "Failed to load resources.",
                    "JDyna3D - Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        gameAdapter.onFrame(frame, events);
    }
}
