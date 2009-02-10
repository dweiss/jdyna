package com.kaluza.mikolajczyk.dyna.corba.factory;

import org.omg.PortableServer.POA;

import com.dawidweiss.dyna.corba.CPlayerFactoryAdapter;
import com.dawidweiss.dyna.corba.ICPlayerFactory;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;
import com.kaluza.mikolajczyk.dyna.local.factory.PsychoPlayerFactory;

public class PsychoCPlayerFactory implements ICPlayerFactory
{

    private CPlayerFactoryAdapter psychoPlayer = new CPlayerFactoryAdapter(new PsychoPlayerFactory());

    @Override
    public ICPlayerController getController(String playerName, POA poa)
    {
        return psychoPlayer.getController(playerName, poa);
    }

    @Override
    public String getDefaultPlayerName()
    {
        return psychoPlayer.getDefaultPlayerName();
    }

    @Override
    public String getVendorName()
    {
        return psychoPlayer.getVendorName();
    }

}
