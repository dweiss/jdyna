package org.jdyna.view.status;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

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
     *
     */
    public Status(StatusField field, BufferedImage icon, String counter)
    {
        this.field = field;
        this.icon = icon;
        this.counter = counter;
        setPreferredSize(new Dimension(icon.getWidth(), icon.getHeight()
            + VERTICAL_OFFSET));
    }

    public void paint(Graphics g)
    {
        g.setColor(getBackground());
        g.fillRect(0, 0, getSize().width, getSize().height);
        g.drawImage(icon, 0, 0, this);
        g.setColor(Color.BLACK);
        FontMetrics fontMetrics = getFontMetrics(g.getFont());
        g.drawString(counter, (icon.getWidth() - fontMetrics.stringWidth(counter)) / 2,
            icon.getHeight() + VERTICAL_OFFSET);
    }

    public void updateValue(String value)
    {
        this.counter = value;
        repaint();
    }
}
