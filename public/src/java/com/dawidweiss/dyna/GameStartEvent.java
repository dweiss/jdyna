package com.dawidweiss.dyna;


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
    private final BoardInfo boardInfo;

    /*
     *  
     */
    public GameStartEvent(BoardInfo boardInfo)
    {
        super(GameEvent.Type.GAME_START);
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
