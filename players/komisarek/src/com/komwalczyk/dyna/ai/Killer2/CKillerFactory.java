package com.komwalczyk.dyna.ai.Killer2;

import org.omg.PortableServer.POA;

import com.dawidweiss.dyna.corba.CPlayerFactoryAdapter;
import com.dawidweiss.dyna.corba.ICPlayerFactory;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;

/**
 * Factory for CORBA Killer.
 */
public class CKillerFactory implements ICPlayerFactory
{
	private CPlayerFactoryAdapter killer = new CPlayerFactoryAdapter(new KillerFactory());
	
	@Override
	public String getDefaultPlayerName() 
	{
		return killer.getDefaultPlayerName();
	}

	@Override
	public String getVendorName() 
	{
		return killer.getVendorName();
	}

	@Override
	public ICPlayerController getController(String playerName, POA poa) 
	{
		return killer.getController(playerName, poa);
	}
}
