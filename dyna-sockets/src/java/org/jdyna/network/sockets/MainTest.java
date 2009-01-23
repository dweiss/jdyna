package org.jdyna.network.sockets;

import java.util.Arrays;

import javax.swing.JFrame;

import org.jdyna.network.sockets.packets.FrameData;

import com.dawidweiss.dyna.GameStartEvent;
import com.dawidweiss.dyna.audio.jxsound.GameSoundEffects;
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

        client.connect();
        final GameHandle handle = client.createGame(gameName, "classic");
        client.disconnect();

        /*
         * Create a local listener of game events on the designated UDP broadcast address.
         */
        final GameEventListenerProxy proxy = new GameEventListenerProxy();
        proxy.addListener(new GameSoundEffects());

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
         * TODO: serialize to a subclass of byte array output stream and reuse byte buffer.
         * 
         * TODO: add feedback UDP port.
         */

        final UDPPacketListener listener = new UDPPacketListener(GameServer.DEFAULT_UDP_BROADCAST);
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
