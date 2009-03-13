package com.lcdz.dyna.factories;

import org.omg.CORBA.UserException;
import org.omg.PortableServer.POA;

import com.dawidweiss.dyna.corba.ICPlayerControllerAdapter;
import com.dawidweiss.dyna.corba.ICPlayerFactory;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerHelper;
import com.lcdz.dyna.ai.AIPlayerController;

public class AICPlayerFactory implements ICPlayerFactory {

	private static final String DEFAULT_NAME = "twoj_stary";
	private static final String VENDOR_NAME = "chojnacki-zielinski";
	
	private ICPlayerController pc = null;
	
	@Override
	public ICPlayerController getController(String playerName, POA poa) {
		if (pc != null) {
			pc._release();
		}
		
		try {
			pc = ICPlayerControllerHelper.narrow(poa.servant_to_reference(
					new ICPlayerControllerAdapter(new AIPlayerController(playerName))));
		} catch (UserException e) {
			throw new RuntimeException(e);
		}
		
		return pc;
	}

	@Override
	public String getDefaultPlayerName() {
		return DEFAULT_NAME;
	}

	@Override
	public String getVendorName() {
		return VENDOR_NAME;
	}
}
