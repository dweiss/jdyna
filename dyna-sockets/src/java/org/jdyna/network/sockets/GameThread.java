package org.jdyna.network.sockets;

import com.dawidweiss.dyna.Game;

/**
 * Threads running games on the server.
 */
public class GameThread extends Thread
{
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
    }
}
