package com.dawidweiss.dyna.corba;

import java.awt.Point;

import com.dawidweiss.dyna.view.IPlayer;
import com.dawidweiss.dyna.view.SpriteType;

/**
 * Temporary structure used in {@link Adapters}.
 */
final class PlayerImpl implements IPlayer
{
    private final String name;
    private final SpriteType type;

    public Point position = new Point();
    public int animationState;
    public int animationFrame; 

    PlayerImpl(int playerIndex, String name)
    {
        this.name = name;
        
        final SpriteType [] types = SpriteType.getPlayerSprites();
        this.type = types[playerIndex % types.length];
    }

    public String getName()
    {
        return name;
    }

    @Override
    public int getAnimationFrame()
    {
        return animationFrame;
    }

    @Override
    public int getAnimationState()
    {
        return animationState;
    }

    @Override
    public Point getPosition()
    {
        return position;
    }

    @Override
    public SpriteType getType()
    {
        return type;
    }
}
