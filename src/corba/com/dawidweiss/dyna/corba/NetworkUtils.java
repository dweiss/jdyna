package com.dawidweiss.dyna.corba;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Simple socket utilities to expose the initial IOR. We don't need naming service for
 * this.
 */
public class NetworkUtils
{
    public static void expose(final String ior, int port) throws IOException
    {
        final ServerSocket socket = new ServerSocket(port);
        final Thread t = new Thread("IOR provider")
        {
            public void run()
            {
                try
                {
                    Socket client = null;
                    while ((client = socket.accept()) != null)
                    {
                        client.getOutputStream().write(ior.getBytes("UTF-8"));
                        client.close();
                    }
                }
                catch (IOException e)
                {
                    Logger.getAnonymousLogger()
                        .severe("I/O problem when publishing IOR.");
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    /*
     * 
     */
    public static byte [] read(String host, int port) throws IOException
    {
        final Socket socket = new Socket(host, port);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final InputStream is = socket.getInputStream();
        final byte [] buffer = new byte [1024];
        int cnt;
        while ((cnt = is.read(buffer)) >= 0)
        {
            baos.write(buffer, 0, cnt);
        }
        socket.close();
        return baos.toByteArray();
    }
}
