package com.kdyna.corba;

import org.omg.CORBA.UserException;
import org.omg.PortableServer.POA;

import com.dawidweiss.dyna.IPlayerFactory;
import com.dawidweiss.dyna.corba.ICPlayerFactory;

import com.dawidweiss.dyna.corba.bindings.ICPlayerController;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerHelper;
import com.kdyna.KDynaPlayerFactory;

public class CorbaPlayerFactory implements ICPlayerFactory {
	private final IPlayerFactory factory = new KDynaPlayerFactory();

	public ICPlayerController getController(String playerName, POA poa) {
		
		final CorbaPlayerServant servant = new CorbaPlayerServant(factory, playerName, poa);
		final ICPlayerController controller;
		
		try {
			controller = ICPlayerControllerHelper.narrow(poa.servant_to_reference(servant));
		} catch (UserException e) {
			throw new RuntimeException(e);
		}
		return controller;
	}
	

	public String getDefaultPlayerName() {
		return factory.getDefaultPlayerName();
	}

	public String getVendorName() {
		return factory.getVendorName();
	}




}
