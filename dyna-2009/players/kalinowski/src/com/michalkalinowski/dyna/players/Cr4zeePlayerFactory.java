package com.michalkalinowski.dyna.players;

import org.jdyna.IPlayerController;
import org.jdyna.IPlayerFactory;

public final class Cr4zeePlayerFactory implements IPlayerFactory
{
    @Override
    public IPlayerController getController(String playerName)
    {
        return new Cr4zeePlayer(playerName);
    }

    @Override
    public String getDefaultPlayerName()
    {
        return Cr4zeePlayer.NAME;
    }

    @Override
    public String getVendorName()
    {
        return Cr4zeePlayer.VENDOR;
    }
}
