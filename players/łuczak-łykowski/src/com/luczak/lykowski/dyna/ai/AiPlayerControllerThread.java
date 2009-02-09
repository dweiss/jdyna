package com.luczak.lykowski.dyna.ai;

import java.util.concurrent.LinkedBlockingDeque;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.corba.Adapters;
import com.dawidweiss.dyna.corba.bindings.ICControllerCallback;

/**
 * 
 * Thread used queue to communicate with corba
 * 
 * @author Konrad Łykowski
 * 
 */
public class AiPlayerControllerThread extends Thread {
	/*
	 * Queue used to sending information by corba
	 */
	private LinkedBlockingDeque<IPlayerController> aiPlayercontollerQueue;
	/*
	 * To make thread safe operation on ICControllerCallback
	 */
	private Object callbackMonitor;
	/*
	 * Call back Controller
	 */
	public ICControllerCallback callback;

	/**
	 * Constructor
	 * 
	 * @param aiPlayercontollerQueue
	 * @param callbackMonitor
	 */
	public AiPlayerControllerThread(
			LinkedBlockingDeque<IPlayerController> aiPlayercontollerQueue,
			Object callbackMonitor) {
		this.aiPlayercontollerQueue = aiPlayercontollerQueue;
		this.callbackMonitor = callbackMonitor;
	}

	/**
	 * Main method for this class it sends information about behavior of our
	 * players by corba
	 */
	@Override
	public void run() {
		while (true) {
			try {
				IPlayerController temp = aiPlayercontollerQueue.takeLast();
				synchronized (callbackMonitor) {
					callback.update(Adapters.adapt(temp));
				}
			} catch (InterruptedException e) {
			}
		}
	}
}
