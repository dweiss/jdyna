package com.dawidweiss.dyna.corba.client;

import java.io.PrintStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.omg.PortableServer.POA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.corba.CorbaUtils;
import com.dawidweiss.dyna.corba.NetworkUtils;
import com.dawidweiss.dyna.corba.bindings.ICGameServer;
import com.dawidweiss.dyna.corba.bindings.ICGameServerHelper;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerHelper;

/**
 * Starts a single game client.
 */
public class GameClientLauncher
{
    private final static Logger logger = LoggerFactory.getLogger("corba.gameclient");

    @Option(name = "-n", aliases = "--name", required = true, metaVar = "name", usage = "Player name.")
    private String name;

    @Option(name = "-p", aliases = "--port",
        required = true, metaVar = "port", usage = "Server's IOR port.")
    protected int port;

    @Option(name = "-h", aliases = "--host", 
        required = true, metaVar = "address", usage = "Server's IOR host.")
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
         * Perform initial setup.
         */
        final org.omg.CORBA.ORB orb = CorbaUtils.initORB(iiop_host, iiop_port);
        final POA rootPOA = CorbaUtils.rootPOA(orb);        

        /*
         * Resolve game server.
         */
        final ICGameServer gameServer = ICGameServerHelper.narrow(
            orb.string_to_object(
                new String(NetworkUtils.read(host, port), "UTF-8")));

        /*
         * Create game controller, views, register player.
         */
        final PlayerServant servant = new PlayerServant(
            Globals.getDefaultKeyboardController(0));

        final ICPlayerController player = ICPlayerControllerHelper.narrow(
            rootPOA.servant_to_reference(servant));

        gameServer.register(name, player);

        /*
         * Start the ORB and run it in an infinite loop.
         */
        logger.info("Client started and ready.");
        orb.run();
    }
    
    /*
     * Console entry point.
     */
    public static void main(String [] args) throws Exception
    {
        final GameClientLauncher launcher = new GameClientLauncher();
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