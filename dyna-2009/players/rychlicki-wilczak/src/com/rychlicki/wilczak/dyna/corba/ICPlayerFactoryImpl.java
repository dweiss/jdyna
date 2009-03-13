package com.rychlicki.wilczak.dyna.corba;

import org.omg.CORBA.UserException;
import org.omg.PortableServer.POA;

import com.dawidweiss.dyna.corba.ICPlayerFactory;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerHelper;

/**
 * ICPlayerFactory implementation.
 */
public class ICPlayerFactoryImpl implements ICPlayerFactory {

	@Override
	public ICPlayerController getController(String playerName, POA poa) {

		try {
			return ICPlayerControllerHelper.narrow(poa
					.servant_to_reference(new AIPlayerController(playerName,
							poa)));
		} catch (UserException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDefaultPlayerName() {
		return "Looser";
	}

	@Override
	public String getVendorName() {
		return "Karol&Darek LLP";
	}

}
