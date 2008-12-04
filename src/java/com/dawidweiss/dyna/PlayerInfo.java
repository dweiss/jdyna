package com.dawidweiss.dyna;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

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
     * Update internal frame state with the controller's.
     * 
     * @see #frame
     */
    public void controllerState(Direction signal)
    {
        if (signal == null)
        {
            // No movement at all. Reset to the first frame in the current state.
            frame = 0;
        }
        else
        {
            final Player.State prev = state;
            switch (signal)
            {
                case LEFT:
                    state = Player.State.LEFT; break;
                case RIGHT:
                    state = Player.State.RIGHT; break;
                case UP:
                    state = Player.State.UP; break;
                case DOWN:
                    state = Player.State.DOWN; break;
                default:
                    throw new RuntimeException(/* unreachable */);
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
}
