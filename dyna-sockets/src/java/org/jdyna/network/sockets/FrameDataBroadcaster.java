package org.jdyna.network.sockets;

import java.io.*;

import org.jdyna.network.sockets.packets.FrameData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FrameDataBroadcaster implements IFrameDataListener
{
    private final static Logger logger = LoggerFactory.getLogger(FrameDataBroadcaster.class);

    private GameContext gameContext;
    private UDPBroadcaster broadcaster;

    public FrameDataBroadcaster(GameContext gameContext, UDPBroadcaster udpBroadcaster)
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
            
            final int header = 8;
            final byte [] buf = new byte [p.length + header];

            buf[0] = (byte) (Packet.HEADER_MAGIC >>> 24);
            buf[1] = (byte) (Packet.HEADER_MAGIC >>> 16);
            buf[2] = (byte) (Packet.HEADER_MAGIC >>> 8);
            buf[3] = (byte) (Packet.HEADER_MAGIC);
            buf[4] = (byte) (p.length >>> 24);
            buf[5] = (byte) (p.length >>> 16);
            buf[6] = (byte) (p.length >>> 8);
            buf[7] = (byte) (p.length);
            System.arraycopy(p.buffer, p.start, buf, header, p.length);

            broadcaster.send(buf, 0, buf.length);
        }
        catch (IOException e)
        {
            logger.warn("Could not broadcast frame events.", e);
        }
    }
}
