package com.dawidweiss.dyna.corba;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.omg.CORBA.ORB;

import com.dawidweiss.dyna.corba.bindings.ICGameServer;
import com.dawidweiss.dyna.corba.bindings.ICGameServerHelper;
import com.dawidweiss.dyna.corba.client.GameClient;
import com.dawidweiss.dyna.corba.client.GameLauncher;
import com.dawidweiss.dyna.corba.server.GameServer;

/**
 * Development-mode launcher, starting the server and two additional players.
 */
public final class TestCorba
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

        while (true)
        {
            try
            {
                NetworkUtils.read("localhost", 50000);
                break;
            }
            catch (Exception e)
            {
                Thread.sleep(500);
            }
        }

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

        final ORB orb = ORB.init(new String [0], null);
        final ICGameServer gameServer = ICGameServerHelper
            .narrow(orb.string_to_object(new String(
                NetworkUtils.read("localhost", 50000), "UTF-8")));

        while (gameServer.players().length < 2)
        {
            Thread.sleep(500);
        }

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