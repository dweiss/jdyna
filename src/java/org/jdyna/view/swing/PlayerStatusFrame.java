package org.jdyna.view.swing;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;

import org.jdyna.GameEvent;
import org.jdyna.IGameEventListener;
import org.jdyna.view.resources.ImageUtilities;
import org.jdyna.view.resources.Images;

/**
 * A {@link JFrame} tracking a single player's status. 
 */
@SuppressWarnings("serial")
public class PlayerStatusFrame extends JFrame implements IGameEventListener
{
    private PlayerStatusPanel statusPanel;

    public PlayerStatusFrame(Images images, String trackedPlayer)
    {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(new Dimension(240, 70));
        setTitle(trackedPlayer);
        setFocusable(false);

        statusPanel = new PlayerStatusPanel(images, trackedPlayer);
        add(statusPanel);

        try
        {
            setIconImage(ImageUtilities.loadResourceImage("icons/window-icon.png"));
        }
        catch (IOException e)
        {
            // Ignore.
        }
    }

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        this.statusPanel.onFrame(frame, events);
    }
}
