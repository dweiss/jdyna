package org.jdyna.network.sockets;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdyna.GameConfiguration;
import org.jdyna.network.packetio.SerializablePacket;
import org.jdyna.network.packetio.TCPPacketEmitter;
import org.jdyna.network.packetio.UDPPacketListener;
import org.jdyna.network.sockets.packets.CreateGameRequest;
import org.jdyna.network.sockets.packets.CreateGameResponse;
import org.jdyna.network.sockets.packets.FailureResponse;
import org.jdyna.network.sockets.packets.JoinGameRequest;
import org.jdyna.network.sockets.packets.JoinGameResponse;
import org.jdyna.network.sockets.packets.ListGamesRequest;
import org.jdyna.network.sockets.packets.ListGamesResponse;
import org.jdyna.network.sockets.packets.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A set of utilities facilitating talking to a remote {@link GameServer} and running a
 * game.
 */
public class GameServerClient
{
    /**
     * Internal logger.
     */
    private final static Logger logger = LoggerFactory.getLogger(GameServerClient.class);

    /**
     * Port for the TCP server connection.
     */
    private int serverTCPControlPort;

    /**
     * Host for the server's TCP connection.
     */
    private String serverAddress;

    /**
     * Server connection.
     */
    private TCPPacketEmitter pe;

    /**
     * Reusable packet.
     */
    final SerializablePacket packet = new SerializablePacket();

    /**
     * @see #lookup(int, int, int) 
     */
    public GameServerClient(ServerInfo info)
    {
        this(info.serverAddress, info.TCPControlPort);
    }

    /**
     * Create a game client connecting to a given server.
     */
    public GameServerClient(String serverAddress, int controlPort)
    {
        this.serverAddress = serverAddress;
        this.serverTCPControlPort = controlPort;
    }

    /**
     * Lookup available servers broadcasting on the local network.
     */
    public static List<ServerInfo> lookup(int udpBroadcastPort, int minServers, int timeout) throws IOException
    {
        final HashMap<String, ServerInfo> servers = Maps.newHashMap();
        final long deadline = System.currentTimeMillis() + timeout;
        final UDPPacketListener listener = new UDPPacketListener(udpBroadcastPort);
        SerializablePacket packet = new SerializablePacket();

        if (minServers <= 0) minServers = Integer.MAX_VALUE;

        while (minServers > 0)
        {
            final int delay = (int) (deadline - System.currentTimeMillis());
            if (delay <= 0 || (packet = listener.receive(packet, delay)) == null)
            {
                break;
            }

            if (packet.getCustom1() == PacketIdentifiers.SERVER_BEACON)
            {
                final ServerInfo si = packet.deserialize(ServerInfo.class);
                si.serverAddress = packet.getSource().getHostAddress();
                if (!servers.containsKey(si.serverAddress))
                {
                    minServers--;
                    servers.put(si.serverAddress, si);
                    logger.info("Discovered server: " + si);
                }
            }
        }

        listener.close();
        return Lists.newArrayList(servers.values());
    }

    /**
     * Establish a control link to a remote server.
     */
    public void connect() throws IOException
    {
        if (pe != null) throw new IllegalStateException("Already connected.");
        if (StringUtils.isEmpty(serverAddress)) throw new IllegalStateException(
            "host is required.");

        pe = new TCPPacketEmitter(new Socket(InetAddress.getByName(serverAddress),
            serverTCPControlPort));
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
    public GameHandle createGame(GameConfiguration conf, String gameName, String boardName) throws IOException
    {
        checkConnected();

        final CreateGameResponse response = sendReceive(CreateGameResponse.class,
            new CreateGameRequest(conf, gameName, boardName));
        return response.handle;
    }

    /**
     * Join or re-join an existing game. Re-joining will happen if player with the given
     * name and the same source IP address was already registered.
     */
    public PlayerHandle joinGame(GameHandle gameHandle, String playerName)
        throws IOException
    {
        checkConnected();

        final JoinGameResponse response = sendReceive(JoinGameResponse.class,
            new JoinGameRequest(gameHandle.gameID, playerName));
        return response.handle;
    }

    /**
     * Get an existing game handle or <code>null</code> if it does not exist. 
     */
    public GameHandle getGame(String gameName) throws IOException
    {
        checkConnected();
        
        final List<GameHandle> handles = listGames();
        for (GameHandle handle : handles)
        {
            if (StringUtils.equals(handle.gameName, gameName))
            {
                return handle;
            }
        }

        return null;
    }

    /**
     * List all games available on the server.
     */
    public List<GameHandle> listGames() throws IOException
    {
        final ListGamesResponse response = sendReceive(ListGamesResponse.class,
            new ListGamesRequest());

        return response.handles;
    }

    /**
     * Send a given object, receive another object and check if the received packet is of
     * the required type.
     */
    private <T> T sendReceive(Class<T> clazz, Serializable object) throws IOException
    {
        logger.debug("Sending: " + object.getClass().getSimpleName());

        packet.serialize(0, 0, object);
        pe.send(packet);

        if (pe.receive(packet) == null)
        {
            throw new IOException("Server connection closed.");
        }

        final Object result = packet.deserialize(Object.class);
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

    /**
     * Check if there is a valid control link.
     */
    private void checkConnected()
    {
        if (pe == null)
        {
            throw new IllegalArgumentException("Not connected.");
        }
    }
}
