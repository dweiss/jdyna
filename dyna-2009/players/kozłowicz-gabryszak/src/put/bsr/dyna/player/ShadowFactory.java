/**
 * 
 */
package put.bsr.dyna.player;


import org.jdyna.IPlayerController;
import org.jdyna.IPlayerFactory;

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
