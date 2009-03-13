package com.komwalczyk.dyna.ai.Killer2;

import org.jdyna.IPlayerController;
import org.jdyna.IPlayerFactory;

/**
 * Factory of {@link Killer} players.
 */
public final class KillerFactory implements IPlayerFactory
{
    @Override
    public IPlayerController getController(String playerName)
    {
        return new KillerAI(playerName);
    }

    @Override
    public String getDefaultPlayerName()
    {
        return "Killer";
    }

    @Override
    public String getVendorName()
    {
        return "Komisarek-Kowalczyk";
    }
}
