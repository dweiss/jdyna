package com.dawidweiss.dyna.corba.client;

import java.io.PrintStream;
import java.util.logging.Logger;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import com.dawidweiss.dyna.corba.NetworkUtils;
import com.dawidweiss.dyna.corba.bindings.CPlayer;
import com.dawidweiss.dyna.corba.bindings.ICGame;
import com.dawidweiss.dyna.corba.bindings.ICGameServer;
import com.dawidweiss.dyna.corba.bindings.ICGameServerHelper;

/**
 * Starts a single game between a set of players.
 */
public class GameLauncher
{
    private final static Logger logger = Logger.getAnonymousLogger();

    @Option(name = "-server", required = true)
    private String server;

    @Option(name = "-port", required = true)
    private int port;

    /*
     * Console entry point.
     */
    public void start() throws Exception
    {
        /*
         * Perform initial setup. You should be familiar with this by now.
         */
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
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
         * Create the game.
         */
        CPlayer [] players = gameServer.players();
        if (players.length < 2)
        {
            logger.warning("Not enough players: " + players.length);
        }
        else
        {
            logger.warning("Creating game for " + players.length + " players.");
            ICGame game = gameServer.create(players);
            logger.warning("Running the game.");
            game.run();
        }
    }
    
    /*
     * Console entry point.
     */
    public static void main(String [] args) throws Exception
    {
        final GameLauncher launcher = new GameLauncher();
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