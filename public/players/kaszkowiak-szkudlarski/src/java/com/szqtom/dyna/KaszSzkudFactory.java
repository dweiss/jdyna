package com.szqtom.dyna;

import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerFactory;

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
