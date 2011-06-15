package org.jdyna.network.sockets;

import java.awt.Dimension;
import java.io.*;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jdyna.*;
import org.jdyna.network.packetio.*;
import org.jdyna.network.sockets.packets.ServerInfo;
import org.jdyna.serialization.GameWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
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

    /*
     * We want to avoid collisions between servers and games that possibly started
     * some time ago. We start with a random game ID.
     */
    private final AtomicInteger gameID = new AtomicInteger(new Random().nextInt());

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
                    sleep(GameServer.AUTO_DISCOVERY_INTERVAL);
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
     * Where should all the logs be stored?
     */
    private File gameStateLogDir;

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
    public GameHandle createNewGame(GameConfiguration conf, String gameName, String boardName)
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
                board.height), Constants.DEFAULT_CELL_SIZE);

            final GameHandle handle = new GameHandle(gameID.incrementAndGet(), gameName,
                boardName, boardInfo, conf);

            final GameContext gameContext = new GameContext(handle, 
                new Game(conf, board, boardInfo));

            gameContext.addFrameDataListener(new FrameDataBroadcaster(gameContext,
                udpBroadcaster));
            
            if (this.gameStateLogDir != null)
            {
                try
                {
                    final File gameLogDir = generateLogDir(handle);
                    final File gameLog = new File(gameLogDir, "game.log");
                    gameContext.getGame().addListener(new GameWriter(new FileOutputStream(gameLog)));
                }
                catch (IOException e)
                {
                    logger.warn("Could not create game log.", e);
                }
            }

            gameContext.startGame();

            logger.info("New game [" + handle.gameID + "]: " + handle.gameName);

            games.put(gameName, gameContext);
            return games.get(gameName).getHandle();
        }
    }

    /**
     * Create logging directory for a single game, write initial information about the game.
     */
    private File generateLogDir(GameHandle handle) throws IOException
    {
        synchronized (this)
        {
            File gameLogDir;
            do
            {
                SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.ENGLISH);
                String timestamp = f.format(new Date());
                gameLogDir = new File(gameStateLogDir, "game-" + timestamp);
            } while (gameLogDir.exists());

            gameLogDir.mkdir();

            String gameInfo = 
                "Name: " + StringEscapeUtils.escapeJava(handle.gameName) + "\n" +
                "ID: " + handle.gameID + "\n" +
                "Board: " + handle.boardName;

            FileUtils.writeStringToFile(new File(gameLogDir, "INFO"), gameInfo, "UTF-8");

            return gameLogDir;
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

    /*
     * 
     */
    public List<GameHandle> getGameHandles()
    {
        synchronized (this)
        {
            ArrayList<GameHandle> handles = Lists.newArrayListWithExpectedSize(games.size());
            for (GameContext c : games.values())
            {
                handles.add(c.getHandle());
            }
            return handles;
        }
    }

    /**
     * Set game state logging directory or disable logging if null.
     */
    public void setGameStateDirectory(File gameStateLogDir)
    {
        synchronized (this)
        {
            if (!gameStateLogDir.isDirectory())
            {
                if (!gameStateLogDir.mkdirs()) throw new IllegalArgumentException("Not a directory" +
                		" or cannot create directory at: " + gameStateLogDir.getAbsolutePath());
            }

            this.gameStateLogDir = gameStateLogDir;
        }
    }

    /**
     * Close the game context and all its resources.
     */
    public void close()
    {
        try
        {
            logger.debug("Shutting down auto-discovery daemon...");
            autoDiscoveryDaemon.interrupt();
            autoDiscoveryDaemon.join();
    
            for (GameContext c : games.values())
            {
                logger.debug("Shutting down running game: " + c.getHandle().gameName + " (" + 
                    c.getHandle().gameID + ").");
    
                c.dispose();
            }
        }
        catch (Exception e)
        {
            logger.debug("Shutdown sequence problem.", e);
        }
    
        logger.debug("Server context closed.");
    }
}
