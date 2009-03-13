package AIplayer.com.inf71391.dyna.local;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.jdyna.*;

import AIplayer.ai.in71391.AIThread;
import AIplayer.ai.in71391.PlayerContollerThread;
import AIplayer.model.BoardInformationClass;
import AIplayer.model.GameStateInformation;
import AIplayer.model.PlayerInfo;
import AIplayer.model.PureController;


public class AIPlayer implements IPlayerController, IGameEventListener,
		IPlayerController2 {

	private final static Logger logger = Logger.getLogger("AI.AIPlayer");

	private String myName;
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
	
	/** Program decision about direction of move */
	private volatile PureController pC = new PureController(false, null);

	/*
	 * Constructor of class AIPlayerServant. Initializing variables, queues to
	 * pass messages, and threads managing artificial intelligence.
	 */
	public AIPlayer(String playerName) {
		this.myName = playerName;
		this.frameGameInfo = new LinkedBlockingQueue<GameStateInformation>();
		this.aiGameInfo = new LinkedBlockingQueue<BoardInformationClass>();
		this.contollerQueue = new LinkedBlockingQueue<PureController>();
		this.playerThread = new PlayerContollerThread(this.frameGameInfo,
				this.aiGameInfo);
		this.controllerThread = new ControllerThread(this.contollerQueue, this);
		this.aiThread = new AIThread(playerName, this.aiGameInfo,
				this.contollerQueue);
	}

	public void setPC(PureController pC) {
		this.pC = pC;
	}

	

	@Override
	public boolean dropsBomb() {
		return pC.dropsBomb();
	}

	@Override
	public Direction getCurrent() {
		return pC.getCurrent();
	}

	@Override
	public void onFrame(int frame, List<? extends GameEvent> events) {

		/*
		 * Update local views.
		 */
		final List<? extends GameEvent> adapted = events;

		for (int i = 0; i < adapted.size(); i++) {
			if (adapted.get(i).type.equals(GameEvent.Type.GAME_START)) {
				logger.info("GAME_START");
				aiThread.start();
				playerThread.start();
				controllerThread.start();
				logger.info("GAME_START - threads");
			} else if (adapted.get(i).type.equals(GameEvent.Type.GAME_OVER)) {
				logger.info("GAME_OVER");
				aiThread.done();
				playerThread.done();
				controllerThread.done();
				this.playerThread = new PlayerContollerThread(
						this.frameGameInfo, this.aiGameInfo);
				this.controllerThread = new ControllerThread(
						this.contollerQueue, this);
				this.aiThread = new AIThread(myName, this.aiGameInfo,
						this.contollerQueue);
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
				logger.info("Unknown type of frame");
		}

	}

	@Override
	public ControllerState getState() {
		return new ControllerState(pC.getCurrent(), pC.dropsBomb(), 4);
	}

}
