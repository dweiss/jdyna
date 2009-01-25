package org.jdyna.network.test;

import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import org.jdyna.network.packetio.SerializablePacket;
import org.jdyna.network.packetio.UDPPacketEmitter;
import org.jdyna.network.packetio.UDPPacketListener;
import org.jdyna.network.sockets.ControllerStateDispatch;
import org.jdyna.network.sockets.GameClient;
import org.jdyna.network.sockets.GameEventListenerMultiplexer;
import org.jdyna.network.sockets.GameHandle;
import org.jdyna.network.sockets.GameServer;
import org.jdyna.network.sockets.PacketIdentifiers;
import org.jdyna.network.sockets.PlayerHandle;
import org.jdyna.network.sockets.packets.FrameData;
import org.jdyna.network.sockets.packets.ServerInfo;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.GameStartEvent;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerFactory;
import com.dawidweiss.dyna.players.HumanPlayerFactory;
import com.dawidweiss.dyna.view.swing.BoardFrame;

/**
 * Set up a server and two clients locally.
 */
public class MainTest
{
    public static void main(final String [] args) throws Exception
    {
        LoggerFactory.getLogger(MainTest.class).debug("Discovering servers...");

        /*
         * Start the game server. 
         */
        new Thread()
        {
            public void run()
            {
                GameServer.main(new String [] {});
            };
        }.start();

        /*
         * Auto-discovery of running servers for at most five seconds. Take the
         * first server available.
         */
        final List<ServerInfo> si = GameClient.lookup(GameServer.DEFAULT_UDP_BROADCAST, 1, 5000);
        if (si.size() < 0)
        {
            throw new Exception("No available servers.");
        }

        /*
         * Create a game room and join it.
         */

        final ServerInfo server = si.get(0);
        final String gameName = "polska-niemcy-klapa";

        /*
         * Connect to the server. 
         */
        final GameClient client = new GameClient(server);
        client.connect();

        final GameEventListenerMultiplexer proxy = new GameEventListenerMultiplexer();
        final GameHandle handle = client.createGame(gameName, "classic");

        final DatagramSocket socket = new DatagramSocket();
        final UDPPacketEmitter serverUpdater = new UDPPacketEmitter(socket);
        serverUpdater.setDefaultTarget(
            Inet4Address.getByName(server.serverAddress), server.UDPFeedbackPort);

        final IPlayerFactory pf1 = new HumanPlayerFactory(Globals
            .getDefaultKeyboardController(0));
        final String playerName = pf1.getDefaultPlayerName();
        final IPlayerController controller = pf1.getController(playerName);
        final PlayerHandle player1 = client.joinGame(handle, playerName);
        if (controller instanceof IGameEventListener)
        {
            proxy.addListener((IGameEventListener) controller);
        }
        proxy
            .addListener(new ControllerStateDispatch(player1, controller, serverUpdater));

        client.disconnect();

        /*
         * Create a local listener of game events on the designated UDP broadcast address.
         */
        // proxy.addListener(new GameSoundEffects());
        final BoardFrame frame = new BoardFrame();
        proxy.addListener(frame);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*
         * Propagate board info to all listeners if joining to a game.
         */
        proxy.onFrame(0, Arrays.asList(new GameStartEvent(handle.info)));

        /*
         * TODO: add a timeout to udp packet listener? If no events appear on input, check
         * if the server still runs the game. 
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
}
