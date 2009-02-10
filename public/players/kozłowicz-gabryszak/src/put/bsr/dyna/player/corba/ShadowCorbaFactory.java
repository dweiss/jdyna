/**
 * 
 */
package put.bsr.dyna.player.corba;

import org.omg.PortableServer.POA;

import com.dawidweiss.dyna.IPlayerFactory;
import com.dawidweiss.dyna.corba.CPlayerFactoryAdapter;
import com.dawidweiss.dyna.corba.ICPlayerFactory;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;

/**
 * @author marcin
 * 
 */
public class ShadowCorbaFactory implements ICPlayerFactory {

	private final CPlayerFactoryAdapter delegate;

	public ShadowCorbaFactory(IPlayerFactory factory) {
		delegate = new CPlayerFactoryAdapter(factory);
	}

	@Override
	public ICPlayerController getController(String playerName, POA poa) {
		return delegate.getController(playerName, poa);
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
