package com.dawidweiss.dyna;

/**
 * A controller for the game.
 */
public interface IPlayerController
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
     * @return Return <code>true</code> if this player wants to drop a bomb at the
     *         current location. Note that:
     *         <ul>
     *         <li>the game controller will not allow two bombs at the same location; if
     *         two players attempt to leave a bomb in the same grid cell, only one of them
     *         will succeed,
     *         <li>the game controller will keep on dropping bombs as the player moves
     *         from one grid's cell to another.
     *         </ul>
     */
    public boolean dropsBomb();
}
