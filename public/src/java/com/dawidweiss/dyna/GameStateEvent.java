package com.dawidweiss.dyna;

import java.util.List;


/**
 * Everything required to render the game's playfield during a single frame. 
 */
public final class GameStateEvent extends GameEvent
{
    public final Cell [][] cells;
    public final List<? extends IPlayerSprite> players;

    /*
     * 
     */
    public GameStateEvent(Cell [][] cells, List<? extends IPlayerSprite> players)
    {
        super(GameEvent.Type.GAME_STATE);

        this.cells = cells;
        this.players = players;
    }
}
