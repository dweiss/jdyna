package org.jdyna.network.sockets;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;

import com.dawidweiss.dyna.*;
import com.google.common.collect.Maps;

/**
 * A server-side context for all gameHandles in progress. Must be thread-safe.
 */
public final class GameServerContext
{
    /**
     * Active gameHandles.
     */
    private Map<String, GameContext> games = Maps.newHashMap();

    /**
     * 
     */
    private final AtomicInteger gameID = new AtomicInteger();

    /**
     * Loaded boards.
     */
    private Boards boards;

    /**
     * Shared broadcaster.
     */
    private UDPBroadcaster udpBroadcaster;

    /**
     * Initialize context.
     */
    public GameServerContext()
    {
        /*
         * Load board configurations.
         */
        try
        {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            this.boards = Boards.read(new InputStreamReader(cl
                .getResourceAsStream("boards.conf"), "UTF-8"));

            this.udpBroadcaster = new UDPBroadcaster(GameServer.DEFAULT_UDP_BROADCAST);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not load boards.", e);
        }
    }

    /*
     * 
     */
    public boolean hasGame(String gameName)
    {
        synchronized (this)
        {
            return games.containsKey(gameName);
        }
    }

    /*
     * 
     */
    public boolean hasBoard(String boardName)
    {
        return boards.getBoardNames().contains(boardName);        
    }

    /*
     * 
     */
    public GameHandle createNewGame(String gameName, String boardName)
    {
        synchronized (this)
        {
            assert !hasGame(gameName);

            final Board board;
            if (!StringUtils.isEmpty(boardName))
            {
                board = boards.get(boardName);
            }
            else
            {
                board = boards.get(0);
            }

            final BoardInfo boardInfo = new BoardInfo(
                new Dimension(board.width, board.height), Globals.DEFAULT_CELL_SIZE);

            final GameHandle handle = new GameHandle(gameID.incrementAndGet(), gameName, boardInfo);
            final GameContext gameContext = new GameContext(handle, new Game(board, boardInfo));

            gameContext.addFrameDataListener(
                new FrameDataBroadcaster(gameContext, this.udpBroadcaster));
            gameContext.startGame();

            games.put(gameName, gameContext);
            return games.get(gameName).getHandle();
        }
    }
}
