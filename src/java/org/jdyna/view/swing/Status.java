package org.jdyna.view.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class Status extends JPanel
{
    /**
     * Name of this statistic.
     */
    public final StatusField field;

    /**
     * Icon of this statistic.
     */
    private BufferedImage icon;

    /**
     * Counter of this statistic.
     */
    private String counter;

    /**
     * Vertical offset between the icon and the bottom of the text.
     */
    private final static int VERTICAL_OFFSET = 11;

    /**
     * Indicates whether this statistic counts down or not.
     */
    private final boolean isCountingDown;

    /**
     * Indicates whether icon should be displayed or not during blinking.
     */
    private boolean showIcon;

    /**
     * Maximum value causing the icon to blink.
     */
    private final int blinkMaxValue = 3;

    /**
     * Timer responsible for toggling the isToBeDrawed variable.
     */
    private Timer timer;

    /**
     *
     */
    public Status(StatusField field, BufferedImage icon, String counter,
        boolean isCountingDown)
    {
        this.field = field;
        this.icon = icon;
        this.counter = counter;
        this.isCountingDown = isCountingDown;
        setPreferredSize(new Dimension(icon.getWidth(), icon.getHeight()
            + VERTICAL_OFFSET));
        this.showIcon = true;

        /*
         * Timer initialization, only if it is counting down statistic.
         */
        if (isCountingDown)
        {
            /*
             * Action Listener to perform timer task.
             */
            ActionListener taskPerformer = new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    showIcon = !showIcon;
                    if (shouldBlink())
                    {
                        timer.restart();
                        repaint();
                    }
                    else showIcon = true;
                }
            };

            /*
             * Delay for timer (in milliseconds).
             */
            int delay = 300;

            /*
             * Initializing the timer.
             */
            timer = new Timer(delay, taskPerformer);
            timer.setRepeats(false);
        }
    }

    public void paint(Graphics g)
    {
        g.setColor(getBackground());
        g.fillRect(0, 0, getSize().width, getSize().height);
        if (showIcon)
        {
            g.drawImage(icon, 0, 0, this);
        }
        g.setColor(Color.BLACK);
        FontMetrics fontMetrics = getFontMetrics(g.getFont());
        g.drawString(counter, (icon.getWidth() - fontMetrics.stringWidth(counter)) / 2,
            icon.getHeight() + VERTICAL_OFFSET);
    }

    public void updateValue(int value)
    {
        /*
         * Start the timer, if the icon should blink.
         */
        if (isCountingDown && shouldBlink() && !timer.isRunning())
        {
            timer.start();
        }

        /*
         * Show or hide statistic.
         */
        if (value < 0) setVisible(false);
        else setVisible(true);

        /*
         * Update counter.
         */
        if (value == Integer.MAX_VALUE) counter = "\u221E";
        else this.counter = Integer.toString(value);

        repaint();
    }

    /**
     * Indicates whether the icon should blink or not.
     */
    private boolean shouldBlink()
    {
        if (!isCountingDown) return false;
        else
        {
            try
            {
                int intCounter = Integer.parseInt(counter);
                return (intCounter > 0 && intCounter <= blinkMaxValue) ? true : false;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
        }
    }
}
