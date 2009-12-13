package org.jdyna;


/**
 * An event dispatched at the beginning of a single game.
 */
public final class GameStartEvent extends GameEvent
{
    /**
     * @see GameEvent#serialVersionUID
     */
    private static final long serialVersionUID = 0x200912130117L;

    /**
     * Board information for the game.
     */
    private BoardInfo boardInfo;

    /**
     * Configuration and settings for the game.
     */
    private Globals conf;

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
    public GameStartEvent(Globals conf, BoardInfo boardInfo)
    {
        this();
        this.boardInfo = boardInfo;
        this.conf = conf;
    }

    /*
     * 
     */
    public BoardInfo getBoardInfo()
    {
        return boardInfo;
    }

    /*
     * 
     */
    public Globals getConfiguration()
    {
        return conf;
    }
}
