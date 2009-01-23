package org.jdyna.network.sockets;

import java.io.IOException;
import java.net.*;

import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.CmdLine;


/**
 * A socket server for jdyna. Broadcasts itself on the local network automatically.
 */
public final class GameServer
{
    /**
     * Default server port (TCP connections).
     */
    public final static int DEFAULT_PORT = 50000;

    /**
     * Default UDP broadcast port.
     */
    public final static int DEFAULT_UDP_BROADCAST = 50001;

    /**
     * Internal logger.
     */
    private final static Logger logger = LoggerFactory.getLogger(GameServer.class);
    
    /**
     * Binding port for the TCP connection server.
     */
    @Option(name = "-p", aliases = "--port", required = false, metaVar = "port", 
        usage = "Binding TCP port (default: " + DEFAULT_PORT + ").")
    public int port = 50000;

    /**
     * Binding network interface for the TCP connection server.
     */
    @Option(name = "-i", aliases = "--interface", 
        required = false, metaVar = "address", 
        usage = "Binding TCP interface (default: all interfaces).")
    public String iface = "0.0.0.0";

    /**
     * Shared context object (all games, their progress, etc.).
     */
    private GameServerContext context;

    /*
     * 
     */
    protected GameServer()
    {
        // Empty, for command-line use.
    }

    /**
     * Bring up the network presence.
     */
    public void start()
    {
        try
        {
            logger.info("Server initializing...");

            final ServerSocket socket = new ServerSocket(port, 0, InetAddress.getByName(iface));
            logger.info("TCP listener bound to: " + socket.getInetAddress());

            this.context = new GameServerContext();

            Socket client;
            while ((client = socket.accept()) != null)
            {
                /*
                 * Start a new thread to converse with the client.
                 */
                new ServerToClientControlConnection(client, context).start();
            }
        }
        catch (IOException e)
        {
            logger.error("Network or I/O error: " + e.getMessage(), e);
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
