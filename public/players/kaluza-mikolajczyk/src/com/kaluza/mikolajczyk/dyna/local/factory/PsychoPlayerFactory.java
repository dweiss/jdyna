package com.kaluza.mikolajczyk.dyna.local.factory;

import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerFactory;
import com.kaluza.mikolajczyk.dyna.ai.AIPlayerController;

/**
 * A factory for creating an artificial player named 'PSYCHO'. 
 */
public class PsychoPlayerFactory implements IPlayerFactory
{
    private final String playerName = "PSYCHO";
    
    @Override
    public IPlayerController getController(String playerName)
    {
        return new AIPlayerController(playerName);
    }

    @Override
    public String getDefaultPlayerName()
    {
        return playerName;
    }

    @Override
    public String getVendorName()
    {
        return "Rafal Kaluza & Mariusz Mikolajczyk";
    }

}
