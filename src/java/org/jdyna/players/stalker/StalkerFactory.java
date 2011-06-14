package org.jdyna.players.stalker;

import org.jdyna.*;

public final class StalkerFactory implements IPlayerFactory
{
    private IPlayerController controller;

    @Override
    public IPlayerController getController(String playerName)
    {
        if (controller == null)
        {
            controller = new StalkerController(playerName);
        }
        return controller;
    }

    @Override
    public String getDefaultPlayerName()
    {
        return "stalker";
    }

    @Override
    public String getVendorName()
    {
        return "Łukasz Chojnacki, Daniel Zieliński";
    }
}
