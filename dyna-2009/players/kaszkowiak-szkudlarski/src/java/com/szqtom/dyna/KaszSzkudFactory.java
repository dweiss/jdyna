package com.szqtom.dyna;

import org.jdyna.IPlayerController;
import org.jdyna.IPlayerFactory;

public class KaszSzkudFactory implements IPlayerFactory
{
    @Override
    public IPlayerController getController(String playerName)
    {
        return new DefensiveController(playerName);
    }

    @Override
    public String getDefaultPlayerName()
    {
        return "KaszSzkud";
    }
    
    @Override
    public String getVendorName()
    {
        return "Kaszkowiak-Szkudlarski";
    }
}
