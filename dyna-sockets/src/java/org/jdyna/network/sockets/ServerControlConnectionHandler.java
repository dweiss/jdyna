package org.jdyna.network.sockets;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;

import org.jdyna.network.sockets.packets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCP packet exchange connection, server side.
 */
final class ServerControlConnectionHandler extends Thread
{
    /*
     * 
     */
    private final Logger logger;

    /*
     * 
     */
    private final Socket client;

    /**
     * Shared game server context.
     */
    private GameServerContext context;

    /**
     * IP address of the client.
     */
    private String clientAddress;

    /**
     * Packet emitter.
     */
    private TCPPacketEmitter pe;

    /*
     * 
     */
    public ServerControlConnectionHandler(Socket client, GameServerContext context)
    {
        this.client = client;
        this.context = context;

        this.clientAddress = client.getInetAddress().getHostAddress();
        logger = LoggerFactory.getLogger(ServerControlConnectionHandler.class.getName()
            + "#" + clientAddress);

        setName("Client-" + clientAddress);
        setDaemon(true);
    }

    /*
     * 
     */
    @Override
    public void run()
    {
        logger.info("Link started.");

        try
        {
            pe = new TCPPacketEmitter(client);

            /*
             * Exchange control packets with the client.
             */
            Packet p;
            while ((p = pe.receive()) != null)
            {
                final Object o = ObjectPacket.deserialize(p);

                try
                {
                    if (o instanceof FailureResponse)
                    {
                        logger.warn("Failure response from the client: "
                            + ((FailureResponse) o).toString());
                    }
                    else if (o instanceof CreateGameRequest)
                    {
                        final CreateGameRequest req = (CreateGameRequest) o;
                        handleRequest(req);
                    }
                    else if (o instanceof JoinGameRequest)
                    {
                        final JoinGameRequest req = (JoinGameRequest) o;
                        handleRequest(req);
                    }
                    else
                    {
                        logger.warn("Unrecognized packet: " + o.getClass().getSimpleName());
                    }
                }
                catch (FailureResponseException e)
                {
                    logger.warn("Failure response to client: " + e.message);
                    send(new FailureResponse(e.message));
                }
            }
        }
        catch (IOException e)
        {
            logger.error("Packet exchange failure.", e);
        }
        finally
        {
            if (pe != null) pe.close();
            pe = null;
        }

        logger.info("Link closed.");
    }

    /*
     * 
     */
    private void handleRequest(JoinGameRequest req) throws IOException
    {
        final InetAddress remote = this.client.getInetAddress();
        final String ip = remote.getHostAddress();

        if (!context.hasGame(req.gameID))
        {
            throw new FailureResponseException("Game does not exists: " + req.gameID);
        }

        /*
         * Check if this player is already registered in this game
         * (reconnecting).
         */
        PlayerHandle player = context.getGameContext(req.gameID)
            .getOrCreatePlayer(ip, req.playerName);

        send(new JoinGameResponse(player));
    }

    /*
     * 
     */
    private void handleRequest(CreateGameRequest req)
        throws IOException
    {
        if (context.hasGame(req.gameName))
        {
            throw new FailureResponseException("Game already exists: " + req.gameName);
        }

        if (!context.hasBoard(req.boardName))
        {
            throw new FailureResponseException("Board does not exist: " + req.boardName);
        }

        final GameHandle handle = context.createNewGame(req.gameName, req.boardName);
        send(new CreateGameResponse(handle));
    }

    /*
     * 
     */
    private void send(Serializable object) throws IOException
    {
        pe.send(ObjectPacket.serialize(object));        
    }
}
