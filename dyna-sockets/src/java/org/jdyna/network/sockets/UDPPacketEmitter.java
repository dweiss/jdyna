package org.jdyna.network.sockets;

import java.io.File;
import java.io.IOException;
import java.net.*;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
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
    
    static int index = 0;

    /**
     * 
     */
    public void send(Packet packet, InetAddress target, int port) throws IOException
    {
        synchronized (this)
        {
            final byte [] buf = datagram.getData();
            buf[0] = (byte) (Packet.HEADER_MAGIC >>> 24);
            buf[1] = (byte) (Packet.HEADER_MAGIC >>> 16);
            buf[2] = (byte) (Packet.HEADER_MAGIC >>> 8);
            buf[3] = (byte) (Packet.HEADER_MAGIC);
            buf[4] = (byte) (packet.length >>> 24);
            buf[5] = (byte) (packet.length >>> 16);
            buf[6] = (byte) (packet.length >>> 8);
            buf[7] = (byte) (packet.length);
            final int header = 8;
            System.arraycopy(packet.buffer, packet.start, buf, header, packet.length);
            
            datagram.setLength(packet.length + header);
            datagram.setAddress(target);
            datagram.setPort(port);
            
            byte [] content = new byte [packet.length + header];
            System.arraycopy(buf, 0, content, 0, packet.length + header);
            FileUtils.writeByteArrayToFile(new File("packet-" + index++), content);

            if (logger.isDebugEnabled())
            {
                logger.debug("USNT: [" + (packet.length + header) + "]");
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
