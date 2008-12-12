package com.dawidweiss.dyna.corba.client;

import java.io.PrintStream;
import java.util.logging.Logger;

import org.kohsuke.args4j.*;
import org.omg.PortableServer.POA;

import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.corba.CorbaUtils;
import com.dawidweiss.dyna.corba.NetworkUtils;
import com.dawidweiss.dyna.corba.bindings.*;

/**
 * Starts a single game client.
 */
public class GameClient
{
    private final static Logger logger = Logger.getAnonymousLogger();

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
         * Create game client, register it.
         */
        final IPlayerController local = Globals.getDefaultKeyboardController(0);
        final ICPlayerController controller = ICPlayerControllerHelper.narrow(
            rootPOA.servant_to_reference(new KeyboardPlayerController(local)));

        gameServer.register(name, controller);

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
        final GameClient launcher = new GameClient();
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