package org.jdyna.network.sockets;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.util.Arrays;
import java.util.List;

import org.jdyna.network.packetio.*;
import org.jdyna.network.sockets.packets.FrameData;
import org.jdyna.network.sockets.packets.ServerInfo;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.*;
import com.dawidweiss.dyna.audio.jxsound.GameSoundEffects;
import com.dawidweiss.dyna.view.swing.BoardFrame;

/**
 * A client that wraps {@link IPlayerFactory} and connects to the server, creating or
 * joining a game with the given name.
 */
public class BotClient
{
    private final static Logger logger = LoggerFactory
        .getLogger(BotClient.class);

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
     * Game room name to join.
     */
    @Option(name = "-g", aliases = "--game", required = true, metaVar = "name", usage = "Game room to join or create.")
    public String gameName;

    /**
     * Board name, if the game room is being created.
     */
    @Option(name = "-b", aliases = "--board", required = false, metaVar = "name", usage = "Board name if the game should be created.")
    public String boardName;

    /**
     * Player name override.
     */
    @Option(name = "-n", aliases = "--player-name", metaVar = "name", required = true, usage = "Player name (must be unique in the game).")
    public String playerName;

    /**
     * Disable local sound.
     */
    @Option(name = "--no-sound", required = false, usage = "Disable sound output.")
    public boolean noSound;

    /**
     * Disable local view.
     */
    @Option(name = "--no-view", required = false, usage = "Disable local view.")
    public boolean noView;

    /**
     * Player factory class.
     */
    @Argument(index = 0, metaVar = "class", required = true, usage = "Fully qualified class name implementing IPlayerFactory.")
    public String clientClass;

    /*
     * 
     */
    public void start() throws Exception
    {
        /*
         * Dynamically attempt to instantiate the client.
         */
        logger.info("Instantiating the client.");

        final IPlayerFactory factory;
        try
        {
            Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(clientClass);
            factory = (IPlayerFactory) c.newInstance();
        }
        catch (ClassCastException e)
        {
            logger.error("Class does not implement " + IPlayerFactory.class.getName());
            return;
        }

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

        // Create a game room or join an existing game room of the given name.
        GameHandle handle;
        if (boardName != null)
        {
            logger.info("Creating a new game: " + gameName + " [board: " + boardName + "]");
            handle = client.createGame(gameName, boardName);
        }
        else
        {
            int attempts = 5;
            do
            {
                logger.info("Joining an existing game [attempts left: " + attempts + "]: " + gameName);
                handle = client.getGame(gameName);

                if (handle == null) Thread.sleep(1000);
            } while (handle == null && --attempts > 0);

            if (handle == null)
            {
                logger.error("No such game: " + gameName + " (create a new one by specifying board name).");
                return;
            }
        }

        /*
         * Create: the client controller, feedback UDP port, proxy for local listeners.
         */
        final UDPPacketEmitter serverUpdater = new UDPPacketEmitter(new DatagramSocket());
        serverUpdater.setDefaultTarget(
            Inet4Address.getByName(server.serverAddress), server.UDPFeedbackPort);

        // Join the remote game.
        final PlayerHandle playerHandle = client.joinGame(handle, playerName);

        // Event proxy to local clients.
        final GameEventListenerMultiplexer proxy = new GameEventListenerMultiplexer();

        // Create local asynchronous controller wrapper.
        final IPlayerController localController = factory.getController(playerName);

        // Asynchronous mode.
        final AsyncPlayerController asyncController = new AsyncPlayerController(localController);
        proxy.addListener(asyncController); 
        proxy.addListener(new ControllerStateDispatch(playerHandle, asyncController, serverUpdater));

        // Disconnect the control link, we don't need it anymore.
        client.disconnect();

        /*
         * Create a local listeners - sound, view.
         */
        if (!noSound) proxy.addListener(new GameSoundEffects());

        if (!noView)
        {
            final BoardFrame frame = new BoardFrame();
            proxy.addListener(frame);
            frame.setVisible(true);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e)
                {
                    proxy.removeListener(frame);
                    frame.dispose();
                }
            });
        }

        /*
         * Propagate board info to all listeners if joining to a game.
         */
        proxy.onFrame(0, Arrays.asList(new GameStartEvent(handle.info)));

        /*
         * TODO: Detect if the game is still in progress on the server. If not, close.
         */

        final UDPPacketListener listener = new UDPPacketListener(server.UDPBroadcastPort);
        SerializablePacket p = new SerializablePacket();
        while ((p = listener.receive(p)) != null)
        {
            if (p.getCustom1() == PacketIdentifiers.GAME_FRAME_DATA
                && p.getCustom2() == handle.gameID)
            {
                final FrameData fd = p.deserialize(FrameData.class);
                proxy.onFrame(fd.frame, fd.events);
            }
        }
    }

    /**
     * Command line entry point.
     */
    public static void main(String [] args)
    {
        final BotClient me = new BotClient();
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
