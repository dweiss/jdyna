package com.dawidweiss.dyna.players;

import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerFactory;

/**
 * Factory of {@link Rabbit} players.
 */
public final class RabbitFactory implements IPlayerFactory
{
    @Override
    public IPlayerController getController(String playerName)
    {
        return new Rabbit(playerName);
    }

    @Override
    public String getDefaultPlayerName()
    {
        return "Rabbit";
    }

    @Override
    public String getVendorName()
    {
        return "Dawid Weiss";
    }
}
