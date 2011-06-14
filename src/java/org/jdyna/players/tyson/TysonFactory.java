package org.jdyna.players.tyson;

import org.jdyna.IPlayerController;
import org.jdyna.IPlayerFactory;
import org.jdyna.players.tyson.emulator.SmartBomber;

/**
 * A factory for constructing players - bots.
 * 
 * @author Michał Kozłowski
 */
public class TysonFactory implements IPlayerFactory
{
    @Override
    public IPlayerController getController(String playerName)
    {
        return new SmartBomber(playerName);
    }

    @Override
    public String getDefaultPlayerName()
    {
        return "Tyson";
    }

    @Override
    public String getVendorName()
    {
        return "Michał Kozłowski, Bartosz Wesołowski";
    }
}
