package org.jdyna.network.sockets;

import org.jdyna.network.sockets.packets.FrameData;

interface IFrameDataListener
{

    void onFrame(FrameData fd);

}
