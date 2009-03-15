package org.jdyna.network.sockets;

import java.io.File;
import java.io.IOException;
import java.net.*;

import org.apache.commons.lang.ObjectUtils;
import org.jdyna.CmdLine;
import org.jdyna.network.packetio.SerializablePacket;
import org.jdyna.network.packetio.UDPPacketListener;
import org.jdyna.network.sockets.packets.ServerInfo;
import org.jdyna.network.sockets.packets.UpdateControllerState;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A socket server for jdyna. Broadcasts itself on the local network for automatic
 * detection by clients. Uses TCP, UDP and broadcast UDP connections. At least three ports
 * must be open in the local network.
 */
public final class GameServer
{
    /**
     * Default UDP broadcast port (for auto-discovery as well).
     */
    public final static int DEFAULT_UDP_BROADCAST = 50001;

    /**
     * Default server port (TCP connections).
     */
    private final static int DEFAULT_TCP_CONTROL_PORT = 50000;

    /**
     * Default feedback port (UDP).
     */
    private final static int DEFAULT_UDP_FEEDBACK_PORT = 50000;

    /**
     * Internal logger.
     */
    private final static Logger logger = LoggerFactory.getLogger(GameServer.class);

    /**
     * Binding port for the TCP connection server.
     */
    @Option(name = "-cp", aliases = "--control-port", required = false, metaVar = "ports", usage = "TCP control port (default: "
        + DEFAULT_TCP_CONTROL_PORT + ").")
    public int TCPport = DEFAULT_TCP_CONTROL_PORT;

    /**
     * Binding port for the asynchronous UDP feedback packets.
     */
    @Option(name = "-fp", aliases = "--feedback-port", required = false, metaVar = "port", usage = "UDP feedback port (default: "
        + DEFAULT_UDP_FEEDBACK_PORT + ").")
    public int UDPport = DEFAULT_UDP_FEEDBACK_PORT;

    /**
     * Broadcast port for distributing game events.
     */
    @Option(name = "-bp", aliases = "--broadcast-port", required = false, metaVar = "port", usage = "UDP broadcast port (default: "
        + DEFAULT_UDP_BROADCAST + ").")
    public int UDPBroadcastPort = DEFAULT_UDP_BROADCAST;

    /**
     * Binding network interface for the TCP control and UDP feedback ports.
     */
    @Option(name = "-i", aliases = "--interface", required = false, metaVar = "address", usage = "Binding TCP interface (default: all interfaces).")
    public String iface = "0.0.0.0";

    /**
     * Enable game state logging for replays later on.
     */
    @Option(name = "-lg", aliases = "--log-games", required = false, usage = "Enable game state logging.")
    public boolean gameStateLogging;

    /**
     * Game state logging directory, if enabled.
     */
    @Option(name = "-ld", aliases = "--log-dir", required = false, usage = "Game state logging directory.")
    public File gameStateLogDir = new File("gamelogs");

    /**
     * Broadcast port for distributing game events.
     */
    @Option(name = "-mg", aliases = "--max-games", required = false, metaVar = "int", 
        usage = "Maximum number of concurrent games.")
    public int maxGames = 1;

    /**
     * Shared context object (all games, their progress, etc.).
     */
    private GameServerContext context;

    /**
     * Feedback UDP socket.
     */
    private UDPPacketListener feedbackSocket;

    /**
     * A thread accepting feedback UDP packets from clients and updating controller
     * states.
     */
    private Thread feedbackSocketListener = new Thread()
    {
        public void run()
        {
            try
            {
                final SerializablePacket p = new SerializablePacket();
                while (true)
                {
                    if (feedbackSocket.receive(p) == null)
                    {
                        break;
                    }

                    if (p.getCustom1() != PacketIdentifiers.PLAYER_CONTROLLER_STATE)
                    {
                        logger.warn("Junk packet received on feedback port: "
                            + p);
                        continue;
                    }

                    handle(p);
                }
            }
            catch (IOException e)
            {
                logger.error("Error on feedback socket.", e);
            }
        }
    };

    /*
     * 
     */
    protected GameServer()
    {
        // Empty, for command-line use.
    }

    /**
     * Handle a controller update message.
     * 
     * @param inetAddress
     */
    protected void handle(SerializablePacket p) throws IOException
    {
        final GameContext gameContext = context.getGameContext(p.getCustom2());
        if (gameContext == null)
        {
            logger.warn("Packet received for a non-existing game: " + p.getCustom2());
            return;
        }

        final UpdateControllerState s = p.deserialize(UpdateControllerState.class);

        if (p.getSource() == null
            || !ObjectUtils.equals(gameContext.getPlayerAddress(s.playerID), p
                .getSource().getHostAddress()))
        {
            logger.warn("Controller update received from a different address "
                + "than the player registered from: [" + p.getSource() + "]");
            return;
        }

        gameContext.updateControllerState(s.playerID, s.state);
    }

    /**
     * Bring up the network presence.
     */
    public void start()
    {
        ServerSocket socket = null;
        try
        {
            logger.info("Server initializing...");

            final InetAddress serverAddress = InetAddress.getByName(iface);
            socket = new ServerSocket(TCPport, 0, serverAddress);
            logger.info("TCP listener bound to: " + socket.getInetAddress());

            final ServerInfo serverInfo = new ServerInfo(
                serverAddress.getHostAddress(), TCPport, UDPBroadcastPort, UDPport);

            this.context = new GameServerContext(serverInfo, maxGames);
            if (gameStateLogging)
            {
                context.setGameStateDirectory(gameStateLogDir);
            }

            /*
             * Start UDP socket listener.
             */
            feedbackSocket = new UDPPacketListener(UDPport);
            feedbackSocketListener.start();

            /*
             * Add a shutdown hook that closes the TCP socket.
             */
            final ServerSocket socketf = socket;
            final Thread serverThread = Thread.currentThread();
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                public void run()
                {
                    try
                    {
                        Closeables.close(socketf);
                        serverThread.join();
                        logger.info("Shutdown hook finished.");
                    }
                    catch (InterruptedException e)
                    {
                        // Ignore.
                    }
                }
            });

            /*
             * Start TCP control socket listener.
             */
            try
            {
                Socket client;
                while ((client = socket.accept()) != null)
                {
                    /*
                     * Start a new thread to converse with this client.
                     */
                    new ServerControlConnectionHandler(client, context).start();
                }
            }
            catch (SocketException e)
            {
                // We assume this is socket closed.
            }
        }
        catch (IOException e)
        {
            logger.error("Network or I/O error: " + e.getMessage(), e);
        }
        finally
        {
            Closeables.close(socket);
            feedbackSocket.close();
            context.close();
        }

        logger.info("Server stopped.");
    }

    /**
     * Command line entry point.
     */
    public static void main(String [] args)
    {
        final GameServer me = new GameServer();
        if (CmdLine.parseArgs(me, args))
        {
            me.start();
        }
    }
}
