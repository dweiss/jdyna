package AIplayer.com.inf71391.dyna.client;

import org.apache.log4j.Logger;
import org.jdyna.IPlayerFactory;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import com.dawidweiss.dyna.corba.ICPlayerFactory;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerHelper;

/**
 * A factory for constructing {@link CIPlayerController}, corba players.
 * 
 * @see IPlayerFactory
 * @author Łukasz Witkowski
 */
public class stubAIPlayerController implements ICPlayerFactory {

	private final static Logger logger = Logger
			.getLogger("AI.stubAIPlayerController");

	// @Override
	public ICPlayerController getController(String playerName, POA poa) {

		/*
		 * Create game controller, views, register player.
		 */
		final AIPlayerServant servant = new AIPlayerServant(playerName,poa);

		ICPlayerController player = null;
		try {
			player = ICPlayerControllerHelper.narrow(poa
					.servant_to_reference(servant));
		} catch (ServantNotActive e) {
			logger.error(e.getMessage());
		} catch (WrongPolicy e) {
			logger.error(e.getMessage());
		}
		
		return player;
	}

	// @Override
	public String getDefaultPlayerName() {
		return "BezwzglednyBrutal";
	}

	// @Override
	public String getVendorName() {
		return "Lukasz Witkowski, Marcin Witkowski";
	}

}
