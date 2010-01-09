package org.jdyna.view.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.google.common.collect.Lists;

/**
 * A single "status" on the {@link PlayerStatusPanel}.
 */
@SuppressWarnings("serial")
final class Status extends JPanel
{
    /**
     * The time (seconds) from which the icon should start blinking.
     */
    private final static int BLINK_THRESHOLD = 3;

    /**
     * Blinking interval (on/off) in milliseconds.
     */
    private final static int BLINK_INTERVAL = 250;

    /**
     * Name of this status indicator.
     */
    public final StatusType field;

    /**
     * The value for this status. <code>null</code> means unknown.
     */
    private volatile int value;

    /**
     * Indicates whether this status should start blinking below {@link #BLINK_THRESHOLD}
     * or not.
     */
    private final boolean blinks;

    /**
     * A list of {@link Status} objects which should be blinking.
     */
    private static List<Status> blinking = Lists.newArrayList();

    /**
     * A single timer toggling the blinking of all icons.
     */
    private static Timer timer;
    static
    {
        timer = new Timer(BLINK_INTERVAL, new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (blinking.isEmpty())
                {
                    timer.stop();
                }

                for (Status s : blinking)
                    s.icon.setEnabled(!s.icon.isEnabled());
            }
        });  
        timer.setRepeats(true);
    }

    /**
     * The icon and label associated with this statistic.
     */
    private JLabel icon;

    /**
     *
     */
    public Status(StatusType field, BufferedImage icon, boolean blinks)
    {
        this.field = field;
        this.blinks = blinks;

        this.icon = new JLabel();
        this.icon.setIcon(new ImageIcon(icon));
        this.icon.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.icon.setHorizontalTextPosition(SwingConstants.CENTER);
        this.icon.setIconTextGap(1);

        setLayout(new BorderLayout());
        add(this.icon, BorderLayout.CENTER);
    }

    /**
     * Update the value and dispatch update to the swing thread.
     */
    public void updateValue(int value)
    {
        if (value == this.value)
            return;

        this.value = value;
        SwingUtilities.invokeLater(updater);
    }

    /*
     * Run the update in the swing thread.
     */
    private final Runnable updater = new Runnable() {
        public void run()
        {
            final boolean visible = (value > 0); 
            if (visible != isVisible())
            {
                setVisible(visible);
            }

            final Status me = Status.this;
            if (visible)
            {
                icon.setText(Integer.toString(value));

                if (blinks)
                {
                    // This could be cached as a field, but the list will be so short
                    // it doesn't make much sense to do it.
                    final boolean onBlinkingList = blinking.contains(me);
                    if (value < BLINK_THRESHOLD)
                    {
                        if (!onBlinkingList)
                        {
                            blinking.add(me);
                            if (!timer.isRunning()) timer.start();
                        }
                    }
                    else
                    {
                        if (onBlinkingList)
                        {
                            blinking.remove(me);
                        }
                    }
                }
                else
                {
                    icon.setEnabled(true);
                }
            }
            else
            {
                if (blinks) blinking.remove(me);
            }
        };
    };
}
