package ai;

import org.omg.CORBA.UserException;
import org.omg.PortableServer.POA;

import com.dawidweiss.dyna.corba.ICPlayerControllerAdapter;
import com.dawidweiss.dyna.corba.ICPlayerFactory;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerHelper;

/**
 * @author Asia
 */
public class MyPlayerFactoryAdapter implements ICPlayerFactory {

	private MyPlayer player;

	@Override
	public ICPlayerController getController(String playerName, POA poa) {
		try {
			player = new MyPlayer(playerName);
			return ICPlayerControllerHelper.narrow(poa
					.servant_to_reference(new ICPlayerControllerAdapter(player)));
		} catch (UserException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDefaultPlayerName() {
		return player.getName();
	}

	@Override
	public String getVendorName() {
		return player.getName();
	}

}
