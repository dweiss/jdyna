package com.dawidweiss.dyna;


/**
 * An event dispatched at the end of a single game.
 */
public final class GameOverEvent extends GameEvent
{
    /**
     * @see GameEvent#serialVersionUID
     */
    private static final long serialVersionUID = 0x200812241355L;

    /*
     * TODO: At some point it makes sense to add the game result to this
     * event. 
     */
    public GameOverEvent()
    {
        super(GameEvent.Type.GAME_OVER);
    }
}
