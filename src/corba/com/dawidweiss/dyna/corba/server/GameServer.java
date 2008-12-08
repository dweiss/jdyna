package com.dawidweiss.dyna.corba.server;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import com.dawidweiss.dyna.corba.NetworkUtils;
import com.dawidweiss.dyna.corba.bindings.ICGameServer;
import com.dawidweiss.dyna.corba.bindings.ICGameServerHelper;

/**
 * Starts the game server and saves its initial reference to a file.
 */
public class GameServer
{
    private final static Logger logger = Logger.getAnonymousLogger();

    @Option(name = "-port", required = true, usage = "The port to bind to.")
    private int port;

    @Argument(metaVar = "ORB params", required = false)
    private List<String> args = new ArrayList<String>();

    /*
     * Console entry point.
     */
    public void start() throws Exception
    {
        /*
         * Perform initial setup. You should be familiar with this by now.
         */
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args
            .toArray(new String [args.size()]), null);
        final POA rootPOA;
        try
        {
            rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();
        }
        catch (org.omg.CORBA.ORBPackage.InvalidName ex)
        {
            throw new Exception("RootPOA missing?");
        }
        
        /*
         * Create game manager, save its reference.
         */
        final GameServerServant servant = new GameServerServant();
        final ICGameServer gameServer = 
            ICGameServerHelper.narrow(rootPOA.servant_to_reference(servant));

        final String ior = orb.object_to_string(gameServer);
        NetworkUtils.expose(ior, port);

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
        final GameServer launcher = new GameServer();
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