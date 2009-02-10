package org.jdyna.network.sockets;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.jdyna.network.packetio.SerializablePacket;
import org.jdyna.network.packetio.UDPPacketEmitter;
import org.jdyna.network.sockets.packets.UpdateControllerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.*;

/**
 * Dispatch local {@link IPlayerController} state to a remote server using UDP.
 */
public final class ControllerStateDispatch implements IGameEventListener
{
    private final static Logger logger = LoggerFactory
        .getLogger(ControllerStateDispatch.class);

    private final IPlayerController2 controller;
    private final UDPPacketEmitter serverUpdate;
    private final PlayerHandle playerHandle;
    private final SerializablePacket packet = new SerializablePacket();

    private ControllerState previous;

    /*
     * 
     */
    public ControllerStateDispatch(PlayerHandle handle, IPlayerController2 controller,
        UDPPacketEmitter serverUpdate)
    {
        this.playerHandle = handle;
        this.controller = controller;
        this.serverUpdate = serverUpdate;
    }

    /*
     * 
     */
    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        final ControllerState current = controller.getState();

        if (current == null)
        {
            previous = null;
        }
        else if (ObjectUtils.equals(current, previous) && previous != null && previous.validFrames == 0)
        {
            return;
        }
        else
        {
            previous = current;

            try
            {
                logger.debug("Updating controller state: " + current);

                final UpdateControllerState state = new UpdateControllerState(
                    playerHandle.gameID, playerHandle.playerID, current);

                packet.serialize(PacketIdentifiers.PLAYER_CONTROLLER_STATE,
                    playerHandle.gameID, state);

                serverUpdate.send(packet);
            }
            catch (IOException e)
            {
                logger.warn("Could not dispatch state.", e);
            }
        }
    }
}
