package com.dawidweiss.dyna.corba;

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

import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IController;
import com.dawidweiss.dyna.corba.bindings.ICGameServer;
import com.dawidweiss.dyna.corba.bindings.ICGameServerHelper;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerHelper;

/**
 * Starts a single game client.
 */
public class GameClient
{
    private final static Logger logger = Logger.getAnonymousLogger();

    @Option(name = "-name", required = true)
    private String name;

    @Option(name = "-server", required = true)
    private String server;

    @Option(name = "-port", required = true)
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
         * Resolve game server.
         */
        final ICGameServer gameServer = ICGameServerHelper.narrow(
            orb.string_to_object(
                new String(NetworkUtils.read(server, port), "UTF-8")));

        /*
         * Create game client, register it.
         */
        final IController local = Globals.getDefaultKeyboardController(0);
        final ICPlayerController controller = ICPlayerControllerHelper.narrow(
            rootPOA.servant_to_reference(new ClientSidePlayerController(local)));

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