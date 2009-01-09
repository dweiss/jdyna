package com.dawidweiss.dyna.corba;

import java.io.PrintStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.corba.bindings.CPlayer;
import com.dawidweiss.dyna.corba.bindings.ICGame;
import com.dawidweiss.dyna.corba.bindings.ICGameServer;
import com.dawidweiss.dyna.corba.bindings.ICGameServerHelper;

/**
 * Starts a single game between all players registered on the server.
 */
public class GameLauncher
{
    private final static Logger logger = LoggerFactory.getLogger("corba.gamelauncher");

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
            logger.warn("Not enough players: " + players.length);
        }
        else
        {
            logger.info("Creating game for " + players.length + " players.");
            ICGame game = gameServer.create(board, players);
            logger.info("Running the game.");
            game.run(Integer.MAX_VALUE);
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