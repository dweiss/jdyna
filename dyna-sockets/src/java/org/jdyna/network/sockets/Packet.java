package org.jdyna.network.sockets;

/**
 * A packet is a unit of information exchanged by parties. A packet is usually reusable, so its
 * body will change when new packets arrive from the network. Copy if needed. 
 */
class Packet
{
    /**
     * Packet header magic on the network.
     */
    public static final int HEADER_MAGIC = ('d' << 24) | ('y' << 16) | ('n' << 8) | ('a');

    /**
     * Maximum packet length. Must fit in an UDP datagram as well.
     */
    public static final int MAX_LENGTH = 63000;

    /**
     * A length field may have a value of -1 to indicate end of stream.
     */
    public static final int LENGTH_MARKER_EOS = 0xffffffff;

    byte [] buffer;
    int start;
    int length;

    /**
     * 
     */
    Packet(byte [] buffer, int start, int length)
    {
        setBody(buffer, start, length);
    }

    /**
     * 
     */
    Packet()
    {
        // Do nothing.
    }

    /**
     * Update the body of this packet. 
     */
    void setBody(byte [] buffer, int start, int length)
    {
        this.buffer = buffer;
        this.start = start;
        this.length = length;        
    }
}
