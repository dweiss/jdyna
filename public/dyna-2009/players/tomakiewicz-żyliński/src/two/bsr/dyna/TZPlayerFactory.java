package two.bsr.dyna;

import org.jdyna.IPlayerController;
import org.jdyna.IPlayerFactory;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import com.dawidweiss.dyna.corba.ICPlayerFactory;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerHelper;


public class TZPlayerFactory implements ICPlayerFactory, IPlayerFactory {

	@Override
	public ICPlayerController getController(String playerName, POA poa) {

		ICPlayerController aiController = null;

		TZPlayerServant aiServant = new TZPlayerServant(playerName);
		try {
			aiController = ICPlayerControllerHelper.narrow(poa.servant_to_reference(aiServant));
		} catch (ServantNotActive e) {
			throw new RuntimeException();
		} catch (WrongPolicy e) {
			throw new RuntimeException();
		}

		return aiController;
	}

	@Override
	public String getDefaultPlayerName() {
		return "T&Z";
	}

	@Override
	public String getVendorName() {
		return "Tomankiewicz-Żyliński";
	}

    @Override
    public IPlayerController getController(String playerName)
    {
        return new TZPlayer(playerName);
    }
}
