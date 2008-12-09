package com.dawidweiss.dyna.corba;

import java.awt.Point;

import com.dawidweiss.dyna.view.IPlayerSprite;
import com.dawidweiss.dyna.view.SpriteType;

/**
 * Temporary structure used in {@link Adapters}.
 */
final class PlayerSpriteImpl implements IPlayerSprite
{
    private final String name;
    private final SpriteType type;

    public Point position = new Point();
    public int animationState;
    public int animationFrame; 

    PlayerSpriteImpl(int playerIndex, String name)
    {
        this.name = name;
        
        final SpriteType [] types = SpriteType.getPlayerSprites();
        this.type = types[playerIndex % types.length];
    }

    public String getName()
    {
        return name;
    }

    public int getAnimationFrame()
    {
        return animationFrame;
    }

    public int getAnimationState()
    {
        return animationState;
    }

    public Point getPosition()
    {
        return position;
    }

    public SpriteType getType()
    {
        return type;
    }
}
