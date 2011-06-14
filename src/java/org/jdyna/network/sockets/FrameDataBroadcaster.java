package org.jdyna.network.sockets;

import java.io.IOException;

import org.jdyna.network.packetio.SerializablePacket;
import org.jdyna.network.packetio.UDPPacketEmitter;
import org.jdyna.network.sockets.packets.FrameData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Broadcasts data received from {@link IFrameDataListener} to a {@link Packet} sent using
 * {@link UDPPacketEmitter}.
 */
final class FrameDataBroadcaster implements IFrameDataListener
{
    private final static Logger logger = LoggerFactory
        .getLogger(FrameDataBroadcaster.class);

    private final SerializablePacket packet = new SerializablePacket();
    private final GameContext gameContext;
    private final UDPPacketEmitter broadcaster;

    /*
     * 
     */
    FrameDataBroadcaster(GameContext gameContext, UDPPacketEmitter udpBroadcaster)
    {
        this.gameContext = gameContext;
        this.broadcaster = udpBroadcaster;
    }

    /**
     * Broadcast frame data to clients.
     */
    @Override
    public void onFrame(FrameData fd)
    {
        try
        {
            packet.serialize(PacketIdentifiers.GAME_FRAME_DATA,
                gameContext.getHandle().gameID, fd);
            broadcaster.send(packet);
        }
        catch (IOException e)
        {
            logger.warn("Could not broadcast frame events.", e);
        }
    }
}
