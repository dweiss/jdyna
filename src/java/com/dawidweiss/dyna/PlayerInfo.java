package com.dawidweiss.dyna;

import java.awt.Point;

import com.dawidweiss.dyna.IController.Direction;
import com.dawidweiss.dyna.view.IPlayer;
import com.dawidweiss.dyna.view.SpriteType;

/**
 * Extra {@link Player} information for the {@link Game}.
 */
final class PlayerInfo implements IPlayer
{
    /* */
    public final Player player;
    
    /* */
    public final int playerIndex;

    /**
     * Coordinates of this player (it's centerpoint). The rectangle actually taken by the
     * player and the position of the sprite is determined by the player's implementation.
     */
    public final Point location = new Point();

    /**
     * Movement speed in each direction.
     */
    public final Point speed = new Point(2, 2);

    /**
     * An increasing counter of frames if the player is in walking state.
     */
    public int frame;

    /**
     * Current walking state. 
     */
    public Player.State state = Player.State.DOWN;

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
    public PlayerInfo(Player player, int playerIndex)
    {
        this.player = player;
        this.playerIndex = playerIndex;
    }

    /**
     * Update internal frame state with the controller's.
     * 
     * @see #frame
     */
    public void updateState(Direction signal)
    {
        /*
         * Keep on advancing frame counter until dead. 
         */
        if (state == Player.State.DEAD)
            return;

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
                    /* 
                     * If changed the mode, start from the first 'active' frame
                     * in the animation sequence.
                     */
                    frame = 1;
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
        this.state = Player.State.DEAD;
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
     * @see IPlayer
     */
    @Override
    public int getAnimationFrame()
    {
        return this.frame;
    }

    /**
     * @see IPlayer
     */
    @Override
    public int getAnimationState()
    {
        return this.state.ordinal();
    }

    /**
     * @see IPlayer
     */
    @Override
    public SpriteType getType()
    {
        final SpriteType [] playerSprites = SpriteType.getPlayerSprites();
        return playerSprites[playerIndex % playerSprites.length];
    }

    /**
     * @see IPlayer
     */
    @Override
    public Point getPosition()
    {
        return new Point(location);
    }
}
