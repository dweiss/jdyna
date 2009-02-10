package AIplayer.com.inf71391.dyna.client;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.*;

import AIplayer.ai.in71391.*;
import AIplayer.model.*;
import AIplayer.model.PlayerInfo;

import com.dawidweiss.dyna.*;
import com.dawidweiss.dyna.corba.Adapters;
import com.dawidweiss.dyna.corba.bindings.*;

/**
 * An player taking part in a remote game. Class based on class made by Dawid
 * Weiss.
 * 
 * @author Lukasz Witkowski
 */
public class AIPlayerServant extends ICPlayerControllerPOA {
	
	private final static Logger logger = Logger.getLogger("AI.AIPlayerServant");

	private final Object semRemoteController = new Object();

	public static String myName;

	/**
	 * Information from playerServant about a frame change is passing through
	 * this queue
	 */
	private LinkedBlockingQueue<GameStateInformation> frameGameInfo;

	/** Information to aiThread is passing through this queue */
	private LinkedBlockingQueue<BoardInformationClass> aiGameInfo;

	/** Information from aiThread passing change in controller */
	private LinkedBlockingQueue<PureController> contollerQueue;

	/** Thread for trace players */
	private PlayerContollerThread playerThread;

	/** Decision thread for AI */
	private AIThread aiThread;

	/** Controller thread */
	private ControllerThread controllerThread;

	/**
	 * Remote controller callback. We pump events from {@link #localController}
	 * to this object.
	 */
	private ICControllerCallback remoteController;

	/* List of players */
	private CPlayer[] players;

	/* Information about board */
	private CBoardInfo boardInfo;
	
	private POA poa;

	/*
	 * Constructor of class AIPlayerServant. Initializing variables, queues to
	 * pass messages, and threads managing artificial intelligence.
	 */
	public AIPlayerServant(String playerName,POA poa) {
		this.poa = poa;
		AIPlayerServant.myName = playerName;
		this.frameGameInfo = new LinkedBlockingQueue<GameStateInformation>();
		this.aiGameInfo = new LinkedBlockingQueue<BoardInformationClass>();
		this.contollerQueue = new LinkedBlockingQueue<PureController>();
		this.playerThread = new PlayerContollerThread(this.frameGameInfo,
				this.aiGameInfo);
		this.controllerThread = new ControllerThread(this.contollerQueue,
				semRemoteController);
		this.aiThread = new AIThread(playerName,this.aiGameInfo, this.contollerQueue);
	}
	
	/*
	 * Constructor of class AIPlayerServant. Initializing variables, queues to
	 * pass messages, and threads managing artificial intelligence.
	 */
	public AIPlayerServant(POA poa) {
		this.poa = poa;
		AIPlayerServant.myName = "BezwglednyBrutal";
		this.frameGameInfo = new LinkedBlockingQueue<GameStateInformation>();
		this.aiGameInfo = new LinkedBlockingQueue<BoardInformationClass>();
		this.contollerQueue = new LinkedBlockingQueue<PureController>();
		this.playerThread = new PlayerContollerThread(this.frameGameInfo,
				this.aiGameInfo);
		this.controllerThread = new ControllerThread(this.contollerQueue,
				semRemoteController);
		this.aiThread = new AIThread("BezwglednyBrutal",this.aiGameInfo, this.contollerQueue);
	}

	/*
	 * Method invoke when new game should be started.
	 */
	public synchronized void onStart(CBoardInfo boardInfo, CPlayer[] players) {

		this.players = players;
		this.boardInfo = boardInfo;
		logger.info("Game started.");
	}

	/*
	 * New event during the game.
	 */
	public synchronized void onFrame(int frame, CGameEvent[] events) {
		/*
		 * Update local views.
		 */
		final List<GameEvent> adapted = Adapters.adapt(events, boardInfo,
				players);

		for (int i = 0; i < adapted.size(); i++) {
			if (adapted.get(i).type.equals(GameEvent.Type.GAME_START)) {
				logger.info("GAME_START");
				aiThread.start();
				playerThread.start();
				controllerThread.setRemoteController(this.remoteController);
				controllerThread.start();
				logger.info("GAME_START - threads");
			} else if (adapted.get(i).type.equals(GameEvent.Type.GAME_OVER)) {
				logger.info("GAME_OVER");
				aiThread.done();
				playerThread.done();
				controllerThread.done();
				setVariables();
				logger.info("GAME_OVER - threads");
			} else if (adapted.get(i).type.equals(GameEvent.Type.SOUND_EFFECT)) {
			} else if (adapted.get(i).type.equals(GameEvent.Type.GAME_STATE)) {
				logger.debug("Get frame " + frame);
				GameStateEvent gs = (GameStateEvent) adapted.get(i);
				GameStateInformation gSI = new GameStateInformation(gs
						.getCells(), frame);
				List<? extends IPlayerSprite> list = gs.getPlayers();
				for (int t = 0; t < list.size(); t++) {
					PlayerInfo pI = new PlayerInfo(list.get(t).getName(), 0, 0,
							list.get(t).getPosition().x, list.get(t)
									.getPosition().y);
					pI.setDead(list.get(t).isDead());
					pI.setImmortal(list.get(t).isImmortal());
					gSI.players.add(pI);
				}
				try {
					frameGameInfo.put(gSI);
				} catch (InterruptedException e) {
					e.printStackTrace();
					logger.error("Interrupted add to frameGameInfo "
							+ e.getMessage());
				}
			} else
				logger.debug("Unknow type of frame");
		}

	}

	private void setVariables() {
		this.frameGameInfo = new LinkedBlockingQueue<GameStateInformation>();
		this.aiGameInfo = new LinkedBlockingQueue<BoardInformationClass>();
		this.contollerQueue = new LinkedBlockingQueue<PureController>();
		this.playerThread = new PlayerContollerThread(this.frameGameInfo,
				this.aiGameInfo);
		this.controllerThread = new ControllerThread(this.contollerQueue,
				semRemoteController);
		this.aiThread = new AIThread(AIPlayerServant.myName,this.aiGameInfo, this.contollerQueue);	
	}

	/*
	 * End of the game
	 */
	public synchronized void onEnd(CGameResult result) {
		try {
			poa.deactivate_object(poa.servant_to_id(this));
		} catch (ObjectNotActive e) {
			logger.error("POA deactivation "
					+ e.getMessage());
		} catch (WrongPolicy e) {
			logger.error("POA deactivation "
					+ e.getMessage());
		} catch (ServantNotActive e) {
			logger.error("POA deactivation "
					+ e.getMessage());
		}
		logger.info("Game finished .");
	}

	/*
	 * 
	 */
	public synchronized void onControllerSetup(ICControllerCallback callback) {
		synchronized (semRemoteController) {
			this.remoteController = callback;
		}
	}
}
