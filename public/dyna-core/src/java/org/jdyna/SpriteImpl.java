package org.jdyna;

import java.awt.Point;
import java.io.Serializable;

/**
 * A stub implementation of {@link ISprite}.
 */
public class SpriteImpl implements ISprite, Serializable
{
    private static final long serialVersionUID = 0x200812241508L;

    public final ISprite.Type type;
    public final Point position = new Point();
    public int animationFrame;
    public int animationState;

    public SpriteImpl(ISprite.Type type)
    {
        this.type = type;
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

    public Type getType()
    {
        return type;
    }
}