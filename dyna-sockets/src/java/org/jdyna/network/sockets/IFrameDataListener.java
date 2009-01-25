package org.jdyna.network.sockets;

import org.jdyna.network.sockets.packets.FrameData;

/**
 * A listener for {@link FrameData} events.
 */
interface IFrameDataListener
{
    void onFrame(FrameData fd);
}
