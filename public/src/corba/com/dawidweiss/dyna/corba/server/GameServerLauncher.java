package com.dawidweiss.dyna.corba.server;

import java.io.PrintStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.omg.PortableServer.POA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.corba.CorbaUtils;
import com.dawidweiss.dyna.corba.NetworkUtils;
import com.dawidweiss.dyna.corba.bindings.ICGameServer;
import com.dawidweiss.dyna.corba.bindings.ICGameServerHelper;

/**
 * Starts the game server and saves its initial reference to a file.
 */
public class GameServerLauncher
{
    private final static Logger logger = LoggerFactory.getLogger("server.launcher");

    @Option(name = "-p", aliases = "--port",
        required = true, metaVar = "port", usage = "Server IOR bind port.")
    protected int port;

    @Option(name = "-h", aliases = "--host", 
        required = true, metaVar = "address", usage = "Server IOR bind interface.")
    protected String host;

    @Option(name = "--iiop.host", 
        required = false, metaVar = "address", usage = "IIOP bind interface.")
    protected String iiop_host;

    @Option(name = "--iiop.port", 
        required = false, metaVar = "port", usage = "IIOP bind port.")
    protected int iiop_port;

   /*
     * Console entry point.
     */
    public void start() throws Exception
    {
        /*
         * ORB setup.
         */
        final org.omg.CORBA.ORB orb = CorbaUtils.initORB(iiop_host, iiop_port);
        final POA rootPOA = CorbaUtils.rootPOA(orb);

        /*
         * Create game manager, save its reference.
         */
        final GameServerServant servant = new GameServerServant();
        final ICGameServer gameServer = 
            ICGameServerHelper.narrow(rootPOA.servant_to_reference(servant));

        final String ior = orb.object_to_string(gameServer);
        NetworkUtils.expose(ior, host, port);

        /*
         * Start the ORB and run it in an infinite loop.
         */
        logger.info("Server started and ready.");
        orb.run();
    }
    
    /*
     * Console entry point.
     */
    public static void main(String [] args) throws Exception
    {
        final GameServerLauncher launcher = new GameServerLauncher();
        final CmdLineParser parser = new CmdLineParser(launcher);
        parser.setUsageWidth(80);

        try
        {
            parser.parseArgument(args);
        }
        catch (CmdLineException e)
        {
            PrintStream ps = System.out;
            ps.print("Usage: ");
            parser.printSingleLineUsage(ps);
            ps.println();
            parser.printUsage(ps);

            ps.println("\n" + e.getMessage());
            return;
        }

        launcher.start();
    }    
}