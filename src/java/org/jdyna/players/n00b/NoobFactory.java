package org.jdyna.players.n00b;

import org.jdyna.IPlayerController;
import org.jdyna.IPlayerFactory;

/**
 * n00b-producing factory. Capable of generating both local and corba players.
 */
public class NoobFactory implements IPlayerFactory
{
    @Override
    public IPlayerController getController(String playerName)
    {
        return new NoobPlayer(playerName);
    }

    @Override
    public String getDefaultPlayerName()
    {
        return "n00b";
    }

    @Override
    public String getVendorName()
    {
        return "Piotr Jędrzejczak, Michał Nowak";
    }
}
