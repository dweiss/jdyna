package org.jdyna.network.sockets;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.jdyna.network.packetio.Packet;
import org.jdyna.network.packetio.UDPPacketEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.Board;
import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.Boards;
import com.dawidweiss.dyna.Game;
import com.dawidweiss.dyna.Globals;
import com.google.common.collect.Maps;

/**
 * All {@link GameContext}s in progress, boards and a sequencer for game IDs. Must be
 * thread-safe.
 */
public final class GameServerContext
{
    private final static Logger logger = LoggerFactory.getLogger(GameServerContext.class);

    /**
     * Broadcast address for dispatching frame events.
     */
    public final static String BROADCAST_ADDRESS = "255.255.255.255";
    
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
    private UDPPacketEmitter udpBroadcaster;

    /**
     * Initialize context.
     */
    public GameServerContext(int broadcastPort)
    {
        try
        {
            /*
             * Load board configurations.
             */
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            this.boards = Boards.read(new InputStreamReader(cl
                .getResourceAsStream("boards.conf"), "UTF-8"));

            /*
             * Set up broadcast socket.
             */
            final DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setReuseAddress(true);
            socket.setSendBufferSize(Packet.MAX_LENGTH);
            this.udpBroadcaster = new UDPPacketEmitter(socket);
            this.udpBroadcaster.setDefaultTarget(Inet4Address
                .getByName(BROADCAST_ADDRESS), broadcastPort);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Problems setting up the server context.", e);
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
    public boolean hasGame(int gameID)
    {
        synchronized (this)
        {
            for (GameContext c : games.values())
            {
                if (c.getHandle().gameID == gameID) return true;
            }
            return false;
        }
    }

    /*
     * 
     */
    public boolean hasBoard(String boardName)
    {
        synchronized (this)
        {
            return boards.getBoardNames().contains(boardName);
        }
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

            final BoardInfo boardInfo = new BoardInfo(new Dimension(board.width,
                board.height), Globals.DEFAULT_CELL_SIZE);

            final GameHandle handle = new GameHandle(gameID.incrementAndGet(), gameName,
                boardInfo);
            final GameContext gameContext = new GameContext(handle, new Game(board,
                boardInfo));

            gameContext.addFrameDataListener(new FrameDataBroadcaster(gameContext,
                udpBroadcaster));
            gameContext.startGame();

            logger.info("New game [" + handle.gameID + "]: " + handle.gameName);

            games.put(gameName, gameContext);
            return games.get(gameName).getHandle();
        }
    }

    /*
     * 
     */
    public GameContext getGameContext(int gameID)
    {
        synchronized (this)
        {
            for (GameContext c : games.values())
            {
                if (c.getHandle().gameID == gameID) return c;
            }
            throw new IllegalStateException("No such game: " + gameID);
        }
    }
}
