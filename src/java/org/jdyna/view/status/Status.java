package org.jdyna.view.status;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Status extends JPanel
{
    /**
     * Name of this statistic.
     */
    private String name;

    /**
     * Icon of this statistic.
     */
    private BufferedImage icon;

    /**
     * Counter of this statistic.
     */
    private int counter;

    /**
     *
     */
    public Status(String name, BufferedImage icon, int counter)
    {
        this.name = name;
        this.icon = icon;
        this.counter = counter;
        setPreferredSize(new Dimension(icon.getWidth(), icon.getHeight() + 11));
    }

    public void paint(Graphics g)
    {
        g.setColor(getBackground());
        g.fillRect(0, 0, getSize().width, getSize().height);
        g.drawImage(icon, 0, 0, this);
        g.setColor(Color.BLACK);

        /*
         * Do not display counter if it is less than zero.
         */
        if (counter >= 0)
        {
            if (counter >= 10) g.drawString(Integer.toString(counter), 0, icon
                .getHeight() + 11);
            else g.drawString(Integer.toString(counter), 4, icon.getHeight() + 11);
        }

        /*
         * If this is bomb range statistic and the counter equals -1, it means that the
         * max range bonus is collected - draw infinity.
         */
        else if (name == "Bomb range") g.drawString("\u221E", 2, icon.getHeight() + 11);
    }

    public void updateCounter(int counter, int frameRate)
    {
        this.counter = (int) (counter / frameRate);
        repaint();
    }

    public int getCounter()
    {
        return counter;
    }

    public String getName()
    {
        return name;
    }
}
