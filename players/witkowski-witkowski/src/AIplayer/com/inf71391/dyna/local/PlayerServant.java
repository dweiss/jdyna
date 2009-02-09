package AIplayer.com.inf71391.dyna.local;

import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerFactory;

public class PlayerServant implements IPlayerFactory{

	@Override
	public IPlayerController getController(String playerName) {
		return new AIPlayer(playerName);
	}

	@Override
	public String getDefaultPlayerName() {
		return "BezwzglednyBrutal";
	}

	@Override
	public String getVendorName() {
		return "Lukasz Witkowski, Marcin Witkowski";
	}

}
