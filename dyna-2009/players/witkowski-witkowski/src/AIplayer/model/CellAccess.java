package AIplayer.model;

/**
 * Represent information of accessibility of an cell up to time when all bombs
 * will blow up.
 * 
 * @author Marcin Witkowski
 * 
 */
public class CellAccess {
	/* Info whether this cell is inaccessible in specific time */
	private boolean[] inaccessible;

	public CellAccess(int max) {
		inaccessible = new boolean[max];
		for (int i = 0; i < max; i++)
			inaccessible[i] = false;
	}

	/**
	 * Setting cell as inaccessible during all the time
	 */
	public void alwaysProhibitAcces() {
		for (int i = 0; i < inaccessible.length; i++) {
			inaccessible[i] = true;
		}
	}

	/**
	 * Setting cell as accessible from a specific point of time
	 */
	public void setAccesibleFrom(int time) {
		for (int i = time; i < inaccessible.length; i++) {
			inaccessible[i] = false;
		}
	}

	/**
	 * Setting cell as accessible during a specific time interval
	 */
	public void setAccesiblePeriod(int time, int period) {
		for (int i = 0; i < period; i++) {
			inaccessible[time + i] = false;
		}
	}

	/**
	 * Setting cell as inaccessible during a specific time interval
	 */
	public void prohibitAcces(int time, int period) {
		for (int i = 0; i < period; i++) {
			inaccessible[time + i] = true;
		}
	}

	/**
	 * Check whether cell is inaccessible in a specific point of time
	 */
	public boolean isInaccessible(int time) {
		if (time >= inaccessible.length)
			return inaccessible[inaccessible.length - 1];
		return inaccessible[time];
	}

	/**
	 * Check whether cell is inaccessible during a specific time interval
	 */
	public boolean isInaccessiblePeriod(int time, int period) {
		if (time >= inaccessible.length)
			return inaccessible[inaccessible.length - 1];
		boolean is = false;
		for (int i = time; i <= time + period; i++) {
			if (i >= inaccessible.length)
				return is;
			if (inaccessible[i])
				is = true;
		}
		return is;
	}
}
