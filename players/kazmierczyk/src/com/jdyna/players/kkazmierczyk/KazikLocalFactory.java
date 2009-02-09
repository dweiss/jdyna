/** Once a time it was required to name packages in this convention */
package com.jdyna.players.kkazmierczyk;

import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerFactory;

public class KazikLocalFactory implements IPlayerFactory
{

    @Override
    public IPlayerController getController(String playerName)
    {
        return new KazikLocalController();
    }

    @Override
    public String getDefaultPlayerName()
    {
        return KazikFactory.PLAYER_NAME;
    }

    @Override
    public String getVendorName()
    {
        return "Krzysztof Kaźmierczyk";
    }

}
