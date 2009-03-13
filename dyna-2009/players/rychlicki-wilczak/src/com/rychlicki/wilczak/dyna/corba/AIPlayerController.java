package com.rychlicki.wilczak.dyna.corba;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.jdyna.*;
import org.omg.CORBA.UserException;
import org.omg.PortableServer.POA;

import com.dawidweiss.dyna.corba.Adapters;
import com.dawidweiss.dyna.corba.bindings.CBoardInfo;
import com.dawidweiss.dyna.corba.bindings.CGameEvent;
import com.dawidweiss.dyna.corba.bindings.CGameResult;
import com.dawidweiss.dyna.corba.bindings.CPlayer;
import com.dawidweiss.dyna.corba.bindings.ICControllerCallback;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerPOA;

/**
 * 
 * Main controller.
 * 
 */
public class AIPlayerController extends ICPlayerControllerPOA {

	private final static Logger logger = Logger.getLogger("AIPlayerController");

	/**
	 * Queue with game events.
	 */
	private LinkedBlockingQueue<GameStateEvent> frameGameInfo;

	/**
	 * Queue with moves.
	 */
	private LinkedBlockingQueue<IPlayerController> senderQueue;

	/**
	 * AI thread.
	 */
	private AIThread aiThread;

	/**
	 * Controller thread.
	 * 
	 */
	private SenderThread senderThread;

	/**
	 * Remote controller callback.
	 */
	private ICControllerCallback remoteController;

	/**
	 * Players table.
	 */
	private CPlayer[] players;

	/**
	 * BoardInfo
	 */
	private CBoardInfo boardInfo;

	/**
	 * Poa reference from ICPlayerFactory.
	 */
	private POA poa;

	public AIPlayerController(String name) {
		this.frameGameInfo = new LinkedBlockingQueue<GameStateEvent>();
		this.senderQueue = new LinkedBlockingQueue<IPlayerController>();
		this.senderThread = new SenderThread(this.senderQueue);
		this.aiThread = new AIThread(this.frameGameInfo, this.senderQueue, name);
	}

	public AIPlayerController(String name, POA poa) {
		this(name);
		this.poa = poa;
	}

	@Override
	public synchronized void onStart(CBoardInfo boardInfo, CPlayer[] players) {
		this.players = players;
		this.boardInfo = boardInfo;

		aiThread.start();
		senderThread.setRemoteController(this.remoteController);
		senderThread.start();
		logger.info("Game started.");
	}

	@Override
	public synchronized void onFrame(int frame, CGameEvent[] events) {
		final List<GameEvent> adapted = Adapters.adapt(events, boardInfo,
				players);

		for (GameEvent gameEvent : adapted) {
			switch (gameEvent.type) {
			case GAME_START:
				gameStartEvent(gameEvent);
				break;
			case GAME_STATE:
				gameStateEvent(gameEvent);
				break;
			case SOUND_EFFECT:
				// skip sound effect
				break;
			case GAME_OVER:
				gameOverEvent(gameEvent);
				break;
			default:
				logger.debug("Unsuported event type " + gameEvent.type);
			}
		}
	}

	private void gameStartEvent(GameEvent gameEvent) {
		logger.debug("game start event");
		aiThread.setStartEvent(gameEvent);
	}

	private void gameStateEvent(GameEvent gameEvent) {
		try {
			frameGameInfo.put((GameStateEvent) gameEvent);
		} catch (InterruptedException e) {
			logger.error("Interrupted add to frameGameInfo " + e.getMessage());
		}

	}

	private void gameOverEvent(GameEvent gameEvent) {
		logger.debug("game over event");
		aiThread.setEndEvent(gameEvent);
	}

	@Override
	public synchronized void onEnd(CGameResult result) {
		logger.debug("on End");

		if (senderThread.isAlive()) {
			senderThread.interrupt();
			try {
				senderThread.join();
			} catch (InterruptedException e) {
				// just leave
			}
		}
		
		if (aiThread.isAlive()) {
			aiThread.interrupt();
			try {
				aiThread.join();
			} catch (InterruptedException e) {
				// just leave
			}
		}

		if (poa != null) {
			try {
				poa.deactivate_object(poa.servant_to_id(this));
			} catch (UserException e) {
				// problem with corba - inform about this
				throw new RuntimeException(e);
			}
		}
		logger.info("Game finished.");
	}

	@Override
	public synchronized void onControllerSetup(ICControllerCallback callback) {
		this.remoteController = callback;
	}
}
