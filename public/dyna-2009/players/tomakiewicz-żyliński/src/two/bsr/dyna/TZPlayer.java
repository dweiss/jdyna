package two.bsr.dyna;

import java.awt.Point;
import java.util.*;

import org.jdyna.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class TZPlayer implements IPlayerController, IPlayerController2, IGameEventListener {

	/* Game data */

	/** Old board cells. */
	private Cell[][] oldCells;
	
	/** Object for locking */
	private final static Object lock = new Object();
	
	/** Logger */
	private final static Logger logger = LoggerFactory.getLogger("corba.gameservant");
	
    /** This player's name. */
    private String name;
    
    /** Max bomb noumber */
    private int max_bombs = Globals.DEFAULT_BOMB_COUNT;
    
    /** Positions of bomb I dropped */
    private ArrayList<Point> myBombPositions = new ArrayList<Point>();
    
    /** Number of current avaliable bombs */
    private int current_bomb_no = 0;

    /** Target position we're aiming at, in pixels. */
    private Point target;
    
    /** Player position on grid*/
    Point myCurrentGridPosition;

    /**
     * Cached board info.
     */
    private BoardInfo boardInfo;

    /**
     * My direction
     */
    private volatile Direction direction;
    
    /**
     * Drop bomb
     */  
    private volatile boolean bomb;	

    /** Information about enemies */
	private HashMap<String, TZPlayerInfo> enemies = new HashMap<String, TZPlayerInfo>();
	
	/** Information about enemies bombs on board*/
	private HashMap<Point, TZBombInfo> bombs = new HashMap<Point, TZBombInfo>();

	public TZPlayer(String name) {
		this.name = name;
	}

	public static Player createPlayer(String name) {
		return new Player(name, new TZPlayer(name));
	}

	@Override
	public boolean dropsBomb() {
		return bomb;
	}

	@Override
	public Direction getCurrent() {
		return direction;
	}

	@Override
	public ControllerState getState() {
		return new ControllerState(direction, bomb, 1);
	}

	@Override
	public void onFrame(int frame, List<? extends GameEvent> events) {
		this.bomb = false;
		for (GameEvent event : events) {
			if (event.type == GameEvent.Type.GAME_START) {
				this.boardInfo = ((GameStartEvent) event).getBoardInfo();
				this.direction = null;
				this.target = null;
			} else if (event.type == GameEvent.Type.GAME_STATE) {
				final GameStateEvent gse = (GameStateEvent) event;

				if (oldCells == null) {
					oldCells = copyCells(gse);
					return;
				}
				
				updateEnemiesInfo(gse);

				oldCells = copyCells(gse);
				
	        	synchronized (lock){
	                final IPlayerSprite myself = identifyMyself(gse.getPlayers());
	                Cell cells[][] = ((GameStateEvent) event).getCells(); 
	                
	                Point myCurentPosition = myself.getPosition();
	                myCurrentGridPosition = boardInfo.pixelToGrid(myCurentPosition);
	                ArrayList<Direction> possibleDirections = getPossibleDirections( cells, myCurentPosition );
	                
	                if( myself.isDead() ){
	                	target = null;
	                }
	                
	                if( !myself.isDead() && ( this.target == null || ( this.target.x == myCurentPosition.x && this.target.y == myCurentPosition.y ) ) ){
	                	Random randomGenerator = new Random();
	                	
	                	if( possibleDirections.size() > 0 ){
	                		
	                		ArrayList<Direction> modPossibleDir = new ArrayList<Direction>( possibleDirections );
	                		ArrayList<Direction> bombDirections = inBombRange( cells, myCurrentGridPosition );
	                		ArrayList<Direction> modPossibleWithLethal; 
	                		
	                		modPossibleDir.removeAll( bombDirections );
	                		modPossibleWithLethal = new ArrayList<Direction>( modPossibleDir );
	                		
	                		ArrayList<Direction> lethalDirections = inExplosionRange( cells, myCurrentGridPosition, modPossibleDir );
	                		modPossibleWithLethal.removeAll(lethalDirections);
	                		ArrayList<Direction> finalList = new ArrayList<Direction>();
	                		
	                		if( modPossibleWithLethal.size() > 0 ){
	                			finalList = modPossibleWithLethal;
	                		}else if( inBombRange( cells, myCurrentGridPosition ).size() == 0 ) {
	                			finalList.clear();
	                		}else{
	                			Direction bestPath = findPositionToRun( cells, myCurrentGridPosition, possibleDirections );
	                			if( bestPath != null ){
	                				finalList.add(bestPath);
	                			}else{
	                				finalList = possibleDirections;
	                			}
	                		}

	                		int dir = 0;
		                	if( finalList.size() == 0 ){
		                		this.direction = null;
		                		target = null;
		                	}else{
		                		Direction bonus = goToBonus( cells, myCurrentGridPosition, possibleDirections );
		                		if( bonus != null ){
		                			this.direction = bonus;
		                		}else{
			                	dir = randomGenerator.nextInt( finalList.size() ) + 1;		                		
			                	this.direction = finalList.get( dir - 1 );
		                		}
		                		this.target = getNewDirectionPoint( this.direction, myCurentPosition );
		                	}
		                	
		                	// Dropping bomb
		                	if( target != null ){
		                		if( possibleDirections.size() >= 3 ){
		                			if( new Random().nextInt() % 5 == 0 ){
		                				this.bomb = true;
		                			}else{
		                				this.bomb = false;
		                			}
		                		}
		                		
		                		if( playerIsNear( cells, myCurentPosition, gse.getPlayers() ) ){
		                			this.bomb = true;
		                		}
		                	}
		                	else{
		                		this.bomb = false;
		                	}
		                	updateBombPosition( cells );
	                	}
	                }
	                else{
	                	
	                }
		        }				
			}
		}
	}
	
	/**
		Chech if enemy is near
	*/
	private boolean playerIsNear(Cell[][] cells, Point myCurentPos, List<? extends IPlayerSprite> players) {
		
        for (IPlayerSprite ps : players)
        {
        	Point p = ps.getPosition();
            if ( ( p.x == myCurentPos.x || p.y == myCurentPos.y ) && !ps.isDead() && !name.equals(ps.getName()) )
            {
                return true;
            }
        }
		
		return false;
	}

	/**
		Updating information about enemies
	*/
	private void updateEnemiesInfo(GameStateEvent state) {
		for (IPlayerSprite player : state.getPlayers()) {
			if (player.getName().equals(name)) {
				continue;
			}
			if (player.isDead()) {
				enemies.remove(player.getName());
				continue;
			}

			Point p = boardInfo.pixelToGrid(player.getPosition());
			p = new Point(p.x, p.y);
			
			if (!enemies.containsKey(player.getName())) {
				enemies.put(player.getName(), new TZPlayerInfo(player.getName(), p));
			}

			final TZPlayerInfo playerInfo = enemies.get(player.getName());
			
			// bomb/range bonuses counting
			if (oldCells[p.x][p.y].type == CellType.CELL_BONUS_BOMB) {
				++playerInfo.bombCount;
			} else if (oldCells[p.x][p.y].type == CellType.CELL_BONUS_RANGE) {
				++playerInfo.bombRange;
			}

			// update bombs list // this doesn't work yet
			final Point pOld = playerInfo.position;

			if (state.getCells()[pOld.x][pOld.y].type == CellType.CELL_BOMB) {
				if (!bombs.containsKey(pOld)) {
					bombs.put(pOld, new TZBombInfo(pOld, playerInfo.bombRange));
				}
				// TODO list clearing
			}
			playerInfo.position = p;
		}
	}
	
	private Cell[][] copyCells(GameStateEvent state) {
		final Cell[][] cells = new Cell[boardInfo.gridSize.width][boardInfo.gridSize.height];
		
		for (int x = 0; x < state.getCells().length; ++x) {
			for (int y = 0; y < state.getCells()[x].length; ++y) {
				cells[x][y] = Cell.getInstance(state.getCells()[x][y].type);
			}
		}
		
		return cells;
	}
	
	private void updateBombsInfo(GameStateEvent state) {
		for (int x = 0; x < state.getCells().length; ++x) {
			for (int y = 0; y < state.getCells()[x].length; ++y) {
				if (state.getCells()[x][y].type == CellType.CELL_BONUS_BOMB) {
					// do something
				}
			}
		}

	}
	
   private void updateBombPosition( Cell [][] cells ) {
    	ArrayList<Point> forDelete = new ArrayList<Point>();

    	for( int i = 0; i < myBombPositions.size(); i++ ){
    		Point p = myBombPositions.get(i);
    		if( cells[p.x][p.y].type != CellType.CELL_BOMB ){
    			forDelete.add(p);
    		}
    	}
    	
    	myBombPositions.removeAll( forDelete );
	}
   
    /**
		Transform Direction to new target point
	*/
	private Point getNewDirectionPoint( Direction dir, Point myCurentPosition ) {
		if( dir == Direction.DOWN ){
			return new Point( myCurentPosition.x, myCurentPosition.y + boardInfo.cellSize );
		}
		if( dir == Direction.LEFT ){
			return new Point( myCurentPosition.x - boardInfo.cellSize, myCurentPosition.y );
		}
		if( dir == Direction.RIGHT ){
			return new Point( myCurentPosition.x + boardInfo.cellSize, myCurentPosition.y );
		}
		if( dir == Direction.UP ){
			return new Point( myCurentPosition.x, myCurentPosition.y - boardInfo.cellSize );
		}
		
		return null;
	}
	
	/**
		Find yopurself on the players list
	*/
	private IPlayerSprite identifyMyself(List<? extends IPlayerSprite> players)
    {
        for (IPlayerSprite ps : players)
        {
            if (name.equals(ps.getName()))
            {
                return ps ;
            }
        }
        throw new RuntimeException("Player not on the list of players: " + name);
    }	

	/**
		Get possible direction where player can go ( walkable cells )
	*/
	private ArrayList<Direction> getPossibleDirections( Cell [][] cells, Point myPosition ){
	    final Point p = boardInfo.pixelToGrid(myPosition);
	    ArrayList<Direction> dirList = new ArrayList<Direction>();
	    
	    // Add upper cell
		if( boardInfo.isOnBoard( new Point( p.x, p.y - 1 ))){
			if( cells[p.x][p.y - 1].type.isWalkable() ){
				dirList.add( Direction.UP );
			}
		}
		// Add left cell
		if( boardInfo.isOnBoard( new Point( p.x - 1, p.y ))){
			if( cells[p.x - 1][p.y].type.isWalkable() ){
				dirList.add( Direction.LEFT );
			}
		}
		// Add right cell
		if( boardInfo.isOnBoard( new Point( p.x + 1, p.y ))){
			if( cells[p.x + 1 ][p.y].type.isWalkable() ){
				dirList.add( Direction.RIGHT );
			}
		}
		// Add down cell
	    if( boardInfo.isOnBoard( new Point( p.x, p.y + 1 ))){
	    	if( cells[p.x][p.y + 1].type.isWalkable() ){
	    		dirList.add( Direction.DOWN );
	    	}
	    }        
		    
	    return dirList;
	}
               
	/**
		Check is target field is in range of any bomb
	*/
	private ArrayList<Direction> inBombRange( Cell [][] cells, Point myPosition ){
	    final Point p = myPosition;
	    ArrayList<Direction> bombDir = new ArrayList<Direction>();
	    
	    for( int i = 0; i < 5; i++ ){
	    	if( (p.x - i) > 0 ){
	    		// LEFT
				if( cells[p.x - i][p.y].type == CellType.CELL_BOMB ){
					bombDir.add(Direction.LEFT);
				}
	    	}
	    	if( (p.x + i) < cells.length ){
				// RIGHT
				if( cells[p.x + i][p.y].type == CellType.CELL_BOMB ){
					bombDir.add(Direction.RIGHT);
				}
	    	}  
			if( (p.y - i) > 0 ){
				// UP
				if( cells[p.x][p.y - i].type == CellType.CELL_BOMB ){
					bombDir.add(Direction.UP);
				}        		
			}
			if( (p.y + i) < cells[i].length ){
				// DOWN
				if( cells[p.x][p.y + i].type == CellType.CELL_BOMB ){
					bombDir.add(Direction.DOWN);
				}        		
		    }         	
		}
		
		return bombDir;
	}
	
	/**
		Check which field near player is safe
	*/
	private ArrayList<Direction> inExplosionRange( Cell [][] cells, Point myPosition, ArrayList<Direction> modPossibleDir ){
	    final Point p = myPosition;
	    ArrayList<Direction> finalLethals = new ArrayList<Direction>();
	    ArrayList<Direction> tmpList = new ArrayList<Direction>();
	    Point myNewPoint = null;
	    Direction whereDelete = null;
	    
	    for( int i = 0; i < modPossibleDir.size(); i++ ){
	    	Direction dir = modPossibleDir.get(i);
	    	switch ( dir ){
	    		case UP : 
	    			myNewPoint = new Point(p.x, p.y - 1);
	    			whereDelete = Direction.UP;
	    			break; 
	    		case DOWN :
	    			myNewPoint = new Point(p.x, p.y + 1);
	    			whereDelete = Direction.DOWN;
	    			break;
	    		case LEFT : 
	    			myNewPoint = new Point(p.x - 1, p.y );
	    			whereDelete = Direction.LEFT;
	    			break;
	    		case RIGHT : 
	    			myNewPoint = new Point(p.x + 1, p.y );
	    			whereDelete = Direction.RIGHT;
	    			break;
	    	}
	    	
	    	tmpList.clear(); 
			tmpList = inBombRange( cells, myNewPoint );
			
			if( tmpList.size() > 0 ){
				finalLethals.add(whereDelete);
			}else{
				if(cells[myNewPoint.x][myNewPoint.y].type.isExplosion() ){
					finalLethals.add(whereDelete);
				}
			}
	    }
		return finalLethals;
	}    

	
	@Deprecated
	/** 
		Sometimes used for testing
	*/
	private ArrayList<Direction> rotateDirections( ArrayList<Direction> directions ){
		ArrayList<Direction> rotatedList = new ArrayList<Direction>();
			
		for( int i = 0; i < directions.size(); i++ ){
			switch( directions.get(i)){
				case UP : rotatedList.add( Direction.LEFT); break;
				case DOWN : rotatedList.add( Direction.RIGHT ); break;
				case LEFT : rotatedList.add( Direction.DOWN); break;
				case RIGHT : rotatedList.add( Direction.UP); break;
			}
		}
		
		return rotatedList;
	}

	/*
		If there isnt any safe field near player, find good place to move.
	*/
	private Direction bestWayToRun( Cell [][] cells, Point myPosition, ArrayList<Direction> posDir ){
		final Point p = myPosition;
		ArrayList<Point> verticalBomb = new ArrayList<Point>();
		ArrayList<Point> horisontalBomb = new ArrayList<Point>();
		ArrayList<Direction> directions = new ArrayList<Direction>();
		ArrayList<Point> bombPositions = new ArrayList<Point>();
		
		for( int i = 0; i < cells.length; i++ ){
			if( cells[i][p.y].type == CellType.CELL_BOMB ){
				verticalBomb.add( new Point( i, p.y ) );
				bombPositions.add( new Point( i, p.y ) );
			}
		}
		
		for( int i = 0; i < cells[0].length; i++){
			if( cells[p.x][i].type == CellType.CELL_BOMB ){
				horisontalBomb.add( new Point( p.x, i ) );
				bombPositions.add( new Point( p.x, i ) );
			}
		}
		
		for( int i = 0; i < verticalBomb.size(); i++ ){
			if( verticalBomb.get(i).x < p.x  && posDir.contains(Direction.RIGHT) ){
				directions.add(Direction.RIGHT);
			}
			else{
				if( posDir.contains(Direction.LEFT) ){
					directions.add(Direction.LEFT);
				}
			}
		}
		
		for( int i = 0; i < horisontalBomb.size(); i++ ){
			if( horisontalBomb.get(i).y < p.y && posDir.contains(Direction.DOWN) ){
				directions.add(Direction.DOWN);
			}
			else{
				if( posDir.contains(Direction.UP) ){
					directions.add(Direction.UP);
				}
			}
		}		
		
		if( directions.size() > 0 ){
			return directions.get( new Random().nextInt(directions.size()) );
		}else{
			return null;
		}
	}

	/**
		If there isnt any safe field near player, find best path to run out from bomb range.
	*/
	private Direction findPositionToRun( Cell [][] cells, Point myPos, ArrayList<Direction> posDir ){
		ArrayList<Point> finalPoints = new ArrayList<Point>();
		ArrayList<Point> pointsToCheck = new ArrayList<Point>();
		ArrayList<Direction> dirsToCheck = new ArrayList<Direction>();
		
		for( int i = 0; i < posDir.size(); i++){
			Point p = getPointFromDirection( myPos, posDir.get(i) );
			ArrayList<Direction> tmpDir = getPossibleDirections( cells, p );
			tmpDir = deleteOpposite( tmpDir, posDir.get(i) );
			for( int j = 0; j < tmpDir.size(); j++ ){
				Point x = getPointFromDirection( p, tmpDir.get(j) );
				if( inBombRange( cells, x ).isEmpty() ){
					finalPoints.add( x );
				}else{
					pointsToCheck.add( x );
					dirsToCheck.add( tmpDir.get(j) );
				}
			}
		}
		
		for( int i = 0; i < pointsToCheck.size(); i++){
			ArrayList<Direction> tmpDir = getPossibleDirections( cells, pointsToCheck.get(i) );
			tmpDir = deleteOpposite( tmpDir, dirsToCheck.get(i) );
			for( int j = 0; j < tmpDir.size(); j++ ){
				Point x = getPointFromDirection( pointsToCheck.get(i), tmpDir.get(j) );
				if( inBombRange( cells, x ).isEmpty() ){
					finalPoints.add( x );
				}else{
					pointsToCheck.add( x );
					dirsToCheck.add( tmpDir.get(j) );
				}
			}
		}	    	
	
		int minDis = Integer.MAX_VALUE;
		Point currPoint = null;
		for( int i = 0; i < finalPoints.size(); i++ ){
			int distance = countDistance( myPos, finalPoints.get(i) );
			if( distance < minDis ){
				minDis = distance;
				currPoint = finalPoints.get(i);
			}
		}
		if( myPos != null && currPoint != null){ 
			return chooseDirection( myPos, currPoint, posDir );
		}else{
			return bestWayToRun(cells, myPos, posDir );
		}
	}

	/**
		If there is more than one path to target field, choose best ( shortest ).
	*/
	private Direction chooseDirection(Point myPos, Point currPoint, ArrayList<Direction> posDir ) {
	
		int x = myPos.x - currPoint.x ;
		int y = myPos.y - currPoint.y ;
		ArrayList<Direction> posibilities = new ArrayList<Direction>();
		ArrayList<Direction> tmp = new ArrayList<Direction>();
		
		if( (myPos.x - currPoint.x) > 0 ){
			posibilities.add( Direction.LEFT );
		}else{
			posibilities.add( Direction.RIGHT );
		}

		if( (myPos.y - currPoint.y) > 0 ){
			posibilities.add( Direction.UP );
		}else{
			posibilities.add( Direction.DOWN );
		}    		

    	for( int i = 0; i < posibilities.size(); i++ ){
    		if( !posDir.contains( posibilities.get(i) ) ){
    			tmp.add( posibilities.get(i) );
    		}
    	}
    	
    	posibilities.removeAll( tmp );
    	
    	if( posibilities.size() > 0){
    		return posibilities.get( new Random().nextInt( posibilities.size() ));
    	}else{
    		return posDir.get( new Random().nextInt( posDir.size() ));
    	}
    	
	}

	/**
		Count distance between 2 cells
	*/
	private int countDistance(Point myPos, Point point) {
    	
    	int x = Math.abs( myPos.x - point.x );
    	int y =  Math.abs( myPos.y - point.y );

		return x + y;
	}

	/**
		Delete opposite direction
	*/
	private ArrayList<Direction> deleteOpposite(ArrayList<Direction> tmpDir, Direction dir) {

    	switch( dir ){
    		case UP :    tmpDir.remove(Direction.DOWN); break;
    		case DOWN :  tmpDir.remove(Direction.UP); break;
    		case LEFT :  tmpDir.remove(Direction.RIGHT); break;
    		case RIGHT : tmpDir.remove(Direction.LEFT);  break;
    	}
    	
		return tmpDir;
	}
 
	/**
	 	Convert direction and current field to target field.
	*/
	private Point getPointFromDirection( Point p, Direction dir ){
    	switch(dir){
    		case UP : return new Point( p.x, p.y - 1 );
    		case DOWN : return new Point( p.x, p.y + 1 );
    		case LEFT : return new Point( p.x - 1, p.y );
    		case RIGHT : return new Point( p.x + 1, p.y );
    	}
    	return null;
    }
	
	/**
		IF player is near bonus, go there.
	*/
	private Direction goToBonus( Cell[][] cells, Point myPos, ArrayList<Direction> posDir ){
		for( int i = 0; i < posDir.size(); i++ ){
			Point p = getPointFromDirection( myPos, posDir.get(i) );
			if( cells[p.x][p.y].type == CellType.CELL_BONUS_BOMB || cells[p.x][p.y].type == CellType.CELL_BONUS_RANGE ){
				if( inBombRange(cells, p ).isEmpty() ){
					return posDir.get(i);
				}
			}
		}
		return null;
	}
}