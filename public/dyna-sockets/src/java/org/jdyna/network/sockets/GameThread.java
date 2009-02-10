package org.jdyna.network.sockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.Game;

/**
 * A thread running a game on the server.
 */
final class GameThread extends Thread
{
    private final static Logger logger = LoggerFactory.getLogger(GameThread.class); 
    private final GameContext context;

    /*
     * 
     */
    public GameThread(GameContext gameContext)
    {
        this.context = gameContext;
        setName("game-" + gameContext.getHandle().gameName);
        setDaemon(true);
    }

    /*
     * 
     */
    @Override
    public void run()
    {
        context.getGame().run(Game.Mode.INFINITE_DEATHMATCH);
        logger.info("Game thread finished [" + getName() + "]");
    }
}
