package org.jdyna.network.sockets;

import java.io.*;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple wrapper to accept and emit packets on a TCP connection. Always close when done.
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

    /** Internal reusable buffer for packet bodies. */
    private final byte [] buffer;
    
    /** Internal reusable packet. */
    private final Packet packet = new Packet();
    
    /**
     * Open the server socket required for accepting connections.
     */
    public TCPPacketEmitter(Socket socket) throws IOException
    {
        this.socket = socket;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
        this.buffer = new byte [Packet.MAX_LENGTH];
    }

    /**
     * Accept the next packet.
     */
    public Packet receive() throws IOException
    {
        final int magic;
        try
        {
            magic = input.readInt();
            if (magic != Packet.HEADER_MAGIC)
            {
                throw new IOException("Invalid packet header: " + Integer.toHexString(magic));
            }
        }
        catch (EOFException e)
        {
            return null;
        }

        final int length = input.readInt();
        if (length == Packet.LENGTH_MARKER_EOS)
        {
            // EOS marker.
            if (logger.isDebugEnabled()) logger.debug("TRCV: [EOS]");
            return null;
        }

        if (length > Packet.MAX_LENGTH)
        {
            throw new IOException("Maximum packet length (" + Packet.MAX_LENGTH 
                + " exceeded: " + length);
        }

        input.readFully(buffer, 0, length);
        packet.setBody(buffer, 0, length);

        if (logger.isDebugEnabled()) logger.debug("TRCV: [" + length + "]");

        return packet;
    }

    /**
     * Send a packet to the remote side.
     */
    public void send(Packet packet) throws IOException
    {
        if (packet.buffer == null || packet.length == 0)
        {
            throw new IOException("A packet must have a non-empty buffer.");
        }

        output.writeInt(Packet.HEADER_MAGIC);
        output.writeInt(packet.length);
        output.write(packet.buffer, packet.start, packet.length);

        if (logger.isDebugEnabled()) logger.debug("TSNT: [" + packet.length + "]");
    }

    /**
     * Send an EOS marker.
     */
    public void sendEOS() throws IOException
    {
        output.writeInt(Packet.HEADER_MAGIC);
        output.writeInt(Packet.LENGTH_MARKER_EOS);

        if (logger.isDebugEnabled()) logger.debug("TSNT: [EOS]");
    }

    /**
     * Send a packet to the remote side and wait for reply.
     */
    public Packet sendAndReceive(Packet packet) throws IOException
    {
        send(packet);
        return receive();
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
