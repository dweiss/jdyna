package org.jdyna.players;


import org.jdyna.*;

/**
 * A factory for players bound to the given controller. Typically a keyboard
 * controller will be used to drive the player.
 */
public class CustomControllerPlayerFactory implements IPlayerFactory
{
    private final IPlayerController controller;
    private final String playerName;

    public CustomControllerPlayerFactory(IPlayerController controller)
    {
        this(controller, "Human");
    }

    public CustomControllerPlayerFactory(IPlayerController controller, String defaultPlayerName)
    {
        this.controller = controller;
        this.playerName = defaultPlayerName;
    }

    /**
     * Returns an implementation of {@link IPlayerController2} and {@link IPlayerController}.
     */
    @Override
    public IPlayerController getController(String playerName)
    {
        return controller;
    }

    @Override
    public String getDefaultPlayerName()
    {
        return playerName;
    }

    @Override
    public String getVendorName()
    {
        return "jdyna.com";
    }
}
