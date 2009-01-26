package org.jdyna.network.sockets;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdyna.network.sockets.packets.ServerInfo;
import org.kohsuke.args4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.CmdLine;

/**
 * Console-level admin for jdyna server.
 */
public class Admin
{
    private final static Logger logger = LoggerFactory.getLogger(Admin.class);

    public enum Command
    {
        games,
        newgame
    }
    
    /**
     * Server broadcast port.
     */
    @Option(name = "-bp", aliases = "--broadcast-port", required = false, metaVar = "port", usage = "UDP broadcast port (default: "
        + GameServer.DEFAULT_UDP_BROADCAST + ").")
    public int UDPBroadcastPort = GameServer.DEFAULT_UDP_BROADCAST;

    /**
     * Discovery timeout.
     */
    @Option(name = "-dt", aliases = "--discovery-timeout", required = false, metaVar = "port", usage = "Server discovery timeout (milliseconds).")
    public int discoveryTimeout = 5000;

    /**
     * Game room name to create.
     */
    @Option(name = "-g", aliases = "--game", required = false, metaVar = "name", usage = "Game room to create.")
    public String gameName;

    /**
     * Board name, if the game room is being created.
     */
    @Option(name = "-b", aliases = "--board", required = false, metaVar = "name", usage = "Board name if the game should be created.")
    public String boardName;

    /**
     * Command to run.
     */
    @Argument(index = 0, metaVar = "command", required = true, usage = "Command: [games | newgame]")
    public Command command;

    /*
     * 
     */
    public void start() throws Exception
    {
        /*
         * Auto-discovery of running servers for at most five seconds. Take the first
         * server available.
         */
        logger.info("Discovering servers...");
        final List<ServerInfo> si = GameServerClient.lookup(UDPBroadcastPort, 1,
            discoveryTimeout);

        if (si.size() != 1)
        {
            logger.warn("No available servers discovered.");
            return;
        }

        /*
         * Connect to the first available server.
         */
        final ServerInfo server = si.get(0);
        final GameServerClient client = new GameServerClient(server);
        client.connect();

        try
        {
            switch (command)
            {
                case games:
                    final List<GameHandle> games = client.listGames();
                    System.out.println("Games on the server: " + games.size());
                    for (GameHandle gh : games)
                    {
                        System.out.println("  Game[name=" + gh.gameName + ", id=" + gh.gameID + "]");
                    }
                    break;

                case newgame:
                    if (StringUtils.isEmpty(gameName)) 
                        throw new CmdLineException("Game name is required.");

                    final GameHandle gh = client.createGame(gameName, boardName);
                    logger.info("Game created [id=" + gh.gameID + "]");

                    break;
            }
        }
        finally
        {
            client.disconnect();
        }
    }

    /**
     * Command line entry point.
     */
    public static void main(String [] args)
    {
        final Admin me = new Admin();
        if (CmdLine.parseArgs(me, args))
        {
            try
            {
                me.start();
            }
            catch (Exception e)
            {
                logger.error("Unhandled error.", e);
            }
        }
    }
}
