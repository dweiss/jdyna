package ai;

import java.awt.Point;
import java.util.List;
import java.util.Map;

import ai.board.BombCell;
import ai.board.EditableCell;
import ai.player.PlayerInfo;
import ai.utilities.BombUtility;
import ai.utilities.BorderFactory;
import ai.utilities.GameUtility;
import ai.utilities.MathUtility;
import ai.utilities.MoveUtility;
import ai.utilities.PathUtility;
import ai.utilities.PlayersUtility;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.SoundEffect;

/**
 * @author Slawek, Asia
 */
public class MyPlayer implements IPlayerController, IGameEventListener {

	/**
	 * current direction
	 */
	private Direction direction = null;

	
	private Map<String, PlayerInfo> players;
	
	/**
	 * editable game snapshot
	 */
	private EditableCell[][] myCells;

	int[] lastUserPosition, lastOponentPosition, lastBombPosition;

	Point lastOponentPointPosition;
	private Direction lastDirection;

	/**
	 * last event frame when state was GameEvent.Type.GAME_STATE
	 */
	private int lastGameFrame = 0;

	PlayerInfo myPlayer;
	boolean bomb = false;

	private int soundEffectCount = -1;
	private SoundEffect soundEffect;
	private String name;
	
	//utilities
	
	private BorderFactory borderFactory;
	private BombUtility bombUtility;
	private PlayersUtility playersUtility;
	private PathUtility pathUtility;
	private GameUtility gameUtility;
	
	public MyPlayer(String name) {
		this.name = name;
		
		playersUtility=new PlayersUtility();
		bombUtility=new BombUtility();
		gameUtility=new GameUtility();
		
		borderFactory=new BorderFactory(bombUtility);
		pathUtility=new PathUtility(bombUtility,gameUtility,borderFactory);
		myPlayer = new PlayerInfo(name, 0);
		myPlayer.bombCount = Globals.DEFAULT_BOMB_COUNT;
		myPlayer.bombRange = Globals.DEFAULT_BOMB_RANGE;
	}

	public String getName() {
		return name;
	}

	private boolean canDropBomb(int[] oponentPosition,
			Direction oponentDirection, int[] userPosition, int userBombRange,
			Direction userDirection, EditableCell[][] cells) {

		int lineLength = Integer.MAX_VALUE;
		int[] clone = userPosition.clone();
		
		if(oponentPosition[0]==userPosition[0] && oponentPosition[1]==userPosition[1]){
			return true;
		}

		if (oponentDirection == null) {
			int distance = MathUtility.getLineDistance(clone, userPosition);
			if (distance >= 0) {
				lineLength = distance;
			}
		} else if (oponentDirection.equals(Direction.LEFT)
				|| oponentDirection.equals(Direction.RIGHT)) {
			lineLength = Math.abs(oponentPosition[1] - userPosition[1]);
			clone[0] = userPosition[0];
		} else {
			lineLength = Math.abs(oponentPosition[0] - userPosition[0]);
			clone[1] = userPosition[1];
		}
		
		if(borderFactory.areInLine(cells, oponentPosition, userPosition) && ((userDirection==null || oponentDirection==null)|| userDirection.equals(oponentDirection))){
			return false;
		}

		if (((lineLength > 0 && lineLength < userBombRange) || (userDirection != null
				&& lineLength == 0 && userDirection.equals(oponentDirection)))
				&& borderFactory.areInLine(cells, clone, oponentPosition)) {
			return true;
		}

		return false;
	}

	/**
	 * 
	 * predict oponent position in base of they last direction and position
	 * 
	 * 
	 * @param cells
	 * @param position
	 * @param direction
	 * @return
	 */
	private int[] getPredictedUserPosition(EditableCell[][] cells,
			int position[], Direction direction) {

		List<Direction> directions = GameUtility.getAvailableDirections(position, cells);
		directions.remove(MathUtility.getOpositeDirection(direction));
		if (directions.contains(direction)) {
			position = MoveUtility.moveUser(position, direction);
		} else {
			if (directions.size() == 1) {
				position = MoveUtility.moveUser(position, directions.get(0));
			}
		}

		return position;
	}

	@Override
	public boolean dropsBomb() {
		return bomb;
	}

	@Override
	public Direction getCurrent() {
		return direction;
	}

	
	private Direction predictOponentDirection(Point lastOponentPointPosition,EditableCell[][] myCells,int [] oponentPosition,Point oponentPointPosition){
		
		Direction oponentDirection =  MathUtility.getDirection(lastOponentPointPosition,
				oponentPointPosition);
		if (oponentDirection != null) {
			oponentPosition = getPredictedUserPosition(myCells,
					oponentPosition, oponentDirection);
		}
		return oponentDirection;
		
	}

