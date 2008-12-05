package com.dawidweiss.dyna;

import java.awt.Graphics2D;

/**
 * A sprite is an object that is superimposed on top of {@link Board}'s cells. The name
 * is traditional, in our case it is simply an image at a given set of coordinates.
 */
interface ISprite
{
    /**
     * Paint the sprite on the graphic context.
     */
    public void paint(Graphics2D g);
}
