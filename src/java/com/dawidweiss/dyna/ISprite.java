package com.dawidweiss.dyna;

import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * A sprite is an object that is superimposed on top of {@link Board}'s cells. The name
 * is traditional, in our case it is simply an image at a given set of coordinates.
 */
interface ISprite
{
    /**
     * Upper left corner of the sprite.
     */
    public Point getPosition();

    /**
     * Image to be drawn.  
     */
    public BufferedImage getImage();
}
