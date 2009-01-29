package com.dawidweiss.dyna.view.swing;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.dawidweiss.dyna.*;
import com.dawidweiss.dyna.serialization.FrameData;
import com.dawidweiss.dyna.view.resources.*;
import com.google.common.collect.Lists;

/**
 * Swing view for instant replays.
 */
@SuppressWarnings("serial")
public final class ReplayFrame extends JFrame
{
    private final GraphicsConfiguration conf;
    private BoardPanel gamePanel;
    private List<FrameData> frameData;

    private boolean playing;
    private int frame;
    private final GameTimer timer = new GameTimer(Globals.DEFAULT_FRAME_RATE);

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

                        if (frame < frameData.size()) frame++;
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

    /*
     * 
     */
    private ReplayFrame(GraphicsConfiguration conf)
    {
        this.conf = conf;
    }

    /*
     * 
     */
    public ReplayFrame(BoardInfo boardInfo, List<FrameData> frameData)
    {
        this(ImageUtilities.getGraphicsConfiguration());
        final Images images = ImagesFactory.DYNA_CLASSIC;

        gamePanel = new BoardPanel(images, conf);
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
        gamePanel.onFrame(0, Arrays.asList(new GameStartEvent(boardInfo)));

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
                timer.setFrameRate(Globals.DEFAULT_FRAME_RATE);
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

        return panel;
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
