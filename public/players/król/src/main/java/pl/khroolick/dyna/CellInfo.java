package pl.khroolick.dyna;

import static org.jdyna.CellType.*;

import org.jdyna.CellType;


public class CellInfo {
	private int timeToExplosion;
	protected CellType type;

	public enum ChangeEvent {
		NONE, BOMB_PLANTED, BONUS_APPEAR, BONUS_DISAPPEAR;
	}

	public CellInfo() {
		timeToExplosion = Integer.MAX_VALUE;
		type = CellType.CELL_EMPTY;
	}

	public CellInfo(int initialSafePeriod) {
		timeToExplosion = initialSafePeriod;
	}

	public int getTimeToExplosion() {
		return timeToExplosion;
	}

	public void setTimeToExplosion(int time) {
	    if (type != CELL_WALL && timeToExplosion > time)
			timeToExplosion = time;
	}

	public void updateTimeToExplosion(int delta) {
		if (timeToExplosion < Integer.MAX_VALUE) {
			timeToExplosion -= delta;
		}
		if(timeToExplosion < -15){
		    timeToExplosion = Integer.MAX_VALUE;
		}
	}

	public ChangeEvent updateState(CellType nextType) {
		//
		// check for events
		//
		if (nextType != type) {
			try {
			    //
		        // reset timeToExplosion if necessary
		        //
		        if (!nextType.isLethal() && type.isLethal() && timeToExplosion < 
		            0) {
		            timeToExplosion = Integer.MAX_VALUE;
		        }
				if (nextType == CELL_BOMB && type != CELL_BOMB) {
					return ChangeEvent.BOMB_PLANTED;
				}
				if ((nextType == CELL_BONUS_BOMB && type != CELL_BONUS_BOMB )||
				    (nextType == CELL_BONUS_RANGE && type != CELL_BONUS_RANGE)){
					return ChangeEvent.BONUS_APPEAR;
				}
				if ((nextType != CELL_BONUS_BOMB && type == CELL_BONUS_BOMB)|| 
				    (nextType != CELL_BONUS_RANGE && type == CELL_BONUS_RANGE)){
					return ChangeEvent.BONUS_DISAPPEAR;
				}
			} finally {
				type = nextType;
			}
		}
		return ChangeEvent.NONE;

	}

	@Override
	public String toString() {
		if (type.isWalkable()) {
			return timeToExplosion == Integer.MAX_VALUE ? " X" : Integer
					.toString(timeToExplosion);
		} else {
			return " ";
		}
	}
}
