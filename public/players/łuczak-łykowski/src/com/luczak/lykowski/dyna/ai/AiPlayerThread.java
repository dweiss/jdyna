package com.luczak.lykowski.dyna.ai;

import java.util.concurrent.LinkedBlockingDeque;

import org.jdyna.IPlayerController;

/**
 * 
 * Thread responsible for input IPlayerControler objects into linked
 * blockingDeque called aiPlayerControlerQueue
 * 
 * @author Konrad Łykowski
 * 
 */
public class AiPlayerThread extends Thread {

	/*
	 * Queue with frame information
	 */
	private LinkedBlockingDeque<OnFrameInformation> onFrameInfoQueue;
	/*
	 * Queue with values send by corba
	 */
	private LinkedBlockingDeque<IPlayerController> aiPlayerContollerQueue;
	/*
	 * AI player
	 */
	private AiPlayerModel aiModel;

	/*
	 * Constructor
	 */
	public AiPlayerThread(String name,
			LinkedBlockingDeque<OnFrameInformation> onFrameInfoQueue,
			LinkedBlockingDeque<IPlayerController> aiPlayerContollerQueue) {
		this.onFrameInfoQueue = onFrameInfoQueue;
		this.aiPlayerContollerQueue = aiPlayerContollerQueue;
		aiModel = new AiPlayerModel(name);
	}

	/**
	 * Main method which fills the aiPlayerContoller Queue
	 */
	@Override
	public void run() {
		try {
			while (true) {
				OnFrameInformation event = onFrameInfoQueue.takeLast();
				aiModel.play(event);
				aiPlayerContollerQueue.add(new IPlayerController() {
					public Direction getCurrent() {
						return aiModel.getCurrent();
					}

					@Override
					public boolean dropsBomb() {
						return aiModel.dropsBomb();
					}

				});
			}
		} catch (InterruptedException e) {
		}
	}

}
