package com.luczak.lykowski.dyna.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

import org.jdyna.*;
import org.omg.PortableServer.POAPackage.*;

import com.dawidweiss.dyna.corba.Adapters;
import com.dawidweiss.dyna.corba.bindings.*;

/**
 * 
 * Main class which controls the communication with corba and choose the
 * attitude of our artificial intelligence player
 * 
 * @author Konrad Łykowski
 * 
 */
public class AiPlayerServant extends ICPlayerControllerPOA {

	private final static Logger logger = Logger.getLogger("AiPlayerServant");
	/*
	 * Player name
	 */
	private final String playerName;
	/*
	 * Board Informations
	 */
	private CBoardInfo cBoardInfo;
	/*
	 * Players Informations
	 */
	private CPlayer cPlayers[];
	/*
	 * Call back to controller
	 */
	private ICControllerCallback callback;
	/*
	 * Queue used to communicate with AI thread
	 */
	private LinkedBlockingDeque<OnFrameInformation> onFrameInfoQueue;
	/*
	 * Queue used to communicate with corba
	 */
	private LinkedBlockingDeque<IPlayerController> aiPlayerContollerQueue;
	/*
	 * Object of AiPlayerControllerThread class
	 */
	private AiPlayerControllerThread aiPlayerControllerThread;
	/*
	 * Thread which use AiPlayerModel to make a prediction
	 */
	private AiPlayerThread aiPlayerThread;
	/*
	 * Supervised safe work on callback object
	 */
	private Object callbackMonitor = new Object();

	/**
	 * Constructor for this class
	 * 
	 * @param playerName
	 */
	public AiPlayerServant(String name) {
		this.playerName = name;

		this.onFrameInfoQueue = new LinkedBlockingDeque<OnFrameInformation>();
		this.aiPlayerContollerQueue = new LinkedBlockingDeque<IPlayerController>();

		this.aiPlayerControllerThread = new AiPlayerControllerThread(
				this.aiPlayerContollerQueue, callbackMonitor);

		this.aiPlayerThread = new AiPlayerThread(playerName,
				this.onFrameInfoQueue, this.aiPlayerContollerQueue);
	}

	/**
	 * Invoked by the game controller just before the game starts. The player's
	 * controller should pass state information to the provided callback object
	 * and <b>must not</b> flood it with requests. It is advised to have one
	 * call every time the state needs to be changed.
	 */
	@Override
	public synchronized void onControllerSetup(ICControllerCallback callback) {
		synchronized (callbackMonitor) {
			this.callback = callback;
		}
	}

	/**
	 * Invoked by the controller each time when corba receive frame from a
	 * remote server with game result
	 */
	@Override
	public synchronized void onEnd(CGameResult result) {
		try {
			this._poa().deactivate_object(this._poa().servant_to_id(this));
		} catch (ObjectNotActive e) {
			logger.warning("Object not active");
		} catch (WrongPolicy e) {
			logger.warning("Wrong policy");
		} catch (ServantNotActive e) {
			logger.warning("Servant not active");
		}
	}

	/**
	 * Invoked by the controller each time when corba receive frame from a
	 * remote server
	 */
	@Override
	public synchronized void onFrame(int frame, CGameEvent[] events) {
		List<GameEvent> gameEvents = Adapters.adapt(events, cBoardInfo,
				cPlayers);
		for (int i = 0; i < gameEvents.size(); i++) {
			if (gameEvents.get(i).type.equals(GameEvent.Type.GAME_STATE)) {
				GameStateEvent gameState = (GameStateEvent) gameEvents.get(i);

				Cell[][] boardCells = gameState.getCells();
				Cell[][] clonedCells = new Cell[boardCells.length][boardCells[0].length];
				for (int ii = 0; ii < boardCells.length; ii++) {
					for (int j = 0; j < boardCells[ii].length; j++) {
						clonedCells[ii][j] = boardCells[ii][j];
					}
				}
				List<? extends IPlayerSprite> list = gameState.getPlayers();
				ArrayList<InitialAiPlayerInformation> clonedList = new ArrayList<InitialAiPlayerInformation>();
				for (int t = 0; t < list.size(); t++) {
					clonedList.add(new InitialAiPlayerInformation(list.get(t)
							.getName(), list.get(t).getPosition(), list.get(t)
							.isDead()));
				}
				OnFrameInformation info = new OnFrameInformation(clonedCells,
						clonedList);

				try {
					onFrameInfoQueue.clear();
					onFrameInfoQueue.putLast(info);
				} catch (InterruptedException e) {
				}
			} else if (gameEvents.get(i).type
					.equals(GameEvent.Type.SOUND_EFFECT)) {
			} else if (gameEvents.get(i).type.equals(GameEvent.Type.GAME_START)) {
				aiPlayerThread.start();
				aiPlayerControllerThread.callback = callback;
				aiPlayerControllerThread.start();
			} else if (gameEvents.get(i).type.equals(GameEvent.Type.GAME_OVER)) {
				aiPlayerThread.interrupt();
				aiPlayerControllerThread.interrupt();
			}
		}
	}

	/**
	 * Invoked by a controller on start of the game when CORBA receive frame
	 * with game data from remote server
	 */
	@Override
	public synchronized void onStart(CBoardInfo boardInfo, CPlayer[] players) {
		this.cBoardInfo = boardInfo;
		this.cPlayers = players;
	}

}
