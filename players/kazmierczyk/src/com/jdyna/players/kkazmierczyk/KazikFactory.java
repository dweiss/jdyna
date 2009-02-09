package com.jdyna.players.kkazmierczyk;

import java.util.logging.Logger;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import com.dawidweiss.dyna.corba.ICPlayerFactory;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerHelper;
import com.krzysztofkazmierczyk.dyna.client.PlayerServant;

public class KazikFactory implements ICPlayerFactory
{
    private final static Logger logger = Logger.getLogger(KazikFactory.class.getName());

    /**
     * It is a small problem that it is a constant value due to fact, that I cannot run
     * the same code inside one vm with different players. When it will need change, it
     * will be changed :) 
     * TODO move it to properties
     **/
    public final static String PLAYER_NAME = "Kazik1616";

    @Override
    public String getDefaultPlayerName()
    {
        return PLAYER_NAME;
    }

    @Override
    public String getVendorName()
    {
        return "Kaźmierczyk Krzysztof";
    }

    @Override
    public ICPlayerController getController(String playerName, POA poa)
    {
        /*
         * Create game controller, views, register player.
         */
        final PlayerServant servant = new PlayerServant(playerName);

        ICPlayerController player = null;
        try
        {
            player = ICPlayerControllerHelper.narrow(poa.servant_to_reference(servant));
        }
        catch (ServantNotActive e)
        {
            logger.severe("Servant is not active.");
            throw new RuntimeException(e);
        }
        catch (WrongPolicy e)
        {
            logger.severe("Wrong policy.");
            throw new RuntimeException(e);
        }

        return player;
    }

}
