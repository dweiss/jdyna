package AIplayer.ai.in71391;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import AIplayer.model.PureController;

import com.dawidweiss.dyna.corba.Adapters;
import com.dawidweiss.dyna.corba.bindings.ICControllerCallback;

/**
 * Class responsible for communication with Game controller through Corba
 * 
 * @author Lukasz Witkowski
 * 
 */
public class ControllerThread extends Thread {

	private final static Logger logger = Logger.getLogger("ControllerThread");

	private Object semRemoteControler;

	/** Information from aiThread passing change in controller */
	private LinkedBlockingQueue<PureController> contollerQueue;

	private ICControllerCallback remoteController;

	/** list of decision made by AI thread */
	ArrayList<PureController> list = new ArrayList<PureController>();

	private Object last;
	
	/** Variable to stop the thread */
	private volatile boolean threadDone = false;

	public ControllerThread(LinkedBlockingQueue<PureController> contollerQueue,
			Object semRemoteControler) {
		this.contollerQueue = contollerQueue;
		this.semRemoteControler = semRemoteControler;
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
					PureController state_0 = contollerQueue.poll(5,
							TimeUnit.SECONDS);
					contollerQueue.drainTo(list);
					if (state_0 != null)
						list.add(0, state_0);
				}
				if (list.size() > 0) {
					sendControllerInfo();
				} else {
					if (!threadDone) logger.info("No Corba Message received");
					break;
				}
			}
		} catch (InterruptedException e) {
			logger.error("ControllerThread - interrupted "
					+ e.getMessage());
		}
	}

	private void sendControllerInfo() {
		final ControllerState now = new ControllerState(list
				.get(list.size() - 1));
		if (last == null || !last.equals(now)) {
			last = now;
			synchronized (this.semRemoteControler) {
				remoteController.update(Adapters.adapt(list
						.get(list.size() - 1)));
			}
		}
	}

	public void setRemoteController(ICControllerCallback remoteController) {
		this.remoteController = remoteController;
	}

}
