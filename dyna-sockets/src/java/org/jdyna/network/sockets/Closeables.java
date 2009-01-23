package org.jdyna.network.sockets;

import java.io.IOException;
import java.net.ServerSocket;

public final class Closeables
{
    private Closeables()
    {
        // no instances.
    }

    /*
     * 
     */
    public static void close(ServerSocket socket)
    {
        try
        {
            if (socket != null) socket.close();
        }
        catch (IOException e)
        {
            // Can't do much about it.
        }
    }
}
