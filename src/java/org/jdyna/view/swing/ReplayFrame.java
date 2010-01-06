package org.jdyna.view.swing;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdyna.*;
import org.jdyna.serialization.FrameData;
import org.jdyna.view.resources.*;

import com.google.common.collect.Lists;

/**
 * Swing view for instant replays.
 */
@SuppressWarnings("serial")
public final class ReplayFrame extends JFrame
{
    private final GraphicsConfiguration graphicsConf;
    private BoardPanel gamePanel;
    private List<FrameData> frameData;
    private List<IHighlightDetector.FrameRange> highlightData;

    private boolean playing;
    private int frame;
   
    private final GameTimer timer;
    private final GameConfiguration conf;

    private int currentHighlight;
    private JSlider slider; 

    /*
     * 
     */
    private Runnable sliderSync = new Runnable()
    {
        public void run()
        {
            slider.setValue(frame);
        }
    };

    /*
     * 
     */
    private Runnable stopAction = new Runnable()
    {
        public void run()
        {
            stop();
        }
    };

    /*
     * 
     */
    private Thread replayer = new Thread() {
        public void run()
        {
            try
            {
                while (!interrupted())
                {
                    timer.waitForFrame();

                    final int current;
                    synchronized (this)
                    {
                        while (!playing) this.wait();
                        current = frame;

                        if (frame + 1 < frameData.size()) frame++;
                        else
                        {
                            playing = false;
                            SwingUtilities.invokeLater(stopAction);
                        }
                    }

                    SwingUtilities.invokeLater(sliderSync);
                    
                    final FrameData frameData = ReplayFrame.this.frameData.get(current);
                    gamePanel.onFrame(frameData.frame, filter(
                        frameData.events, GameEvent.Type.GAME_START, GameEvent.Type.GAME_OVER));
                }
            }
            catch (InterruptedException e)
            {
                // Do nothing.
            }
        }

        private List<? extends GameEvent> filter(List<? extends GameEvent> events, GameEvent.Type... types)
        {
            final List<GameEvent.Type> banned = Arrays.asList(types);
            for (GameEvent e : events)
            {
                if (banned.contains(e.type))
                {
                    final ArrayList<GameEvent> ge = Lists.newArrayList(events);
                    for (GameEvent e2 : events)
                    {
                        if (!banned.contains(e2.type)) ge.add(e2);
                    }
                    return ge;
                }
            }
            return events;
        }
    };

    private JButton slow;
    private JButton play;
    private JButton stop;

    private JButton nextHighlight;
    private JButton previousHighlight;

    /*
     * 
     */
    private ReplayFrame(GameConfiguration conf, GraphicsConfiguration graphicsConf)
    {
        this.conf = conf;
        this.graphicsConf = graphicsConf;
        this.timer = new GameTimer(conf.DEFAULT_FRAME_RATE);
    }

