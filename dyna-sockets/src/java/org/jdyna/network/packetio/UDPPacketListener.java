package org.jdyna.network.packetio;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Packet receiver over UDP protocol.
 */
public final class UDPPacketListener
{
    private final static Logger logger = LoggerFactory.getLogger(UDPPacketListener.class);

    /*
     * 
     */
    private final DatagramSocket receiver;

    /**
     * 
     */
    private final DatagramPacket udpPacket;

    /*
     * 
     */
    public UDPPacketListener(int port)
        throws IOException
    {
        final DatagramSocket receiver = new DatagramSocket(port);
        receiver.setBroadcast(true);
        receiver.setReuseAddress(true);
        receiver.setReceiveBufferSize(Packet.MAX_LENGTH);

        this.receiver = receiver;
        this.udpPacket = new DatagramPacket(new byte [Packet.MAX_LENGTH], Packet.MAX_LENGTH);
    }

    /**
     * Receive the next packet from the network. Filter out junk.
     */
    public <T extends Packet> T receive(T packet) throws IOException
    {
        return receive(packet, 0);
    }

    /**
     * Receive the next packet from the network. Filter out junk.
     */
    public <T extends Packet> T receive(T packet, int timeout) throws IOException
    {
        do
        {
            try
            {
                receiver.setSoTimeout(timeout);
                receiver.receive(udpPacket);
            }
            catch (SocketTimeoutException e)
            {
                return null;
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("URCV: [" + udpPacket.getLength() + "]");
            }

            final DataInputStream input = new DataInputStream(
                new ByteArrayInputStream(udpPacket.getData(), udpPacket.getOffset(), udpPacket.getLength()));

            try
            {
                packet.read(input);
                packet.source = udpPacket.getAddress();

                return packet;
            }
            catch (StreamCorruptedException e)
            {
                logger.debug("Not a packet on input.");
            }
        } while (true);
    }

    /*
     * 
     */
    public void close()
    {
        if (!receiver.isClosed())
        {
            this.receiver.close();
        }
    }
}
