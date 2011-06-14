package org.jdyna;

import java.awt.Point;

import org.jdyna.IPlayerController.Direction;
import org.jdyna.Player.State;


/**
 * Extra {@link Player} information for the {@link Game}.
 */
final class PlayerInfo implements IPlayerSprite
{
    /* */
    public final Player player;
    
    /* */
    public final ISprite.Type spriteType;

    /**
     * Coordinates of this player (it's centerpoint). The rectangle actually taken by the
     * player and the position of the sprite is determined by the player's implementation.
     */
    final Point location = new Point();

    /**
     * Movement speed in each direction.
     */
    final Point speed = new Point(2, 2);

    /**
     * An increasing counter of frames if the player is in walking state.
     */
    private int stateFrame;

    /**
     * An ever-increasing frame counter during the game.
     */
    private int frame;

    /**
     * Current walking state. 
     */
    private Player.State state = Player.State.DOWN;

    /**
     * Current arsenal to use (bomb count).
     */
    int bombCount = Globals.DEFAULT_BOMB_COUNT;

    /**
     * Bomb range for this player. Assigned to {@link BombCell#range}.
     */
    int bombRange = Globals.DEFAULT_BOMB_RANGE;

    /**
     * This field stores the most recent frame number when a bomb was dropped. The purpose of this
     * is to avoid dropping two bombs when crossing the line between two grid cells.
     * 
     * @see Globals#BOMB_DROP_DELAY
     */
    public int lastBombFrame = Integer.MIN_VALUE;

    /**
     * Frame number after which immortality ends for this player.
     */
    private int immortalityEndsAtFrame = Integer.MIN_VALUE;

    /**
     * If the player is dead, this is the frame number of its death.
     */
    private int deathAtFrame;
    
    /**
     * When did the player join the game?
     */
    private final int joinedAtFrame;

    /**
     * Number of killed enemies. The more, the better.
     */
    private int killedEnemies;

    /**
     * Number of lives lost in battle. The fewer, the better.  
     */
    private int reincarnations; 

    /**
     * Number of reincarnations a player has left.  
     */
    private int livesLeft; 

    /*
     * 
     */
    PlayerInfo(Player player, int lives, ISprite.Type spriteType, int joinedAtFrame)
    {
        assert lives > 0 : "Number of lives must be > 0";

        this.player = player;
        this.spriteType = spriteType;
        this.livesLeft = lives;
        this.joinedAtFrame = joinedAtFrame;
    }

    /**
     * Update internal frame state with the controller's.
     */
    void nextFrameUpdate(Direction signal)
    {
        /*
         * Keep on advancing frame counter even if dead. 
         */
        frame++;
        if (state == Player.State.DEAD)
            return;

        if (signal == null)
        {
            // No movement at all. Reset to the first frame in the current state.
            stateFrame = 0;
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
                stateFrame = 0;
            }
            else
            {
                if (stateFrame == 0) 
                {
                    /* 
                     * If changed the mode, start from the first 'active' frame
                     * in the animation sequence.
                     */
                    stateFrame = 1;
                }
                else
                {
                    stateFrame++;
                }
            }
        }
    }

    /**
     * Kills the player, initiating the death state's animation. 
     */
    public void kill()
    {
        if (this.state == Player.State.DEAD)
        {
            return;
        }

        this.state = Player.State.DEAD;
        this.stateFrame = 0;
        this.deathAtFrame = frame;
        this.livesLeft--;        
    }

    /**
     * @return Returns <code>true</code> if the player is dead (dying sequence is finished).
     */
    public boolean isDead()
    {
        return state == Player.State.DEAD;
    }

    /**
     * A stone-dead player is dead and has no chances of coming back to life.
     */
    boolean isStoneDead()
    {
        return isDead() && livesLeft <= 0;
    }

    /**
     * @see IPlayerSprite
     */
    public int getAnimationFrame()
    {
        return this.stateFrame;
    }

    /**
     * @see IPlayerSprite
     */
    public int getAnimationState()
    {
        return this.state.ordinal();
    }

    /**
     * @see IPlayerSprite
     */
    public ISprite.Type getType()
    {
        return spriteType;
    }

    /**
     * @see IPlayerSprite
     */
    public Point getPosition()
    {
        return new Point(location);
    }
    
    /**
     * @see IPlayerSprite
     */
    public String getName()
    {
        return player.name;
    }

    /**
     * Temporal immortality is possible when the player is resurrected in deatch match mode.
     */
    public boolean isImmortal()
    {
        return !isStoneDead() && frame < immortalityEndsAtFrame;
    }
    
    /**
     * Make the given player immortal for a little while.
     */
    void makeImmortal(int immortalityFrameCount)
    {
        this.immortalityEndsAtFrame = frame + immortalityFrameCount;
    }

    /**
     * Should the player be resurrected?
     */
    boolean shouldResurrect()
    {
        return livesLeft > 0 && frame > deathAtFrame + Globals.DEFAULT_RESURRECTION_FRAMES;
    }

    /**
     * Ressurect this player.
     */
    void resurrect()
    {
        this.stateFrame = 0;
        this.state = State.DOWN;
        this.bombCount = Globals.DEFAULT_BOMB_COUNT;
        this.bombRange = Globals.DEFAULT_BOMB_RANGE;

        this.reincarnations++;
        makeImmortal(Globals.DEFAULT_IMMORTALITY_FRAMES);
    }
 
    /**
     * Collect a token for killing an enemy.
     */
    void collectKill()
    {
        this.killedEnemies++;
    }

    /**
     * Return the current status information.
     */
    PlayerStatus getStatus()
    {
        final PlayerStatus ps = new PlayerStatus(getName());
        ps.dead = isDead();
        ps.deathFrame = deathAtFrame + joinedAtFrame;
        ps.immortal = isImmortal();
        ps.killedEnemies = killedEnemies;
        ps.livesLeft = livesLeft;
        return ps;
    }
}
