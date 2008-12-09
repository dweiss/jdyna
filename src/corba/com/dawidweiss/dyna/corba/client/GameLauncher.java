package com.dawidweiss.dyna.corba.client;

import java.io.PrintStream;
import java.util.logging.Logger;

import org.kohsuke.args4j.*;

import com.dawidweiss.dyna.corba.CorbaUtils;
import com.dawidweiss.dyna.corba.NetworkUtils;
import com.dawidweiss.dyna.corba.bindings.*;

/**
 * Starts a single game between a set of players.
 */
public class GameLauncher
{
    private final static Logger logger = Logger.getAnonymousLogger();

    @Option(name = "-b", aliases = "--board", required = false, metaVar = "number", 
        usage = "Board number.")
    private int board = 0;

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

        /*
         * Resolve game server.
         */
        final ICGameServer gameServer = ICGameServerHelper.narrow(
            orb.string_to_object(
                new String(NetworkUtils.read(host, port), "UTF-8")));

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
            ICGame game = gameServer.create(board, players);
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