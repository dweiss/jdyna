package AIplayer.com.inf71391.dyna.local;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import AIplayer.model.PureController;

/**
 * Class responsible for communication with Game controller
 * 
 * @author Lukasz Witkowski
 * 
 */
public class ControllerThread extends Thread {

	private final static Logger logger = Logger.getLogger("ControllerThread");

	/** Information from aiThread passing change in controller */
	private LinkedBlockingQueue<PureController> contollerQueue;

	/** list of decision made by AI thread */
	ArrayList<PureController> list = new ArrayList<PureController>();

	private AIPlayer aiPlayer;

	/** Variable to stop the thread */
	private volatile boolean threadDone = false;

	public ControllerThread(LinkedBlockingQueue<PureController> contollerQueue,
			AIPlayer pC) {
		this.aiPlayer = pC;
		this.contollerQueue = contollerQueue;
	}

	public void done() {
		threadDone = true;
	}

	public void run() {
		try {
			while (!threadDone) {
				list.clear();
				// take out form queue
				int number = contollerQueue.drainTo(list);
				if (number == 0) {
					PureController state_0 = contollerQueue.poll(15,
							TimeUnit.SECONDS);
					contollerQueue.drainTo(list);
					if (state_0 != null)
						list.add(0, state_0);
				}
				if (list.size() > 0) {
					aiPlayer.setPC(list.get(list.size() - 1));
				} else {
					logger.error("No Corba Message received");
					break;
				}
			}
		} catch (InterruptedException e) {
			logger.info("GAME_OVER " + e.getMessage());
		}
	}

}
