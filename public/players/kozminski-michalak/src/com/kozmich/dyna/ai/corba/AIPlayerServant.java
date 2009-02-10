package com.kozmich.dyna.ai.corba;

import java.util.List;

import org.apache.log4j.Logger;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.corba.Adapters;
import com.dawidweiss.dyna.corba.bindings.CBoardInfo;
import com.dawidweiss.dyna.corba.bindings.CControllerState;
import com.dawidweiss.dyna.corba.bindings.CGameEvent;
import com.dawidweiss.dyna.corba.bindings.CGameResult;
import com.dawidweiss.dyna.corba.bindings.CPlayer;
import com.dawidweiss.dyna.corba.bindings.ICControllerCallback;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerPOA;
import com.kozmich.dyna.ai.AiPlayer;

/**
 * 
 * @author Lukasz Kozminski
 * @author Tomasz Michalak
 *
 */
public class AIPlayerServant extends ICPlayerControllerPOA {

	private final static Logger logger = Logger.getLogger("AIPlayer.AIPlayerController");

	private final AiPlayer player;

	private POA poa;

	private CBoardInfo boardInfo;

	private CPlayer[] cPlayers;

	private ICControllerCallback remoteCallback;

	private final Object semRemoteController = new Object();

	public AIPlayerServant(String name, POA poa) {
		this.player = new AiPlayer(name);
		this.poa = poa;
	}

	@Override
	public synchronized void onControllerSetup(ICControllerCallback callback) {
		synchronized (semRemoteController) { 
			this.remoteCallback = callback;
		}
	}

	@Override
	public synchronized void onEnd(CGameResult gameResult) {
		try {
			poa.deactivate_object(poa.servant_to_id(this));
		} catch (ObjectNotActive e) {
			logger.error("POA.deactivate_object " + e.getMessage());
		} catch (WrongPolicy e) {
			logger.error("POA.deactivate_object " + e.getMessage());
		} catch (ServantNotActive e) {
			logger.error("POA.deactivate_object " + e.getMessage());
		}
	}

	@Override
	public synchronized void onFrame(int frame, CGameEvent[] events) {
		final List<GameEvent> adapted = Adapters.adapt(events, boardInfo, cPlayers);
		player.onFrame(frame, adapted);
		final CControllerState state = new CControllerState(Adapters.adapt(player.getCurrent()), player.dropsBomb());
		remoteCallback.update(state);
	}

	@Override
	public synchronized void onStart(CBoardInfo cBoardInfo, CPlayer[] cPlayer) {
		this.boardInfo = cBoardInfo;
		this.cPlayers = cPlayer;
	}

}
