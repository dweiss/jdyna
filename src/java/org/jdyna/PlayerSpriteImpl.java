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

    public final String name;

    public PlayerSpriteImpl(ISprite.Type type, String name, boolean dead, boolean immortal)
    {
        super(type);

        this.name = name;
        flags = (short) ((dead ? FLAG_DEAD : 0) | (immortal ? FLAG_IMMORTAL : 0)); 
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
}
