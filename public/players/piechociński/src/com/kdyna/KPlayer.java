package com.kdyna;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.Player;
import com.kdyna.gamestate.BonusesInfo;
import com.kdyna.gamestate.CellInfo;
import com.kdyna.gamestate.DynaBoard;
import com.kdyna.gamestate.DynaGameState;
import com.kdyna.gamestate.PlayerInfo;
import com.kdyna.gamestate.DynaGameState.CellPredicate;


/**
 * 
 * SupaHiro, undefeated till first fight :]
 * 
 * @author Krzysztof P
 *
 */
public class KPlayer implements IGameEventListener, IPlayerController{
	

	private enum PlayerState {
		CHASE,						// player chasing for bonus or enemy
		HIDE,						// player hiding from explosion
		CHASE_AND_DIG,				// chasing, but first need to do some demolition
		UNKNOWN						// appears when game state isn't avaialable, simply wait
	};
	
	final private Player player;
	private DynaGameState state;
	private DynaBoard board;
	private PlayerInfo myInfo;
	private PlayerState myState;

	
	public KPlayer(String name) {
		player = new Player(name, this);
	}

	public Player getPlayer() {
		return player;
	}

	public void onFrame(int frame, List<? extends GameEvent> events) {
		for (GameEvent event : events) {
			if (event instanceof GameStateEvent) {
				if (state != null) {
					state.update(frame, (GameStateEvent) event);
				} else {
					state = new DynaGameState(frame, (GameStateEvent) event);
					board = state.getBoard();
					myInfo = state.getPlayer(player.name);
				}
			}
		}
	}
	

	public boolean dropsBomb() {
		if (state != null && myInfo != null) {
			return (canSafelyPlaceBomb() && (enemyInRange() || 
					(myState == PlayerState.CHASE_AND_DIG && isCrateInRange())) );			
		} else {
			return false;
		}
	}


	public Direction getCurrent() {
		
		final Direction direction;
		final Stack<CellInfo> route;
		myState = determineMyState();		
		switch (myState) {
			case HIDE:
				CellInfo myPosition = board.getCell(myInfo.getCoords().x,myInfo.getCoords().y);
				CellInfo nextCell = this.findPlaceToHide(4);
				if (nextCell != null) {
					if (willCrossCellBorder(nextCell) && nextCell.getType().isLethal()) {	// passing between cells & new cell is dangerous
						direction = null;					// wait
					} else {								// still same cell
						direction = board.getDirection(myPosition.x, myPosition.y, nextCell.x, nextCell.y); 
					}
				} else {
					direction = null;
				}
				        
				break;
			case CHASE:
				CellInfo to = findTarget();
				final CellInfo from = board.getCell(myInfo.getCoords().x,myInfo.getCoords().y);
				if ( myState == PlayerState.CHASE_AND_DIG) {
					CellPredicate pred = new CellPredicate() {
						public boolean test(CellInfo ci) {
							return (ci.getType().isWalkable() || (ci.x == from.x && ci.y == from.y)  || ci.getType() == CellType.CELL_CRATE);
						}			
					};					
					route = board.findRoute(from, to, pred);
				} else {
					route = board.findRoute(from, to);
				}
				if (route == null) {
					direction = null;			//if no target found simply wait
				} else  {
					route.pop();				// pop start point
					nextCell = route.pop();		// go to second
					if (willCrossCellBorder(nextCell) &&
						(nextCell.willExplode() || nextCell.getType().isExplosion())) {
						direction = null;					
					} else {
						direction = board.getDirection(from.x, from.y, nextCell.x, nextCell.y); 
					}
				}
				break;
			case UNKNOWN:
				direction = null;
				break;
			default:
				direction = null;
		
		}
		
		return direction;
	}


	private PlayerState determineMyState() {
		final PlayerState myState;
		if (state != null && myInfo != null) {
			if (!isSafe()) {
				myState = PlayerState.HIDE;
			} else {
				myState = PlayerState.CHASE;
			}
		} else {
			myState = PlayerState.UNKNOWN;
		}
		return myState;
	}
	
	/** 
	 * Search for destination cell
	 *
	 */
	private CellInfo findTarget() {
		final CellInfo targetCell;
		final List<CellInfo> reachableBonuses = getReacheableBonuses();
		if (reachableBonuses.size() != 0) {
			targetCell = reachableBonuses.get(0);
		} else {
			PlayerInfo target = getNearestReachableOpponent();
			if (target != null) {
				targetCell = board.getCell(target.getCoords().x, target.getCoords().y);
			} else {
				PlayerInfo targetPlayer = findNearestPlayer();
				if (targetPlayer != null) {
					targetCell = board.getCell(targetPlayer.getCoords().x, targetPlayer.getCoords().y);
					myState = PlayerState.CHASE_AND_DIG;
				} else {
					targetCell = null;
				}
			}			
		}
		return targetCell;
	}

