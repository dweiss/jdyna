package com.michalkalinowski.dyna.players;

import org.omg.PortableServer.POA;

import com.dawidweiss.dyna.corba.CPlayerFactoryAdapter;
import com.dawidweiss.dyna.corba.ICPlayerFactory;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;

public final class Cr4zeeCPlayerFactory implements ICPlayerFactory
{
    final ICPlayerFactory delegate;

    public Cr4zeeCPlayerFactory()
    {
        delegate = new CPlayerFactoryAdapter(new Cr4zeePlayerFactory());
    }

    @Override
    public ICPlayerController getController(String playerName, POA poa)
    {
        return delegate.getController(playerName, poa);
    }

    @Override
    public String getDefaultPlayerName()
    {
        return delegate.getDefaultPlayerName();
    }

    @Override
    public String getVendorName()
    {
        return delegate.getVendorName();
    }
}
