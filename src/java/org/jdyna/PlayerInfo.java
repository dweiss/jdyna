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
    Point speed = new Point(Constants.DEFAULT_PLAYER_SPEED, Constants.DEFAULT_PLAYER_SPEED);

    /**
     * If player collects the speed bonus the variable is changed. 
     * If player doesn't have speed bonus the variable is set to 1.0. 
     */
    float speedMultiplier = 1.0f;
    
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
    int bombCount;

    /**
     * Bomb range for this player. Assigned to {@link BombCell#range}.
     */
    int bombRange;
    
    /**
     * Stores the actual bomb range when player is under the influence of max range bonus.
     */
    int storedBombRange = Integer.MIN_VALUE;
    
    /**
     * Frame number after which diarrhea bonus ends for the player.
     */
    int diarrheaEndsAtFrame = Integer.MIN_VALUE;

    /**
     * Frame number after which max range bonus ends for the player.
     */
    int maxRangeEndsAtFrame = Integer.MIN_VALUE;
    
    /**
     * Frame number after which slow down or speed up bonus ends for this player.
     */
    int speedEndsAtFrame = Integer.MIN_VALUE;

    /**
     * Frames number the player is under Wall Walking Bonus influence
     */
    int crateWalkingEndsAtFrame = Integer.MIN_VALUE;
    
    /**
     * Indicates whether or not player can walk trough crates
     */
    boolean canWalkCrates = false; 
    
    /**
     * Frames number the player is under Bomb Walking Bonus influence
     */
    int bombWalkingEndsAtFrame = Integer.MIN_VALUE;

    /**
     * Indicates whether or not player can walk through bombs
     */
    boolean canWalkBombs = false;

    /**
     * Indicates whether or not the player has the Ahmed bonus.
     * Ahmed bonus means that the next bomb the player drops explodes
     * immediately but doesn't kill the player.
     */
    boolean isAhmed = false;
    
    /**
     * This field stores the most recent frame number when a bomb was dropped. The purpose of this
     * is to avoid dropping two bombs when crossing the line between two grid cells.
     * 
     * @see Constants#BOMB_DROP_DELAY
     */
    int lastBombFrame = Integer.MIN_VALUE;

    /**
     * Frame number after which immortality ends for this player.
     */
    private int immortalityEndsAtFrame = Integer.MIN_VALUE;
    
    /**
     * If player collects the immortality bonus the variable is changed.  
     */
    boolean immortalityBonusCollected = false;
    
    /**
     * Frame number after which no bombs bonus ends for this player.
     */
    int noBombsEndsAtFrame = Integer.MIN_VALUE; 

    /**
     * Frame number after which controller reverse disease ends for this player.
     */
    int controllerReverseEndsAtFrame = Integer.MIN_VALUE;

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

    /**
     * Global configuration for the player.
     */
    private GameConfiguration conf;

    /*
     * 
     */
    PlayerInfo(GameConfiguration conf, Player player, int lives, ISprite.Type spriteType, int joinedAtFrame)
    {
        assert lives > 0 : "Number of lives must be > 0";

        this.player = player;
        this.spriteType = spriteType;
        this.livesLeft = lives;
        this.joinedAtFrame = joinedAtFrame;
        this.conf = conf;

        bombCount = conf.DEFAULT_BOMB_COUNT;
        bombRange = conf.DEFAULT_BOMB_RANGE;
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
        return livesLeft > 0 && frame > deathAtFrame + conf.DEFAULT_RESURRECTION_FRAMES;
    }

    /**
     * Ressurect this player.
     */
    void resurrect()
    {
        this.stateFrame = 0;
        this.state = State.DOWN;
        this.bombCount = conf.DEFAULT_BOMB_COUNT;
        this.bombRange = conf.DEFAULT_BOMB_RANGE;
        this.immortalityBonusCollected = false;
        this.controllerReverseEndsAtFrame = Integer.MIN_VALUE;
        this.isAhmed = false;

        this.reincarnations++;
        makeImmortal(conf.DEFAULT_IMMORTALITY_FRAMES);
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

    /* */
    @Override
    public int getBombCount()
    {
        return bombCount;
    }

	@Override
	public int getLifeCount() 
	{
		return livesLeft;
	}

	@Override
	public int getBombRange() 
	{
		return bombRange;
	}

	@Override
	public int getBombWalkingEndsAtFrame() 
	{
		return bombWalkingEndsAtFrame;
	}

	@Override
	public int getControllerReverseEndsAtFrame() 
	{
		return controllerReverseEndsAtFrame;
	}

	@Override
	public int getCrateWalkingEndsAtFrame() 
	{
		return crateWalkingEndsAtFrame;
	}

	@Override
	public int getDiarrheaEndsAtFrame() 
	{
		return diarrheaEndsAtFrame;
	}

	@Override
	public int getImmortalityEndsAtFrame() 
	{
		return immortalityEndsAtFrame;
	}

	@Override
	public int getMaxRangeEndsAtFrame() 
	{
		return maxRangeEndsAtFrame;
	}

	@Override
	public int getNoBombsEndsAtFrame() 
	{
		return noBombsEndsAtFrame;
	}

	@Override
	public int getSlowDownEndsAtFrame() 
	{
		if (speedMultiplier < 1.0f)
			return speedEndsAtFrame;
		else return Integer.MIN_VALUE;
	}

	@Override
	public int getSpeedUpEndsAtFrame() 
	{
		if (speedMultiplier > 1.0f)
			return speedEndsAtFrame;
		else return Integer.MIN_VALUE;
	}

	@Override
	public boolean isAhmed() 
	{
		return isAhmed;
	}
}
