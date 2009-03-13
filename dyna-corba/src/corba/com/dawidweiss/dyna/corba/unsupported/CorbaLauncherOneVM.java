package com.dawidweiss.dyna.corba.unsupported;

import java.util.concurrent.Callable;

import org.omg.CORBA.ORB;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.corba.*;
import com.dawidweiss.dyna.corba.bindings.ICGameServer;
import com.dawidweiss.dyna.corba.bindings.ICGameServerHelper;

/**
 * Development-mode launcher, starting the server and two additional players.
 */
public final class CorbaLauncherOneVM
{
    /* Command-line entry point. */
    public static void main(String [] args) throws Exception
    {
        Thread t = wrap(new Callable<Object>()
        {
            public Object call() throws Exception
            {
                GameServerLauncher.main(new String []
                {
                    "--host", "localhost", "--port", "50000"
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
                GameClientLauncher.main(new String []
                {
                    "--name", "p1", "--host", "localhost", "--port", "50000"
                });
                return null;
            }
        }).start();

        wrap(new Callable<Object>()
        {
            public Object call() throws Exception
            {
                GameClientLauncher.main(new String []
                {
                    "--name", "p2", "--host", "localhost", "--port", "50000"
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
                    "--host", "localhost", "--port", "50000"
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
                    LoggerFactory.getLogger("anonymous").error(e.getMessage(), e);
                }
            }
        };
    }
}