	/**
	 * Find nearest enemy, whether reachable or not,
	 * although players which can't be "made reachable" by detonating crates are igonred 
	 * (assuming this situation won't appear)
	 * 
	 */
	
	private PlayerInfo findNearestPlayer() {
		PlayerInfo result = null;
		final int x = myInfo.getCoords().x;
		final int y = myInfo.getCoords().y;
		
		CellPredicate pred = new CellPredicate() {
			public boolean test(CellInfo ci) {
				return (ci.getType().isWalkable() || (ci.x == x && ci.y == y)  || ci.getType() == CellType.CELL_CRATE);
			}			
		};
		
		int distance = Integer.MAX_VALUE;
		for( PlayerInfo pi : state.getPlayers()) {
			Stack<CellInfo> route = board.findRoute(board.getCell(x, y), board.getCell(pi.getCoords().x,pi.getCoords().y), pred);
			if (route != null && pi != myInfo && route.size() < distance){
				result = pi;
				distance = route.size();
			}
		}		
		return result;
	}

	/**
	 *  Finds nearest reacheable enemy.
	 * 
	 * @return Nearest reachable enemy or null if none can be found;
	 */
	private PlayerInfo getNearestReachableOpponent() {
		PlayerInfo result = null;
		int distance = Integer.MAX_VALUE;
		for (PlayerInfo pi : state.getPlayers()) {
			Stack<CellInfo> route = board.findRoute(board.getCell(myInfo.getCoords().x, myInfo.getCoords().y), board.getCell(pi.getCoords().x,pi.getCoords().y));
			if (route != null && pi != myInfo && route.size() < distance){
				result = pi;
				distance = route.size();
			}
		}		
		return result;
	}


	/** 
	 * 	Tells if 
	 * 
	 */
	private boolean isSafe() {
		if (myInfo == null) return false;
		int x = myInfo.getCoords().x;
		int y = myInfo.getCoords().y;
		if (board.getCell(x, y).willExplode()) {
			return false;
		} else
			return true;
	}
	
	/**
	 * Finds place to hide
	 * 
	 * @param maxDistance - max allowed distance between current position and place to hide
	 * @return Place to hide or null if doesn't find anything
	 */
	
	private CellInfo findPlaceToHide(final int maxDistance) {
		final int x = myInfo.getCoords().x;
		final int y = myInfo.getCoords().y;
		final CellInfo playerCell = board.getCell(x, y);
		final List<CellInfo> neighbours = new ArrayList<CellInfo>();
		final CellInfo result;
		final HashMap<CellInfo, Integer> distances = new HashMap<CellInfo, Integer>();
		CellPredicate pred = new CellPredicate() {
			public boolean test(CellInfo ci) {
				int dist = board.getDistance(ci, playerCell);
				boolean result = ( (ci.getType().isWalkable() || (ci.x ==x && ci.y == y)) &&
						 (!ci.willExplode() || ci.getTimeToExplode() > (board.getDistance(ci, playerCell) + 1) * Globals.DEFAULT_CELL_SIZE / 2) ); 
				if (result) distances.put(ci, dist);
				return result;
			}
		};
		
		CellInfo destination = null;
		board.addConnectedCells(neighbours, x, y, maxDistance, pred);
		int minDistance = Integer.MAX_VALUE;
	
		for (CellInfo ci : neighbours) { 	// try to find completly safe cell
			if (!ci.willExplode() && distances.get(ci) < minDistance) {
				destination = ci;
				minDistance = distances.get(ci);
			}
		}
		
		if (destination == null) {			// get cell with highest timeToExplode, if all will explode
			int maxTime = Integer.MIN_VALUE;			
			for (CellInfo ci : neighbours) {
				if (ci.getTimeToExplode() > maxTime) {
					destination = ci;
					maxTime = ci.getTimeToExplode();
				}
			}
		}
		if (destination == null) {
			result = null; //GONNA DIE, What should i do !? ;(
		} else {
			Stack<CellInfo> route = board.findRoute(playerCell, destination, pred );
			if (route == null) {	// shouldn't happend
				result = null; 		// GONNA DIE
			} else {
				route.pop();			// pop starting point
				result = route.pop();	// return next cell;
			}
		}
		return result;
	}
	
	
	/**
	 * Tells if there will be chance to escape in case of placing bomb
	 * 
	 */
	private boolean canSafelyPlaceBomb() {
		
		CellPredicate pred = new CellPredicate() {
			public boolean test(CellInfo ci) {
				return !ci.willExplode() &&	ci.getType().isWalkable() && !ci.getType().isExplosion();
			}
		};
		
		int x = myInfo.getCoords().x;
		int y = myInfo.getCoords().y;
		List<CellInfo> cells = new ArrayList<CellInfo>();
		state.getBoard().addConnectedCells(cells, x, y, 4, pred);
		cells.remove(board.getCell(x,y));
		for (int r = 1; r <= myInfo.getBombRange(); r++) {
			if (x + r < board.getWidth()) 
				cells.remove(board.getCell(x+r,y));
			if (x - r >= 0) 
				cells.remove(board.getCell(x-r,y));
			if (y + r < board.getHeight()) 
				cells.remove(board.getCell(x,y+r));
			if (y - r >= 0) 
				cells.remove(board.getCell(x,y-r));
		}
		return (!cells.isEmpty() && 
				(!board.getCell(x, y).willExplode() || board.getCell(x, y).getTimeToExplode() > 2 * Globals.DEFAULT_CELL_SIZE)) ;
	}
	
