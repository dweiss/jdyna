package com.dawidweiss.dyna;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.google.common.collect.Lists;

/**
 * Image I/O utilities.
 */
final class ImageUtilities
{
    private ImageUtilities()
    {
        // No instances.
    }

    /**
     * Load an image and re-render it into a {@link GraphicsConfiguration}-compatible
     * image.
     */
    public static BufferedImage loadResourceImage(String resourcePath,
        GraphicsConfiguration conf) throws IOException
    {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final InputStream is = cl.getResourceAsStream(resourcePath);
        if (is == null) throw new IOException("Resource not found: " + resourcePath);
        try
        {
            final BufferedImage image = ImageIO.read(is);

            final BufferedImage converted = conf.createCompatibleImage(image.getWidth(),
                image.getHeight(), Transparency.TRANSLUCENT);
            final Graphics graphics = converted.getGraphics();
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();

            return converted;
        }
        finally
        {
            is.close();
        }
    }

    /**
     * Cut out a rectangular section of the target image and create a
     * {@link GraphicsConfiguration}-compatible (but not volatile) copy.
     */
    public static BufferedImage tile(BufferedImage source, Rectangle r,
        GraphicsConfiguration conf) throws IOException
    {
        final BufferedImage sub = source.getSubimage(r.x, r.y, r.width, r.height);
        final BufferedImage tile = conf.createCompatibleImage(r.width, r.height,
            Transparency.BITMASK);

        final Graphics2D g = (Graphics2D) tile.getGraphics();
        g.drawImage(sub, null, 0, 0);
        g.dispose();

        return tile;
    }

    /**
     * @return Return the default {@link GraphicsConfiguration}.
     */
    public static GraphicsConfiguration getGraphicsConfiguration()
    {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
            .getDefaultConfiguration();
    }

    /**
     * Same as {@link #cell(GraphicsConfiguration, BufferedImage, int, int, int[]...)} but
     * for single-frame cells.
     */
    public static BufferedImage cell(GraphicsConfiguration conf, BufferedImage bricks, int w,
        int h, int x, int y) throws IOException
    {
        return cell(conf, bricks, w, h, new int [] []
        {
            {
                x, y
            }
        })[0];
    }

    /**
     * Cut out image tiles for a given cell.
     * 
     * @param w Width of each frame's image.
     * @param h Height of each frame's image.
     * @param offsets An array of <code>int[2]</code> with each frame's positions on the
     *            source bricks image. The coordinates are multiplied by
     *            {@link #CELL_SIZE}.
     */
    public static BufferedImage [] cell(GraphicsConfiguration conf, BufferedImage bricks,
        int w, int h, int []... offsets) throws IOException
    {
        final ArrayList<BufferedImage> frames = Lists.newArrayList();
        for (int [] coords : offsets)
        {
            assert coords.length == 2;

            final int x = coords[0];
            final int y = coords[1];
            final Rectangle r = new Rectangle(x, y, w, h);
            frames.add(ImageUtilities.tile(bricks, r, conf));
        }

        return frames.toArray(new BufferedImage [frames.size()]);
    }
}