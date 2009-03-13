package org.jdyna;


/**
 * An event dispatched at the beginning of a single game.
 */
public final class GameStartEvent extends GameEvent
{
    /**
     * @see GameEvent#serialVersionUID
     */
    private static final long serialVersionUID = 0x200812241355L;

    /**
     * Board information for the game.
     */
    private BoardInfo boardInfo;

    /*
     * 
     */
    protected GameStartEvent()
    {
        super(GameEvent.Type.GAME_START);
    }

    /*
     *  
     */
    public GameStartEvent(BoardInfo boardInfo)
    {
        this();
        this.boardInfo = boardInfo;
    }

    /*
     * 
     */
    public BoardInfo getBoardInfo()
    {
        return boardInfo;
    }
}
