package org.jdyna.network.sockets;

import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.util.Arrays;

import javax.swing.JFrame;

import org.jdyna.network.sockets.packets.FrameData;

import com.dawidweiss.dyna.GameStartEvent;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerFactory;
import com.dawidweiss.dyna.players.HumanPlayerFactory;
import com.dawidweiss.dyna.view.swing.BoardFrame;

public class MainTest
{
    public static void main(final String [] args) throws Exception
    {
        new Thread()
        {
            public void run()
            {
                GameServer.main(new String [] {});
            };
        }.start();

        Thread.sleep(1000);

        /*
         * Create a game room and join it.
         */

        final String gameName = "polska-niemcy-klapa";
        final GameClient client = new GameClient();
        client.host = "127.0.0.1";

        final GameEventListenerProxy proxy = new GameEventListenerProxy();

        client.connect();
        final GameHandle handle = client.createGame(gameName, "classic");

        final DatagramSocket socket = new DatagramSocket();
        final UDPPacketEmitter serverUpdater = new UDPPacketEmitter(socket);
        serverUpdater.setDefaultTarget(
            Inet4Address.getByName(client.host), GameServer.DEFAULT_UDP_FEEDBACK_PORT);

        final IPlayerFactory pf1 = new HumanPlayerFactory(
            Globals.getDefaultKeyboardController(0));
        final String playerName = pf1.getDefaultPlayerName();
        final IPlayerController controller = pf1.getController(playerName);
        final PlayerHandle player1 = client.joinGame(handle, playerName);
        if (controller instanceof IGameEventListener)
        {
            proxy.addListener((IGameEventListener) controller);
        }
        proxy.addListener(new ControllerStateDispatch(player1, controller, serverUpdater));

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
         * 
         * TODO: UDP packets should contain game id so that they can be filtered without deserializing
         * other stuff.
         * 
         * TODO: Serialize to a subclass of byte array output stream and reuse byte buffer.
         * 
         * TODO: add feedback UDP port.
         * 
         * TODO: add auto-discovery.
         */

        final UDPPacketListener listener = new UDPPacketListener(
            GameServer.DEFAULT_UDP_BROADCAST);
        Packet p;
        while ((p = listener.receive()) != null)
        {
            final Object o = ObjectPacket.deserialize(p);
            if (o instanceof FrameData)
            {
                // TODO: filter start game from the event stream?
                final FrameData fd = (FrameData) o;
                proxy.onFrame(fd.frame, fd.events);
            }
        }
    }
}
