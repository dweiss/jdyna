package com.dawidweiss.dyna.view;

import com.dawidweiss.dyna.Cell;

/**
 * Represents the state of a single game at the given moment in time (single frame).
 * <p>
 * The state contains all the information required to render the state of a game (cells in
 * the grid, location of players and their animation frames).
 * <p>
 * The state should <b>not</b> be preserved between frames as it may be changed by the
 * controller.
 */
public interface IBoardSnapshot
{
    /**
     * Current playfield grid.
     */
    Cell [][] getCells();

    /**
     * Current position and attributes of players.
     */
    IPlayerSprite [] getPlayers();
}
