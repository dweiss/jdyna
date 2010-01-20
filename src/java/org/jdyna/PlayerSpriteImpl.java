package org.jdyna;


/**
 * Static immutable structure exposing {@link IPlayerSprite}. Implementation uses bit coding
 * to save a bit on serialization in case more flags show up.
 */
public final class PlayerSpriteImpl extends SpriteImpl implements IPlayerSprite
{
    private static final long serialVersionUID = 0x200901071550L;

    private final static int FLAG_IMMORTAL = 1 << 0;
    private final static int FLAG_DEAD = 1 << 1;
    private final short flags;
    private final short bombCount;
    private final short lifeCount;
    private final short bombRange;
    private final int diarrheaEndsAtFrame;
    private final int immortalityEndsAtFrame;
    private final int maxRangeEndsAtFrame;
    private final int noBombsEndsAtFrame;
    private final int speedUpEndsAtFrame;
    private final int slowDownEndsAtFrame;
    private final int crateWalkingEndsAtFrame;
    private final int bombWalkingEndsAtFrame;
    private final int controllerReverseEndsAtFrame;
    private final boolean isAhmed;

    public final String name;

	public PlayerSpriteImpl(ISprite.Type type, String name, boolean dead,
			boolean immortal, int bombCount, int lifeCount, int bombRange,
			int diarrheaEndsAtFrame, int immortalityEndsAtFrame,
			int maxRangeEndsAtFrame, int noBombsEndsAtFrame,
			int speedUpEndsAtFrame, int slowDownEndsAtFrame,
			int crateWalkingEndsAtFrame, int bombWalkingEndsAtFrame,
			int controllerReverseEndsAtFrame, boolean isAhmed)
    {
        super(type);

        this.name = name;
        flags = (short) ((dead ? FLAG_DEAD : 0) | (immortal ? FLAG_IMMORTAL : 0));

        this.bombCount = (short) bombCount;
        this.lifeCount = (short) lifeCount;
        this.bombRange = (short) bombRange; 
        this.diarrheaEndsAtFrame = diarrheaEndsAtFrame;
        this.immortalityEndsAtFrame = immortalityEndsAtFrame;
        this.maxRangeEndsAtFrame = maxRangeEndsAtFrame;
        this.noBombsEndsAtFrame = noBombsEndsAtFrame; 
        this.speedUpEndsAtFrame = speedUpEndsAtFrame;
        this.slowDownEndsAtFrame = slowDownEndsAtFrame;
        this.crateWalkingEndsAtFrame = crateWalkingEndsAtFrame;
        this.bombWalkingEndsAtFrame = bombWalkingEndsAtFrame;
        this.controllerReverseEndsAtFrame = controllerReverseEndsAtFrame;
        this.isAhmed = isAhmed;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isDead()
    {
        return (flags & FLAG_DEAD) != 0;
    }

    @Override
    public boolean isImmortal()
    {
        return (flags & FLAG_IMMORTAL) != 0;
    }
    
    @Override
    public int getBombCount()
    {
        return bombCount;
    }

	@Override
	public int getLifeCount() 
	{
		return lifeCount;
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
		return slowDownEndsAtFrame;
	}

	@Override
	public int getSpeedUpEndsAtFrame() 
	{
		return speedUpEndsAtFrame;
	}

	@Override
	public boolean isAhmed() 
	{
		return isAhmed;
	}

    @Override
    public IPlayerSprite clone()
    {
        try
        {
            return (IPlayerSprite) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            // since we implement Cloneable, this shouldn't occur.
            return null;
        }
    }
}
