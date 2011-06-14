package org.jdyna;

/**
 * A future replacement interface for {@link IPlayerController}, returning an immutable
 * {@link ControllerState} instead of two independent method calls and providing a
 * <i>valid frame count</i> for the controller state in case the controller is not able to
 * keep up with the game frame rate.
 * <p>
 * A "typical" scenario for fast controllers is that {@link ControllerState#validFrames}
 * contains zero, meaning its state never expires. Slower (and asynchronous) controllers
 * will have this value set to 1 or the number of frames they wish to apply the state on
 * the game server's side. For example, to move the player leftwards three times a single
 * frame's offset, set {@link ControllerState#validFrames} to 3 and wait until the player
 * reaches the desired position. Any subsequent non-null state returned from
 * {@link #getState()} resets the server's side state.
 */
public interface IPlayerController2
{
    /**
     * @return Return the current controller state. <code>null</code> means no change
     *         should be reflected on the game controller's side (the previous state
     *         should remain valid, unless its validity frame counter expired).
     */
    public ControllerState getState();
}
