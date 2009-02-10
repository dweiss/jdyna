package com.jdyna.corba;

import org.omg.CORBA.UserException;
import org.omg.PortableServer.POA;

import com.dawidweiss.dyna.IPlayerFactory;
import com.dawidweiss.dyna.corba.ICPlayerControllerAdapter;
import com.dawidweiss.dyna.corba.ICPlayerFactory;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerHelper;
import com.jdyna.players.kozwes.BomberFactory;

/**
 * Creates {@link ICPlayerFactory} from {@link BomberFactory}
 * 
 * @author Michał Kozłowski
 */
public class CBomberFactory implements ICPlayerFactory {
	private final IPlayerFactory delegate = new BomberFactory();

	@Override
	public ICPlayerController getController(String playerName, POA poa) {
		try {
			return ICPlayerControllerHelper.narrow(poa.servant_to_reference(new ICPlayerControllerAdapter(delegate
					.getController(playerName))));
		} catch (UserException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDefaultPlayerName() {
		return delegate.getDefaultPlayerName();
	}

	@Override
	public String getVendorName() {
		return delegate.getVendorName();
	}

}
