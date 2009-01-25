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
import org.jdyna.network.packetio.SerializablePacket;
import org.jdyna.network.packetio.UDPPacketEmitter;
import org.jdyna.network.sockets.packets.ServerInfo;
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
     * How frequently should auto-discovery messages be sent?
     */
    public final static int AUTO_DISCOVERY_INTERVAL = 1000 * 2;

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
     * Server information.
     */
    private final ServerInfo serverInfo; 

    /**
     * Auto-discovery daemon.
     */
    private final Thread autoDiscoveryDaemon = new Thread()
    {
        {
            this.setDaemon(true);
        }

        public void run()
        {
            try
            {
                final SerializablePacket autodiscoveryPacket = new SerializablePacket();
                autodiscoveryPacket.serialize(PacketIdentifiers.SERVER_BEACON, 0, serverInfo);

                while (!interrupted())
                {
                    sleep(AUTO_DISCOVERY_INTERVAL);
                    udpBroadcaster.send(autodiscoveryPacket);
                }
            }
            catch (IOException e)
            {
                logger.warn("Auto-discovery daemon failed.", e);
            }
            catch (InterruptedException e)
            {
                // Do nothing.
            }
        }
    };

    /**
     * Maximum number of concurrent games running on the server.
     */
    private final int maximumGames;

    /**
     * Initialize context.
     */
    public GameServerContext(ServerInfo serverInfo, int maxGames)
    {
        try
        {
            this.maximumGames = maxGames;

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
                .getByName(BROADCAST_ADDRESS), serverInfo.UDPBroadcastPort);

            /*
             * Start the auto-discovery daemon.
             */
            this.serverInfo = serverInfo;
            autoDiscoveryDaemon.start();
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

            if (this.games.size() + 1 > maximumGames)
            {
                throw new FailureResponseException("Maximum number of concurrent games" +
                		" on the server reached (" + games.size() + "). Use one" +
                				" of the active games."); 
            }

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
