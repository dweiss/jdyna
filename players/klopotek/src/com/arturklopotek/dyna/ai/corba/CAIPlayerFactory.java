package com.arturklopotek.dyna.ai.corba;

import org.omg.PortableServer.POA;

import com.arturklopotek.dyna.ai.AIPlayerController;
import com.arturklopotek.dyna.ai.AIPlayerFactory;
import com.dawidweiss.dyna.corba.ICPlayerFactory;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerHelper;

/** A CORBA wrapper for local IPlayerFactory interface. */
public class CAIPlayerFactory implements ICPlayerFactory
{

    /** The underlying factory (local interface being wrapped) */
    private final AIPlayerFactory factory = new AIPlayerFactory();
    private ICPlayerController controller;

    @Override
    public ICPlayerController getController(String playerName, POA poa)
    {
        try
        {
            AIPlayerController local = factory.getController(playerName);
            AIPlayerControllerServant servant = new AIPlayerControllerServant(local);
            org.omg.CORBA.Object stub = poa.servant_to_reference(servant);
            controller = ICPlayerControllerHelper.narrow(stub);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not create a reference from servant",e);
        }
        return controller;
    }

    @Override
    public String getDefaultPlayerName()
    {
        return factory.getDefaultPlayerName();
    }

    @Override
    public String getVendorName()
    {
        return factory.getVendorName();
    }

}
