package com.dawidweiss.dyna;


/**
 * A controller for the game.
 */
public interface IController
{
    enum Direction
    {
        LEFT, RIGHT, UP, DOWN
    }

    /**
     * @return Current direction or <code>null</code> if none.
     */
    public Direction getCurrent();

    /**
     * @return Return <code>true</code> if player has activated
     * bomb-dropping facility.
     */
    public boolean dropsBomb();
}
