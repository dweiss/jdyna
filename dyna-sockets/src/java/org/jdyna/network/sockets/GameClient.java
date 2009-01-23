package org.jdyna.network.sockets;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;

import org.jdyna.network.sockets.packets.*;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class GameClient
{
    /**
     * Internal logger.
     */
    private final static Logger logger = LoggerFactory.getLogger(GameClient.class);

    /**
     * Port for the TCP server connection.
     */
    @Option(name = "-p", aliases = "--port", required = false, metaVar = "port", usage = "Server's TCP port (default: "
        + GameServer.DEFAULT_PORT + ").")
    public int port = GameServer.DEFAULT_PORT;

    /**
     * Host for the server's TCP connection.
     */
    @Option(name = "-s", aliases = "--server", required = true, metaVar = "address", usage = "Server's host address.")
    public String host;

    /**
     * Server connection.
     */
    private TCPPacketEmitter pe;

    /**
     * Connect to the server.
     */
    public void connect() throws IOException
    {
        if (pe != null) throw new IllegalStateException("Already connected.");

        pe = new TCPPacketEmitter(new Socket(InetAddress.getByName(host), port));
        logger.info("Connected.");
    }

    /**
     * 
     */
    public void disconnect()
    {
        pe.close();
        pe = null;
    }

    /**
     * Create a new game on the server.
     */
    public GameHandle createGame(String gameName, String boardName) throws IOException
    {
        CreateGameResponse response = 
            sendReceive(CreateGameResponse.class, new CreateGameRequest(gameName, boardName));
        return response.handle;
    }

    /**
     * Check if the received packet is of the required type.
     */
    private <T> T sendReceive(Class<T> clazz, Serializable packet) throws IOException
    {
        logger.debug("Sending: " + packet.getClass().getSimpleName());

        final Packet p = pe.sendAndReceive(ObjectPacket.serialize(packet));
        if (p == null)
        {
            throw new IOException("Server connection closed.");
        }
        final Object result = ObjectPacket.deserialize(p);

        logger.debug("Received: " + result.getClass().getSimpleName());

        if (result instanceof FailureResponse)
        {
            final FailureResponse response = (FailureResponse) result;

            throw new IOException("Server indicates failure: " + response.message,
                response.throwable);
        }

        if (clazz.isAssignableFrom(result.getClass()))
        {
            return clazz.cast(result);
        }

        throw new IOException("Unexpected packet content received: "
            + result.getClass().getSimpleName() + " (expected: " + clazz.getSimpleName()
            + ")");
    }
}
