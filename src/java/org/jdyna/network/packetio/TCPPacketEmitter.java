package org.jdyna.network.packetio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple wrapper to accept and emit packets on a TCP connection. Always close when
 * done.
 */
public final class TCPPacketEmitter
{
    private final static Logger logger = LoggerFactory.getLogger(TCPPacketEmitter.class);

    /**
     * Server socket to receive/send packets.
     */
    private final Socket socket;

    /** Input stream from the socket. */
    private final DataInputStream input;

    /** Output stream to the socket. */
    private final DataOutputStream output;

    /**
     * Open the server socket required for accepting connections.
     */
    public TCPPacketEmitter(Socket socket) throws IOException
    {
        this.socket = socket;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
    }

    /**
     * Accept the next packet and return it. <code>null</code> is returned on EOF.
     */
    public <T extends Packet> T receive(T packet) throws IOException
    {
        try
        {
            packet.read(input);
            packet.source = this.socket.getInetAddress();

            if (logger.isDebugEnabled()) logger.debug("TRCV: [" + packet.getLength() + "]");
            return packet;
        }
        catch (EOFException e)
        {
            return null;
        }
    }

    /**
     * Send a packet to the remote side.
     */
    public void send(Packet packet) throws IOException
    {
        final byte [] buffer = packet.getSendBuffer();
        final int length = packet.getLength();
        final int start = packet.getStart();

        if (length == 0)
        {
            throw new IOException("A packet must have a non-empty buffer.");
        }

        output.write(buffer, start, length);
        output.flush();

        if (logger.isDebugEnabled()) logger.debug("TSNT: [" + length + "]");
    }

    /**
     * Close the allocated server socket.
     */
    public void close()
    {
        if (!socket.isClosed())
        {
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                // We can't do anything.
            }
        }
    }
}
