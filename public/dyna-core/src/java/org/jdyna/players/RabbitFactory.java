package org.jdyna.players;

import org.jdyna.IPlayerController;
import org.jdyna.IPlayerFactory;

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
