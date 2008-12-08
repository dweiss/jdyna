package com.dawidweiss.dyna.view;

import com.dawidweiss.dyna.Cell;

/**
 * Represents the state of a single game at the given moment in time (single frame). The
 * state should <b>not</b> be preserved between frames as it may be changed by the
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