	/**
	 * Tells if there any other player currently in range of player's bomb
	 * 
	 */
	
	private boolean enemyInRange() {
		final List<PlayerInfo> ops = state.getPlayers();
		ops.remove(myInfo);
		for (PlayerInfo pi : ops) {
			if ( (myInfo.getCoords().x == pi.getCoords().x &&
				  Math.abs(myInfo.getCoords().y - pi.getCoords().y) < myInfo.getBombRange()-1) ||
				 (myInfo.getCoords().y == pi.getCoords().y &&
				   Math.abs(myInfo.getCoords().x - pi.getCoords().x) < myInfo.getBombRange()-1))
				return true;
		}
		return false;
	}
	
	
	/**
	 * Tells if there any crate in range of player's bomb
	 * 
	 */
	private boolean isCrateInRange() {
		final int x = myInfo.getCoords().x;
		final int y = myInfo.getCoords().y;
		
		CellPredicate pred = new CellPredicate() {
			public boolean test(CellInfo ci) {
				 return (( (ci.x == x && Math.abs(ci.y - y) < myInfo.getBombRange()) || 
						    ci.y == y && Math.abs(ci.x - x) < myInfo.getBombRange()) &&
						   (ci.getType().isWalkable() || ci.getType() == CellType.CELL_CRATE) );
			}
		};
		List<CellInfo> cells = new ArrayList<CellInfo>();
		state.getBoard().addConnectedCells(cells, x, y, myInfo.getBombRange(), pred);
		for (CellInfo ci : cells) {
			if (ci.getType() == CellType.CELL_CRATE) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 *  Get list of bonuses, for wchich our bot is closest player.
	 * 
	 */
	private List<CellInfo> getReacheableBonuses() {
		BonusesInfo bi = state.getBonusesInfo();
		final List<CellInfo> result = new ArrayList<CellInfo>();
		for( CellInfo cell : bi.getCellsWithBonus()) {
			PlayerInfo nearestPlayer = null;
			int minDistance = Integer.MAX_VALUE;			
			for ( PlayerInfo pi : state.getPlayers()) {
				Stack<CellInfo> route = board.findRoute(cell, board.getCell(pi.getCoords().x,pi.getCoords().y));
				if (route != null && route.size() < minDistance) {
					nearestPlayer = pi;
					minDistance = route.size();
				}
			}
			if (nearestPlayer == myInfo) result.add(cell);
		}
		return result;
	}

	/**
	 * Tell if bot change cell in next frame, after continue moving in current direction
	 * 
	 */
	private boolean willCrossCellBorder(CellInfo nextCell) {
		int deltaX = (int)(Math.signum(nextCell.x - myInfo.getCoords().x) * 2);
		int deltaY = (int)(Math.signum(nextCell.y - myInfo.getCoords().y) * 2); 
		int nextX = deltaX + myInfo.getExactCoords().x;
		int nextY = deltaY + myInfo.getExactCoords().y;
		Point nextGridCoords = state.exactToGridCoords(new Point(nextX, nextY));
		return (nextGridCoords.x == nextCell.x && nextGridCoords.y == nextCell.y);
	}

	
	
}
