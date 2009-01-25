package org.jdyna.network.sockets;

import org.jdyna.network.packetio.SerializablePacket;

/**
 * Packet identifiers sent as {@link SerializablePacket#getCustom1()} fields.
 */
public final class PacketIdentifiers
{
    /**
     * Packet is a frame data for a given game. The second custom field is the game
     * identifier.
     */
    public static final int GAME_FRAME_DATA = 1 << 0;
    
    /**
     * Packet indicating controller state update from a player.
     */
    public static final int PLAYER_CONTROLLER_STATE = 1 << 1;

    /*
     * 
     */
    private PacketIdentifiers()
    {
        // No instances.
    }
}
