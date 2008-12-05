package com.dawidweiss.dyna;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.EnumMap;

/**
 * Image data for a {@link Player}.
 */
final class PlayerImageData
{
    private final EnumMap<Player.State, BufferedImage []> images;

    /**
     * Largest rectangle occupied by this player's image.
     */
    public final Rectangle envelope;

    /*
     * 
     */
    public PlayerImageData(EnumMap<Player.State, BufferedImage []> images)
    {
        this.images = images;
       
        final Rectangle r = new Rectangle();
        for (BufferedImage [] i : images.values())
            for (BufferedImage j : i)
                Rectangle.union(r, new Rectangle(0,0,j.getWidth(), j.getHeight()), r);
        this.envelope = r;
    }

    /**
     * Return an image of the player if turned in a given direction. The frame counter is
     * used to indicate the animation frame if moving.
     */
    public BufferedImage get(Player.State direction, int frame)
    {
        final BufferedImage [] directionImages = images.get(direction);
        return directionImages[frame % directionImages.length];
    }

    /**
     * Return the number of frames assigned for a given state.
     */
    public int getFrameCount(Player.State direction)
    {
        final BufferedImage [] directionImages = images.get(direction);
        return directionImages.length;
    }
}
