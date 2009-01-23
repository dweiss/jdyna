package org.jdyna.network.sockets;

import java.io.IOException;

import org.jdyna.network.sockets.packets.FrameData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FrameDataBroadcaster implements IFrameDataListener
{
    private final static Logger logger = LoggerFactory.getLogger(FrameDataBroadcaster.class);

    private GameContext gameContext;
    private UDPPacketEmitter broadcaster;

    public FrameDataBroadcaster(GameContext gameContext, UDPPacketEmitter udpBroadcaster)
    {
        this.gameContext = gameContext;
        this.broadcaster = udpBroadcaster;
    }

    @Override
    public void onFrame(FrameData fd)
    {
        try
        {
            final Packet p = ObjectPacket.serialize(fd);
            broadcaster.send(p);
        }
        catch (IOException e)
        {
            logger.warn("Could not broadcast frame events.", e);
        }
    }
}
