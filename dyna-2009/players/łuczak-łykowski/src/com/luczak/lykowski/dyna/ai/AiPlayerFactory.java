package com.luczak.lykowski.dyna.ai;

import java.util.logging.Logger;

import org.jdyna.IPlayerController;
import org.jdyna.IPlayerFactory;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import com.dawidweiss.dyna.corba.ICPlayerFactory;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerHelper;

/**
 * Factory which creates corba players
 * @author Konrad Łykowski
 */
public class AiPlayerFactory implements ICPlayerFactory, IPlayerFactory {

	/*
	 * Player name
	 */
	private String playerName;

	/*
	 * Constructor for these class
	 */
	public AiPlayerFactory(String name) {
		this.playerName = name;
	}
	
	public AiPlayerFactory()
    {
	    this("ŁucŁyk");
    }

	/**
	 * Constructs and returns a controller for the given player name on the
	 * designated POA.
	 */
	@Override
	public ICPlayerController getController(String playerName, POA poa) {

		ICPlayerController aiController = null;

		AiPlayerServant aiServant = new AiPlayerServant(playerName);
		try {
			aiController = ICPlayerControllerHelper.narrow(poa
					.servant_to_reference(aiServant));
		} catch (ServantNotActive e) {
			Logger.getAnonymousLogger().warning(e.getMessage());
			throw new RuntimeException();
		} catch (WrongPolicy e) {
			Logger.getAnonymousLogger().warning(e.getMessage());
			throw new RuntimeException();
		}

		return aiController;
	}

	/**
	 * Return the default player name for this factory. See class documentation.
	 */
	@Override
	public String getDefaultPlayerName() {
		return playerName;
	}

	/**
	 * Return the implementation author(s), see class documentation.
	 */
	@Override
	public String getVendorName() {
		return "Ewa Łuczak, Konrad Lykowski";
	}

	@Override
	public IPlayerController getController(String playerName)
	{
	    return new AiPlayerModel(playerName);
	}
}
