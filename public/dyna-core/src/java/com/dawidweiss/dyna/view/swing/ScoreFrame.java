package com.dawidweiss.dyna.view.swing;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.view.resources.*;

/**
 * Swing scoreboard view.
 */
@SuppressWarnings("serial")
public final class ScoreFrame extends JFrame implements IGameEventListener
{
    private ScorePanel scorePanel;

    /*
     * 
     */
    public ScoreFrame()
    {
        scorePanel = new ScorePanel();

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        getContentPane().add(panel);
        panel.add(scorePanel, BorderLayout.CENTER);

        setLocationByPlatform(true);
        setFocusTraversalKeysEnabled(false);
        setResizable(true);

        try
        {
            setIconImage(ImageUtilities.loadResourceImage("icons/window-icon.png"));
        }
        catch (IOException e)
        {
            // Ignore.
        }
        setTitle("Scoreboard.");
    }

    /**
     * Update the scoreboard.
     */
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        scorePanel.onFrame(frame, events);
    }
}
