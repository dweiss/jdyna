package org.jdyna.network.sockets;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.jdyna.network.packetio.SerializablePacket;
import org.jdyna.network.packetio.UDPPacketEmitter;
import org.jdyna.network.sockets.packets.UpdateControllerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerController;

/**
 * Dispatch local {@link IPlayerController} state to a remote server using UDP.
 */
public final class ControllerStateDispatch implements IGameEventListener
{
    private final static Logger logger = LoggerFactory
        .getLogger(ControllerStateDispatch.class);

    private final IPlayerController controller;
    private final UDPPacketEmitter serverUpdate;
    private final PlayerHandle playerHandle;
    private final SerializablePacket packet = new SerializablePacket();

    private ControllerState previous;

    /*
     * 
     */
    public ControllerStateDispatch(PlayerHandle handle, IPlayerController controller,
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
        if (previous == null || previous.dropsBomb != controller.dropsBomb()
            || !ObjectUtils.equals(previous.direction, controller.getCurrent()))
        {
            previous = new ControllerState(controller.getCurrent(), controller
                .dropsBomb(), 0);

            /*
             * Dispatch new state.
             */
            try
            {
                final int validityFrames = 0;

                logger.debug("Updating controller state: " + previous);

                final UpdateControllerState state = new UpdateControllerState(
                    playerHandle.gameID, playerHandle.playerID, previous.direction,
                    previous.dropsBomb, validityFrames);

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
