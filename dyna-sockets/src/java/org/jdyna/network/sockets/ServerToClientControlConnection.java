package org.jdyna.network.sockets;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

import org.jdyna.network.sockets.packets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCP packet exchange connection, server side.
 */
final class ServerToClientControlConnection extends Thread
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
    public ServerToClientControlConnection(Socket client, GameServerContext context)
    {
        this.client = client;
        this.context = context;

        this.clientAddress = client.getInetAddress().getHostAddress();
        logger = LoggerFactory.getLogger(ServerToClientControlConnection.class.getName()
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

                if (o instanceof FailureResponse)
                {
                    logger.warn("Failure response from the client: "
                        + ((FailureResponse) o).toString());
                }
                else if (o instanceof CreateGameRequest)
                {
                    final CreateGameRequest req = (CreateGameRequest) o;
                    handleGameRequest(pe, req);
                }
                else
                {
                    logger.warn("Unrecognized packet: " + o.getClass().getSimpleName());
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
    private void handleGameRequest(TCPPacketEmitter pe, CreateGameRequest req)
        throws IOException
    {
        if (context.hasGame(req.gameName))
        {
            send(new FailureResponse("Game already exists: " + req.gameName));
            return;
        }

        if (!context.hasBoard(req.boardName))
        {
            send(new FailureResponse("Board does not exist: " + req.boardName));
            return;
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
