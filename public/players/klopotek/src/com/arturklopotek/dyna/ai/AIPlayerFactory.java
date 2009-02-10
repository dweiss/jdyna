package com.arturklopotek.dyna.ai;

import com.dawidweiss.dyna.IPlayerFactory;

/**A factory producing instances of {@link AIPlayerController}. */
public class AIPlayerFactory implements IPlayerFactory
{

    @Override
    public AIPlayerController getController(String playerName)
    {
        return new AIPlayerController(playerName);
    }

    @Override
    public String getDefaultPlayerName()
    {
        return "Golem";
    }

    @Override
    public String getVendorName()
    {
        return "Artur Kłopotek";
    }

}
