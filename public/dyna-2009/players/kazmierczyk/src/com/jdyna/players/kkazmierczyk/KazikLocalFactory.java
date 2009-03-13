/** Once a time it was required to name packages in this convention */
package com.jdyna.players.kkazmierczyk;

import org.jdyna.IPlayerController;
import org.jdyna.IPlayerFactory;

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
