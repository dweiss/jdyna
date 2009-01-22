package com.dawidweiss.dyna;

/**
 * A controller for a player, conceptually similar to a joystick.
 * <p>
 * With human players, a controller will translate events from a physical device such the
 * keyboard. Computer-driven players (bots) will implement this interface to pass
 * synthetic events, resulting from whatever artificial intelligence algorithm is
 * implemented inside the bot.
 * <p>
 * The class implementing {@link IPlayerController} may also wish to implement
 * {@link IGameEventListener}. If this is the case, the controller will be automatically
 * subscribed to game events.
 */
public interface IPlayerController
{
    /**
     * Direction of movement in {@link IPlayerController#getCurrent()}.
     */
    enum Direction
    {
        LEFT, RIGHT, UP, DOWN
    }

    /**
     * @return Current direction or <code>null</code> if none.
     */
    public Direction getCurrent();

    /**
     * @return Return <code>true</code> if this player wants to drop a bomb at the current
     *         location. Note that:
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
