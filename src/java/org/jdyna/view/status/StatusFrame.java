package org.jdyna.view.status;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;

import org.jdyna.GameEvent;
import org.jdyna.IGameEventListener;
import org.jdyna.view.resources.ImageUtilities;
import org.jdyna.view.resources.Images;
import org.jdyna.view.resources.ImagesFactory;

@SuppressWarnings("serial")
public class StatusFrame extends JFrame implements IGameEventListener
{
    private StatusPanel statusPanel;
    private final GraphicsConfiguration conf;

    public StatusFrame()
    {
        conf = ImageUtilities.getGraphicsConfiguration();
        final Images images = ImagesFactory.DYNA_CLASSIC;

        statusPanel = new StatusPanel(images, conf);
        add(statusPanel);

        try
        {
            setIconImage(ImageUtilities.loadResourceImage("icons/window-icon.png"));
        }
        catch (IOException e)
        {
            // Ignore.
        }
        setTitle("Status.");

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(new Dimension(400, 70));
        setFocusable(false);
    }

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        this.statusPanel.onFrame(frame, events);
    }
}
