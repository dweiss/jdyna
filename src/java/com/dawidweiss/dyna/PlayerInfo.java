package com.dawidweiss.dyna;

import java.awt.*;
import java.awt.image.BufferedImage;

import com.dawidweiss.dyna.IController.Direction;

/**
 * Extra {@link Player} information for the {@link Game}.
 */
final class PlayerInfo implements ISprite
{
    /* */
    public final Player player;

    /**
     * Coordinates of this player (it's centerpoint). The rectangle actually taken by the
     * player and the position of the sprite is determined by the player's implementation.
     */
    public final Point location = new Point();

    /**
     * Movement speed in each state.
     */
    public final Point speed = new Point(2, 2);

    /**
     * Image data for the player.
     */
    public final PlayerImageData images;

    /**
     * If walking anywhere, an increasing counter of frames.
     */
    private int frame;

    /**
     * Slowdown rate for the animation.
     */
    private final int frameRate = 4;

    /**
     * Walking state or <code>null</code>. 
     */
    private Player.State state = Player.State.DOWN;

    /**
     * Current arsenal to use (bomb count).
     */
    public int bombCount = Globals.DEFAULT_BOMB_COUNT;

    /**
     * Bomb range for this player. Assigned to {@link BombCell#range}.
     */
    public int bombRange = Globals.DEFAULT_BOMB_RANGE;

    /*
     * 
     */
    public PlayerInfo(Player player, PlayerImageData playerImageData)
    {
        this.player = player;
        this.images = playerImageData;
    }

    /*
     * @see ISprite
     */
    public void paint(Graphics2D g)
    {
        if (state == Player.State.DEAD)
        {
            return;
        }

        final BufferedImage image = images.get(state, frame / frameRate);
        final Rectangle envelope = images.envelope;
        final Point p = new Point(location);
        p.translate(-envelope.width / 2, -envelope.height / 2);
        g.drawImage(image, null, p.x, p.y);
    }

    /**
     * Update internal frame state with the controller's.
     * 
     * @see #frame
     */
    public void controllerState(Direction signal)
    {
        /*
         * Keep on advancing frame counter until dead. 
         */
        if (state == Player.State.DEAD)
            return;

        if (state == Player.State.DYING)
        {
            if ((frame + 1) / frameRate == images.getFrameCount(state))
            {
                state = Player.State.DEAD;
            }
            frame++;
            return;
        }

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
                if (frame == 0) 
                {
                    // If changed the mode, start from the first 'active' frame.
                    frame = frameRate;
                }
                else
                {
                    frame++;
                }
            }
        }
    }

    /**
     * Kills the player, initiating the death state's animation. 
     */
    public void kill()
    {
        this.state = Player.State.DYING;
        this.frame = 0;
    }

    /**
     * @return Returns <code>true</code> if the player is dead (dying sequence is finished).
     */
    public boolean isDead()
    {
        return state == Player.State.DEAD;
    }
    
    /**
     * @return Returns <code>true</code> if the player is either dying or dead.
     */
    public boolean isKilled()
    {
        return state == Player.State.DEAD || state == Player.State.DYING;
    }
}
