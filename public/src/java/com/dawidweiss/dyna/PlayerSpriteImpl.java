package com.dawidweiss.dyna;


/**
 * Static immutable structure exposing {@link IPlayerSprite}.
 */
public final class PlayerSpriteImpl extends SpriteImpl implements IPlayerSprite
{
    private static final long serialVersionUID = 0x200812241508L;

    public final String name;

    public PlayerSpriteImpl(ISprite.Type type, String name)
    {
        super(type);
        this.name = name;
    }

    public String getName()
    {
        return name;
    }    
}
