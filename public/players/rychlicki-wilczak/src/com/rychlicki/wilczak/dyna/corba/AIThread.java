package com.rychlicki.wilczak.dyna.corba;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.jdyna.*;

import com.google.common.collect.Lists;
import com.rychlicki.wilczak.dyna.player.SmarterRabbit;

/**
 * Passes messages to/from local AI player. 
 *
 */
public class AIThread extends Thread {

	private final static Logger logger = Logger.getLogger("AIThread");

	/**
	 * Queue with game events.
	 */
	private LinkedBlockingQueue<GameStateEvent> frameGameInfo;

	
	/**
	 * Queue with moves.
	 */
	private LinkedBlockingQueue<IPlayerController> senderQueue;

	
	/**
	 * AI player.
	 */
	private SmarterRabbit rabbit;

	public AIThread(LinkedBlockingQueue<GameStateEvent> frameGameInfo,
			LinkedBlockingQueue<IPlayerController> senderQueue, String name) {
		this.frameGameInfo = frameGameInfo;
		this.senderQueue = senderQueue;
		rabbit = new SmarterRabbit(name);
		setName("AIThread");
	}

	@Override
	public void run() {

		try {
			int frame = 1;
			while (!isInterrupted()) {
				GameEvent event = frameGameInfo.take();
				rabbit.onFrame(frame, Lists.newArrayList(event));
				senderQueue.add(new IPlayerController() {
					public Direction getCurrent() {
						return rabbit.getCurrent();
					}

					@Override
					public boolean dropsBomb() {
						return rabbit.dropsBomb();
					}

				});
			}
		} catch (InterruptedException e) {
			logger.info("AIThread interrupted!");
		}
	}

	public void setStartEvent(GameEvent gameEvent) {
		rabbit.onFrame(0, Lists.newArrayList(gameEvent));
	}
	
	public void setEndEvent(GameEvent gameEvent) {
		frameGameInfo.clear();
		senderQueue.clear();
		rabbit.onFrame(0, Lists.newArrayList(gameEvent));
	}

}
