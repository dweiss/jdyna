package org.jdyna.network.packetio;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Packet sender over UDP protocol.
 */
public final class UDPPacketEmitter
{
    private final static Logger logger = LoggerFactory.getLogger(UDPPacketEmitter.class);

    /*
     * 
     */
    private final DatagramSocket socket;

    /**
     * 
     */
    private final DatagramPacket datagram;

    /**
     * Default target.
     */
    private InetAddress defaultTarget;
    private int defaultPort;

    /*
     * 
     */
    public UDPPacketEmitter(DatagramSocket socket) throws IOException
    {
        this.socket = socket;
        socket.setReuseAddress(true);
        socket.setReceiveBufferSize(Packet.MAX_LENGTH);

        this.datagram = new DatagramPacket(new byte [Packet.MAX_LENGTH],
            Packet.MAX_LENGTH);
    }

    /**
     * Send to the default address and port.
     */
    public void send(Packet packet) throws IOException
    {
        send(packet, defaultTarget, defaultPort);
    }
    
    /**
     * 
     */
    public void send(Packet packet, InetAddress target, int port) throws IOException
    {
        synchronized (this)
        {
            final byte [] buf = packet.getSendBuffer();
            final int length = packet.getLength();
            final int start = packet.getStart();

            datagram.setData(buf, start, length);
            datagram.setAddress(target);
            datagram.setPort(port);

            if (logger.isDebugEnabled())
            {
                logger.debug("USNT: [" + (length) + "]");
            }

            socket.send(datagram);
        }
    }

    public void close()
    {
        if (!socket.isClosed())
        {
            this.socket.close();
        }
    }

    /*
     * 
     */
    public void setDefaultTarget(InetAddress address, int port)
    {
        assert port > 0;
        assert address != null;

        this.defaultTarget = address;
        this.defaultPort = port;
    }
}
