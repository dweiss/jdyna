package org.jdyna;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Game wall time.
 */
@SuppressWarnings("serial")
public final class GameWallTimeEvent extends GameEvent
{
    private static SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);

    /*
     * Wall time in ISO format.
     */
    public String wallTime; 

    /*
     *  
     */
    protected GameWallTimeEvent()
    {
        super(GameEvent.Type.GAME_WALL_TIME);
    }
    
    /*
     *  
     */
    public GameWallTimeEvent(Date now)
    {
        this();

        synchronized (iso)
        {
            wallTime = iso.format(now);
        }
    }
}
