package org.jdyna.network.sockets;

import java.io.IOException;
import java.net.*;

/**
 * 
 */
class UDPBroadcaster
{
    private final DatagramSocket socket;
    private final DatagramPacket p;

    /*
     * 
     */
    public UDPBroadcaster(int broadcastPort) throws IOException
    {
        this.socket = new DatagramSocket();
        socket.setBroadcast(true);
        socket.setReuseAddress(true);
        socket.setSendBufferSize(Packet.MAX_LENGTH);

        p = new DatagramPacket(new byte [Packet.MAX_LENGTH], Packet.MAX_LENGTH);
        p.setAddress(Inet4Address.getByName("255.255.255.255"));
        p.setPort(broadcastPort);
    }

    /*
     * 
     */
    public void send(byte [] buffer, int start, int length)
        throws IOException
    {
        /**
         * All games broadcast on the same UDP port.
         */
        synchronized (this)
        {
            this.p.setData(buffer, start, length);
            socket.send(this.p);
        }
    }
    
    /*
     * 
     */
    public void close()
    {
        this.socket.close();
    }
}
