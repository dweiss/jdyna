package com.kozmich.dyna.ai.corba;

import org.apache.log4j.Logger;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import com.dawidweiss.dyna.corba.ICPlayerFactory;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerHelper;

/**
 * 
 * @author Lukasz Kozminski
 * @author Tomasz Michalak
 * 
 */
public class AIPlayerController implements ICPlayerFactory {

	private final static Logger logger = Logger.getLogger("AI.AIPlayerController");

	@Override
	public ICPlayerController getController(String playerName, POA poa) {
		final AIPlayerServant servant = new AIPlayerServant(playerName, poa);

		ICPlayerController player = null;
		try {
			player = ICPlayerControllerHelper.narrow(poa.servant_to_reference(servant));
		} catch (ServantNotActive e) {
			logger.error(e.getMessage());
		} catch (WrongPolicy e) {
			logger.error(e.getMessage());
		}
		return player;
	}

	@Override
	public String getDefaultPlayerName() {
		return "Katun";
	}

	@Override
	public String getVendorName() {
		return "Tomasz Michalak, Łukasz Koźmiński";
	}
}
