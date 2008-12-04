package com.dawidweiss.dyna;

import java.util.EnumSet;

/**
 * A controller for the game.
 */
public interface IController
{
    enum Direction
    {
        LEFT, RIGHT, UP, DOWN
    }

    public EnumSet<Direction> getCurrent();
}
