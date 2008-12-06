package com.dawidweiss.dyna.view;

import java.awt.Point;


/**
 * A sprite is an object that is superimposed on top of {@link Board}'s cells. The name
 * is traditional.
 * <p>
 * All animation sequences for a sprite are split into "states". Each state
 * may have one or more image frames. 
 */
public interface ISprite
{
    /** Sprite type. */
    SpriteType getType();

    /** Current animation state */
    int getAnimationState();

    /** Current animation frame */
    int getAnimationFrame();

    /** Current _center_ of the sprite. */
    Point getPosition();
}