    /*
     * 
     */
    public ReplayFrame(GameConfiguration c, BoardInfo boardInfo, List<FrameData> frameData)
    {
        this(c, ImageUtilities.getGraphicsConfiguration());

        final Images images = ImagesFactory.DYNA_CLASSIC;

        gamePanel = new BoardPanel(images, graphicsConf);
        gamePanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e)
            {
                /*
                 * Resize the entire frame when the game panel changes size.
                 */
                pack();
            }
        });

        this.frameData = frameData;
        gamePanel.onFrame(0, Arrays.asList(new GameStartEvent(c, boardInfo)));

        final JPanel panel = createMainPanel();
        getContentPane().add(panel);

        setLocationByPlatform(true);
        setFocusTraversalKeysEnabled(false);
        getRootPane().setDoubleBuffered(false);
        setResizable(false);
        try
        {
            setIconImage(ImageUtilities.loadResourceImage("icons/window-icon.png"));
        }
        catch (IOException e)
        {
            // Ignore.
        }

        setTitle("Replaying game...");
        pack();

        replayer.start();
        addWindowListener(new WindowAdapter()
        {
            public void windowClosed(WindowEvent e)
            {
                replayer.interrupt();
            }
        });
    }

    /*
     * 
     */
    public ReplayFrame(GameConfiguration c, BoardInfo boardInfo,
        List<FrameData> frameData, List<IHighlightDetector.FrameRange> highlightData)
    {
        this(c, boardInfo, frameData);

        if (highlightData != null && highlightData.size() > 0)
        {
            currentHighlight = 0;
            this.highlightData = highlightData;

            Dictionary<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
            for (IHighlightDetector.FrameRange frameRange : highlightData)
            {
                labels.put(frameRange.beginFrame, new JLabel("^"));
            }
            slider.setLabelTable(labels);
            slider.setPaintLabels(true);

            nextHighlight.setEnabled(true);
            previousHighlight.setEnabled(true);
        }
    }

    /*
     * 
     */
    private JPanel createMainPanel()
    {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(gamePanel, BorderLayout.CENTER);
        panel.add(createButtons(), BorderLayout.SOUTH);
        return panel;
    }

    /*
     * 
     */
    private Component createButtons()
    {
        final GridBagLayout gridbag = new GridBagLayout();
        final GridBagConstraints cc = new GridBagConstraints();

        final JPanel panel = new JPanel();
        panel.setLayout(gridbag);

        slow = new JButton("slow");
        cc.fill = GridBagConstraints.VERTICAL;
        gridbag.setConstraints(slow, cc);
        panel.add(slow);

        play = new JButton("play");
        cc.fill = GridBagConstraints.VERTICAL;
        gridbag.setConstraints(play, cc);
        panel.add(play);

        stop = new JButton("stop");
        gridbag.setConstraints(stop, cc);
        panel.add(stop);
        stop.setEnabled(false);

        previousHighlight = new JButton("<--");
        gridbag.setConstraints(previousHighlight, cc);
        panel.add(previousHighlight);
        previousHighlight.setEnabled(false);

        nextHighlight = new JButton("-->");
        gridbag.setConstraints(nextHighlight, cc);
        panel.add(nextHighlight);
        nextHighlight.setEnabled(false);

        slider = new JSlider(0, frameData.size() - 1);
        cc.fill = GridBagConstraints.HORIZONTAL;
        cc.anchor = GridBagConstraints.CENTER;
        cc.weightx = 1;
        gridbag.setConstraints(slider, cc);
        panel.add(slider);
        slider.setValue(0);
        slider.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                ReplayFrame.this.frame = slider.getValue();
                gamePanel.onFrame(frameData.get(frame).frame, frameData.get(frame).events);
            }
        });
        
        slow.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                timer.setFrameRate(5);
                play();
            }
        });

        play.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                timer.setFrameRate(conf.DEFAULT_FRAME_RATE);
                play();
            }
        });

        stop.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                stopAction.run();
            }
        });

        nextHighlight.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (setNextHighlight())
                {
                    slider.setValue(highlightData.get(currentHighlight).beginFrame);
                }
            }
        });

        previousHighlight.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (setPreviousHighlight())
                {
                    slider.setValue(highlightData.get(currentHighlight).beginFrame);
                }
            }
        });

        return panel;
    }

    private boolean setNextHighlight()
    {
        for (int i = 0; i < highlightData.size(); i++)
        {
            if (highlightData.get(i).beginFrame > frame)
            {
                currentHighlight = i;
                return true;
            }
        }
        return false;
    }

    private boolean setPreviousHighlight() 
    {
        for (int i = highlightData.size() - 1; i >= 0; i--)
        {
            if (highlightData.get(i).beginFrame + conf.DEFAULT_FRAME_RATE < frame)
            {
                currentHighlight = i;
                return true;
            }
        }
        return false;
    }

    /**
     * 
     */
    private void play()
    {
        stop.setEnabled(true);
        slider.setEnabled(false);
        play.setEnabled(false);
        slow.setEnabled(false);

        synchronized (replayer)
        {
            playing = true;
            replayer.notifyAll();
        }
    }

    private void stop()
    {
        stop.setEnabled(false);
        slider.setEnabled(true);
        play.setEnabled(true);
        slow.setEnabled(true);
        
        synchronized (replayer)
        {
            playing = false;
            replayer.notifyAll();
        }
    }
}