	@Override
	public void onFrame(int frame, List<? extends GameEvent> events) {
		GameEvent gameEvent;
		List<Direction> bombedDirections;
		List<Direction> lethAndExpDirections;
		Direction oponentDirection = null;
		direction = null;
		int id;

		for (int c = 0; c < events.size(); c++) {
			gameEvent = events.get(0);
			if (gameEvent.type.equals(GameEvent.Type.GAME_STATE)) {

				GameStateEvent gameStateEvent = (GameStateEvent) gameEvent;
				Cell[][] cells = gameStateEvent.getCells();
				Point myPointPosition = playersUtility.getMyPosition(gameStateEvent.getPlayers(),name); 
				Point oponentPointPosition = playersUtility.getNearestOponentPointPosition(gameStateEvent.getPlayers(), myPointPosition,myPlayer);
				
				int myCellPosition[] = MathUtility.getPosition(myPointPosition);
				int oponentCellPosition[] = MathUtility.getPosition(oponentPointPosition);
				myPlayer.position = myCellPosition;				

				if (players == null) {
					// first time
					players=playersUtility.initPlayers(gameStateEvent.getPlayers(),name, frame);
				}

				playersUtility.updateOponentPositions(gameStateEvent.getPlayers(), frame,myPlayer);
				players=playersUtility.getPlayers();
				
				if (soundEffectCount != -1) {
					borderFactory.handleSoundEffects(cells, myCells, players, soundEffect,
							myPlayer,soundEffectCount, frame);
					soundEffectCount = -1;
				}

				if (myCells == null) {
					// first time
					myCells = borderFactory.convert(cells, frame);
				} else {
					
					int dropBombFrame;
					if (lastGameFrame + 1 != frame) {
						dropBombFrame = lastGameFrame + 1;
					} else {
						dropBombFrame = frame;
					}
					borderFactory.update(cells, myCells, players,myPlayer, dropBombFrame);
				}

				if (lastOponentPointPosition != null) {
					oponentDirection = predictOponentDirection(lastOponentPointPosition,myCells,oponentCellPosition,oponentPointPosition);
				}

				// przewidujemy kolejna pozycje uzytkownika, w tym przypadku
				// zakladamy ze uzytkownik w kolejnej
				// ramce poruszy sie w ten sam sposob

				List<Direction> availableDirections = GameUtility.getAvailableDirections(myCellPosition,myCells);
				
				if(borderFactory.isOnBomb(myCells, myCellPosition)){
					availableDirections=pathUtility.getSafeDirections(myCells, myCellPosition, myPointPosition, availableDirections, frame, frame - 34);
				}
				
				bombedDirections = pathUtility.getBombedDirections(myCellPosition,myPointPosition, availableDirections, myCells, frame);

				lethAndExpDirections = pathUtility.getBlockedDirections(myCellPosition,myCells, availableDirections);
				List<Direction> lds = pathUtility.getLethalDirections(myCellPosition,myPointPosition, availableDirections, myCells, frame);

				List<Direction> blocked = pathUtility.getBlockedDirection2(myCellPosition.clone(), (Point) myPointPosition.clone(), availableDirections, myCells,
						frame);

				availableDirections.removeAll(lds);
				availableDirections.removeAll(bombedDirections);
				availableDirections.removeAll(lethAndExpDirections);
				availableDirections.removeAll(blocked);

				id = frame + 1;
				availableDirections = gameUtility.sortDirections(myCellPosition, oponentCellPosition, availableDirections);
				myCells[myCellPosition[0]][myCellPosition[1]].id = id;
				
				direction=pathUtility.getShortestDirection(availableDirections, myPointPosition, myCellPosition, oponentCellPosition, myCells, id, frame);
				
				if (direction == null) {
					// kierunki poblokowane - stoj w miejscu, sprawdzenie czy
					// nie ma bomb na miejscu
					
					if(borderFactory.isOnBomb(myCells, myCellPosition)){
						// user and his oponent are on the bomb !!
						// run anywere ! 
						if(!availableDirections.isEmpty()){
							direction=availableDirections.get(0);		
						}else{
							throw new RuntimeException("no direction is avaialable");
						}
					}
					
					
					if (pathUtility.isSafePosition(myCells, myCellPosition,
							myPointPosition, frame)) {
					} else {
						if (!availableDirections.isEmpty()) {
							if (availableDirections.contains(lastDirection)) {
								direction = lastDirection;
							} else {
								direction = availableDirections.get(0);
							}
						}
					}
				}

				int[] temp;

				int distanceToLastBomb = 2;
				if (lastBombPosition != null) {
					distanceToLastBomb = Math.abs(myCellPosition[0]
							- lastBombPosition[0])
							+ Math.abs(myCellPosition[1] - lastBombPosition[1]);
				}

				if (distanceToLastBomb >= 2
						&& canDropBomb(oponentCellPosition, oponentDirection,
								myCellPosition, myPlayer.bombRange, direction,
								myCells)) {
					Point nextPoint = (Point) myPointPosition.clone();
					nextPoint = MoveUtility.moveUser(nextPoint, 2, direction);
					temp = MathUtility.getPosition(nextPoint);
					BombCell bc = new BombCell(CellType.CELL_BOMB);
					bc.explosionFrame = (frame + 1)
							+ Globals.DEFAULT_FUSE_FRAMES;
					bc.range = myPlayer.bombRange;
					EditableCell copy = myCells[temp[0]][temp[1]];
					myCells[temp[0]][temp[1]] = bc;
					List<Direction> dirs = pathUtility.getSafeDirections(myCells, temp.clone(),
							nextPoint, GameUtility.getAvailableDirections(temp, myCells),
							frame, id-1);
					if (dirs.isEmpty()) {
						bomb = false;
					} else {
						bomb = true;
						lastBombPosition = myCellPosition;
					}
					myCells[temp[0]][temp[1]] = copy;
				} else {
					bomb = false;
				}
				lastDirection = direction;
				lastOponentPointPosition = oponentPointPosition;
				lastUserPosition = myCellPosition;
				lastGameFrame = frame;
			}
		}
	}
}
