package com.dawidweiss.dyna.corba;

import org.jdyna.IPlayerFactory;
import org.omg.CORBA.UserException;
import org.omg.PortableServer.POA;

import com.dawidweiss.dyna.corba.bindings.*;

/**
 * An adapter from {@Link IPlayerFactory} to {@link ICPlayerFactory} using
 * {@link ICPlayerControllerAdapter}. 
 */
public final class CPlayerFactoryAdapter implements ICPlayerFactory
{
    private final IPlayerFactory delegate;

    public CPlayerFactoryAdapter(IPlayerFactory playerFactory)
    {
        this.delegate = playerFactory;
    }

    public ICPlayerController getController(String playerName, POA poa)
    {
        try
        {
            return ICPlayerControllerHelper
                .narrow(poa.servant_to_reference(
                    new ICPlayerControllerAdapter(delegate.getController(playerName))));
        }
        catch (UserException e)
        {
            throw new RuntimeException(e);
        }
    }

    public String getDefaultPlayerName()
    {
        return delegate.getDefaultPlayerName();
    }

    public String getVendorName()
    {
        return delegate.getVendorName();
    }
}
