package org.jdyna;


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
     *  
     */
    public GameOverEvent()
    {
        super(GameEvent.Type.GAME_OVER);
    }
}
