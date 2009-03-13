package com.rychlicki.wilczak.dyna.corba;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.jdyna.IPlayerController;

import com.dawidweiss.dyna.corba.Adapters;
import com.dawidweiss.dyna.corba.bindings.ICControllerCallback;

/**
 * 
 * Sends messages to server.
 * 
 */
public class SenderThread extends Thread {

	private final static Logger logger = Logger.getLogger("ControllerThread");

	/**
	 * Remote controller callback.
	 */
	ICControllerCallback remoteController;

	/**
	 * Queue with moves.
	 */
	LinkedBlockingQueue<IPlayerController> senderQueue;

	/**
	 * Previous move.
	 */
	IPlayerController lastMove;

	public SenderThread(LinkedBlockingQueue<IPlayerController> senderQueue) {
		this.senderQueue = senderQueue;
		setName("SenderThread");
	}

	public void setRemoteController(ICControllerCallback remoteController) {
		this.remoteController = remoteController;
	}

	@Override
	public void run() {
		try {
			while (!isInterrupted()) {
				IPlayerController move = senderQueue.take();
				if (lastMove == null
						|| !(move.getCurrent() == lastMove.getCurrent() && move
								.dropsBomb() == lastMove.dropsBomb()))
					lastMove = move;
				remoteController.update(Adapters.adapt(move));
			}
		} catch (InterruptedException e) {
			logger.info("SenderThread interrupted.");
		}
	}

}
