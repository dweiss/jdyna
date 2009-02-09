/**
 * 
 */
package put.bsr.dyna.player;


import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerFactory;

/**
 * 
 * @author marcin
 *
 */
public class ShadowFactory implements IPlayerFactory{

	@Override
	public IPlayerController getController(String playerName) {
		return new Shadow(playerName);
	}

	@Override
	public String getDefaultPlayerName() {
        return Shadow.DEFAULT_NAME;
	}

	@Override
	public String getVendorName() {
		return "Marcin Kozłowicz & Piotr Gabryszak";
	}

	
}
