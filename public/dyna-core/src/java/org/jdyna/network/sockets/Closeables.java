package org.jdyna.network.sockets;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * Utilities closing anything that can be closed.s
 */
public final class Closeables
{
    /*
     * 
     */
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
    
    public static void close(Closeable c)
    {
        try
        {
            c.close();
        }
        catch (IOException e)
        {
            // Can't do much about it.
        }
    }
}
