package com.szqtom.dyna;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameStartEvent;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerSprite;
import com.szqtom.dyna.generator.PathGenerator;
import com.szqtom.dyna.generator.Point;
import com.szqtom.dyna.radar.BombsDetector;

public class DefensiveController implements IPlayerController, IGameEventListener {

	private Direction direction;
	
	private String name;
	
	private BoardInfo boardInfo;
	
	private Point last;
	
	private static Logger log = LoggerFactory.getLogger(DefensiveController.class);
	
	private final PathGenerator generator;
	
	private boolean dropbomb = false;
	
	private final int DROPTIME = 10;
	
	private int toDrop = 0;
	
	private boolean generate = true;
	
	private Point destination;
	
	private BombsDetector bombsDetector = new BombsDetector();
	
	public DefensiveController(String Name){
		name = Name;
		generator = new PathGenerator(bombsDetector);
	}
	
	@Override
	public boolean dropsBomb() {
		return dropbomb;
	}

	@Override
	public Direction getCurrent() {
		return direction;
	}

	@Override
	public void onFrame(int frame, List<? extends GameEvent> events) {

        for (GameEvent event : events){
            if (event.type == GameEvent.Type.GAME_START) {           	
            	boardInfo = ((GameStartEvent) event).getBoardInfo();
                this.direction = null;
                last = new Point();
                destination = null;
            }else if (event.type == GameEvent.Type.GAME_STATE){
            	
            	final GameStateEvent gse = (GameStateEvent) event;
            	bombsDetector.updateBombsList(gse.getCells(), frame);
                 final IPlayerSprite myself = identifyMyself(gse.getPlayers());
                 if (myself.isDead()){
                     direction = null;
                 }
                 else {
                     final Point pixelPosition = new Point(myself.getPosition());
                     final Point gridPosition = new Point(boardInfo.pixelToGrid(pixelPosition));
                     if(!gridPosition.equals(last)){
                    	 last = gridPosition;
                 	 
                    	 if(toDrop > 0){
                    		 toDrop --;
                    		 dropbomb = false;                    		
                    	 }else{
                    		 toDrop = DROPTIME;
                    		 dropbomb = true;
                    	 }
                    	if(generate || direction == null){
                    		generate = false;
                    		destination = generator.getBestTarget(gse.getCells(), gridPosition, frame);
                    		generator.generatePath(gse.getCells(), new Point(gridPosition.x, gridPosition.y),destination, frame);
                    	}

                    	if(bombsDetector.pathInExplodeZone(gridPosition, generator.getPath(), gse.getCells(), frame)){
                    		generator.generatePath(gse.getCells(), new Point(gridPosition.x, gridPosition.y),destination, frame);
                    	}

                    	generator.makeMoveFromPath();
                    	 direction = generator.getDirection();
                     	 if(direction == null){
                     		destination = generator.getBestTarget(gse.getCells(), gridPosition, frame);
                     		generator.generatePath(gse.getCells(), new Point(gridPosition.x, gridPosition.y),destination, frame);
                    		 last = new Point();
                    	 }	

                     }else {             	 
                    	 if(bombsDetector.pathInExplodeZone(gridPosition, generator.getPath(), gse.getCells(), frame)){
                    		 destination = generator.getBestTarget(gse.getCells(), gridPosition, frame);
                    		 last = new Point();
                    	 }
                     }
                   
                     
                 }
            }
        }
		
	}
            
	/**
	 * Determine this player in the player list.
 	*/
	private IPlayerSprite identifyMyself(List<? extends IPlayerSprite> players){
        	for (IPlayerSprite ps : players){
				if (name.equals(ps.getName())){
					return ps;
                }
        	}
            throw new RuntimeException("Player not on the list of players: " + name);
       }
}
