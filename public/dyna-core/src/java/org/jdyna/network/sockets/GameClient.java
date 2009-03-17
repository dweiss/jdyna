package org.jdyna.network.sockets;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.jdyna.*;
import org.jdyna.audio.jxsound.JavaSoundSFX;
import org.jdyna.network.packetio.SerializablePacket;
import org.jdyna.network.packetio.UDPPacketListener;
import org.jdyna.network.sockets.packets.FrameData;
import org.jdyna.network.sockets.packets.ServerInfo;
import org.jdyna.view.swing.BoardFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A set of utilities facilitating running a remote {@link Game} over the network.
 */
public class GameClient implements IGameEventListenerHolder 
{
    /** Internal logger. */
    private final static Logger logger = LoggerFactory.getLogger(GameClient.class);

    private final GameHandle gameHandle;
    private final ServerInfo server;
    private final GameEventListenerMultiplexer proxy = new GameEventListenerMultiplexer();

    private JavaSoundSFX soundEffects;
    private BoardFrame boardFrame;

    /*
     * 
     */
    public GameClient(GameHandle gh, ServerInfo server)
    {
        this.gameHandle = gh;
        this.server = server;
    }

    /**
     * Attach sound listener to the client.
     */
    public void attachSound()
    {
        if (soundEffects != null) throw new RuntimeException("Already attached.");

        soundEffects = new JavaSoundSFX();
        proxy.addListener(soundEffects);
    }

    /*
     * 
     */
    public void attachView()
    {
        attachView(null);
    }

    /*
     * 
     */
    public void attachView(String playerName)
    {
        if (boardFrame != null) throw new RuntimeException("Already attached.");

        boardFrame = new BoardFrame();
        if (!StringUtils.isEmpty(playerName))
        {
            boardFrame.getGamePanel().trackPlayer(playerName);
        }
        proxy.addListener(boardFrame);
        boardFrame.addWindowListener(new WindowAdapter()
        {
            public void windowClosed(WindowEvent e)
            {
                proxy.removeListener(boardFrame);
            }
        });
    }

    /*
     * 
     */
    public void addListener(IGameEventListener l)
    {
        this.proxy.addListener(l);
    }

    /**
     * Enter the game loop and run indefinitely.
     */
    public void runLoop() throws IOException
    {
        /*
         * Propagate board info to all listeners.
         */
        proxy.onFrame(0, Arrays.asList(new GameStartEvent(gameHandle.info)));
        if (boardFrame != null) boardFrame.setVisible(true);

        final UDPPacketListener listener = new UDPPacketListener(server.UDPBroadcastPort);
        final SerializablePacket p = new SerializablePacket();

        final int PACKET_TIMEOUT = 1000;
        final int INITIAL_RETRIES = 3;
        long retryDeadline = System.currentTimeMillis() + PACKET_TIMEOUT;
        int retries = INITIAL_RETRIES;

        while (!Thread.currentThread().isInterrupted())
        {
            SerializablePacket p2 = listener.receive(p, PACKET_TIMEOUT);

            if (p2 != null && p.getCustom1() == PacketIdentifiers.GAME_FRAME_DATA
                && p.getCustom2() == gameHandle.gameID)
            {
                final FrameData fd = p.deserialize(FrameData.class);
                proxy.onFrame(fd.frame, fd.events);

                retryDeadline = System.currentTimeMillis() + PACKET_TIMEOUT;
                retries = INITIAL_RETRIES;
            }
            else
            {
                if (System.currentTimeMillis() > retryDeadline)
                {
                    retryDeadline = System.currentTimeMillis() + PACKET_TIMEOUT;
                    if (--retries > 0)
                    {
                        logger.warn("Receiving no packets from the server... retries: "
                            + retries);
                    }
                    else break;
                }
            }
        }

        logger.info("Shutting down...");
        listener.close();
        proxy.onFrame(0, Arrays.asList(new GameOverEvent()));

        if (boardFrame != null) boardFrame.dispose();
        if (soundEffects != null) soundEffects.dispose();

        logger.info("Done.");
    }

    /*
     * 
     */
    @Override
    public Collection<IGameEventListener> getListeners()
    {
        return proxy.getListeners();
    }

    /*
     * 
     */
    @Override
    public void removeListener(IGameEventListener l)
    {
        throw new UnsupportedOperationException("Not implemented on this object.");
    }
}
