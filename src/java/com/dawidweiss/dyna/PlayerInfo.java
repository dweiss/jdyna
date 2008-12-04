package com.dawidweiss.dyna;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.EnumSet;

import com.dawidweiss.dyna.IController.Direction;

/**
 * Extra {@link Player} information for the {@link Game}.
 */
final class PlayerInfo implements ISprite
{
    /**
     * Coordinates of this player (it's centerpoint). The rectangle actually taken by the
     * player and the position of the sprite is determined by the player's implementation.
     */
    public final Point location = new Point();

    /**
     * Movement speed in each state.
     */
    public final Point speed = new Point(4, 4);

    /**
     * Image data for the player.
     */
    public final PlayerImageData images;

    /**
     * If walking anywhere, an increasing counter of frames.
     */
    private int frame;

    /**
     * Walking state or <code>null</code>. 
     */
    private Player.State state = Player.State.DOWN;

    /*
     * 
     */
    public PlayerInfo(PlayerImageData playerImageData)
    {
        this.images = playerImageData;
    }

    /*
     * @see ISprite 
     */
    public BufferedImage getImage()
    {
        return images.get(state, frame);
    }

    /*
     * @see ISprite 
     */
    public Point getPosition()
    {
        final Rectangle envelope = images.envelope;
        final Point p = new Point(location);
        p.translate(-envelope.width / 2, -envelope.height / 2);
        return p;
    }

    /**
     * Update internal state with the controller's.
     */
    public void controllerState(EnumSet<Direction> signals)
    {
        final Player.State prev = state;

        if (signals.contains(IController.Direction.LEFT)) state = Player.State.LEFT;
        else if (signals.contains(IController.Direction.RIGHT)) state = Player.State.RIGHT;
        else if (signals.contains(IController.Direction.UP)) state = Player.State.UP;
        else if (signals.contains(IController.Direction.DOWN)) state = Player.State.DOWN;
        else
        {
            // No movement at all. Reset to the first frame in the current state.
            frame = 0;
            return;
        }

        if (state != prev)
        {
            frame = 0;
        }
        else
        {
            frame ++;
        }
    }
}
