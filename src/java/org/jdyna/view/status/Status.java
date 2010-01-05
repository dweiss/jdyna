package org.jdyna.view.status;

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
public class Status extends JPanel implements ActionListener
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
    private final int VERTICAL_OFFSET = 11;

    /**
     * Indicates whether this statistic counts down or not.
     */
    private final boolean isCountingDown;

    /**
     * Indicates whether status should be displayed or not during blinking.
     */
    private boolean isToBeDrawed;

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
        this.isToBeDrawed = true;

        /*
         * Initializing the timer to period 0.3s.
         */
        timer = new Timer(300, this);
    }

    public void paint(Graphics g)
    {
        g.setColor(getBackground());
        g.fillRect(0, 0, getSize().width, getSize().height);
        if (isToBeDrawed)
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
        if (isCountingDown)
        {
            if (value < 0)
            {
                timer.stop();
                isToBeDrawed = true;
            }
            else if (value < 4)
            {
                timer.start();
            }
        }

        if (value < 0) setVisible(false);
        else setVisible(true);

        if (value == Integer.MAX_VALUE) counter = "\u221E";
        else this.counter = Integer.toString(value);

        repaint();

    }

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        isToBeDrawed = !isToBeDrawed;
    }
}
