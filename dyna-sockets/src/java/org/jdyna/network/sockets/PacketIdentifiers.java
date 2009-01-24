package org.jdyna.network.sockets;

import org.jdyna.network.packetio.SerializablePacket;

/**
 * Packet identifiers sent as {@link SerializablePacket#getCustom1()} fields.
 */
final class PacketIdentifiers
{
    /**
     * Packet is a frame data for a given game. The second custom field is the game
     * identifier.
     */
    public static final int GAME_FRAME_DATA = 0x00000001 << 0;

    /*
     * 
     */
    private PacketIdentifiers()
    {
        // No instances.
    }
}
