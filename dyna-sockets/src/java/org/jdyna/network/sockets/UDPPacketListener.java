package org.jdyna.network.sockets;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
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

    /**
     * 
     */
    private final Packet packet;
    
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
        this.packet = new Packet();
    }

    /**
     * Receive the next packet from the network. Filter out junk.
     */
    public Packet receive() throws IOException
    {
        boolean madeSense = false;
        do
        {
            receiver.receive(udpPacket);
            if (logger.isDebugEnabled()) logger.debug("URCV: [" + udpPacket.getLength() + "]");

            final DataInputStream input = new DataInputStream(
                new ByteArrayInputStream(udpPacket.getData(), udpPacket.getOffset(), udpPacket.getLength()));

            final int magic = input.readInt();
            if (magic != Packet.HEADER_MAGIC)
            {
                logger.debug("Invalid packet header: " + Integer.toHexString(magic));
                continue;
            }

            final int length = input.readInt();
            if (length == Packet.LENGTH_MARKER_EOS)
            {
                // EOS marker.
                logger.debug("URCV: [EOS]");
                continue;
            }

            madeSense = true;
            final int header = 8;
            packet.setBody(udpPacket.getData(), udpPacket.getOffset() + header, udpPacket.getLength() - header);
        } while (!madeSense);

        return packet;
    }
}
