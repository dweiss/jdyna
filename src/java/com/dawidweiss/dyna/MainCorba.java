package com.dawidweiss.dyna;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dawidweiss.dyna.corba.GameClient;
import com.dawidweiss.dyna.corba.GameLauncher;
import com.dawidweiss.dyna.corba.GameServer;

/**
 * Development-mode, starts the server and two additional players connected via Corba.
 */
public final class MainCorba
{
    /* Command-line entry point. */
    public static void main(String [] args) throws Exception
    {
        Thread t = wrap(new Callable<Object>()
        {
            public Object call() throws Exception
            {
                GameServer.main(new String []
                {
                    "-port", "50000"
                });
                return null;
            }
        });
        t.start();
        Thread.sleep(1000);

        wrap(new Callable<Object>()
        {
            public Object call() throws Exception
            {
                GameClient.main(new String []
                {
                    "-name", "p1", "-server", "localhost", "-port", "50000"
                });
                return null;
            }
        }).start();

        wrap(new Callable<Object>()
        {
            public Object call() throws Exception
            {
                GameClient.main(new String []
                {
                    "-name", "p2", "-server", "localhost", "-port", "50000"
                });
                return null;
            }
        }).start();

        Thread.sleep(2000);
        
        wrap(new Callable<Object>()
        {
            public Object call() throws Exception
            {
                GameLauncher.main(new String []
                {
                    "-server", "localhost", "-port", "50000"
                });
                return null;
            }
        }).start();

    }

    private static Thread wrap(final Callable<Object> callable)
    {
        return new Thread()
        {
            public void run()
            {
                try
                {
                    callable.call();
                }
                catch (Exception e)
                {
                    Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
                }
            }
        };
    }
}