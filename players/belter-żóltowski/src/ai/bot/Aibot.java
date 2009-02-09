package ai.bot;


import java.awt.Point;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.omg.PortableServer.POA;

import ai.navigation.Metrics;
import ai.navigation.NavGraph;
import ai.navigation.Node;
import ai.navigation.NodeType;
import ai.navigation.PathFinder;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.ControllerState;
import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameStartEvent;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerController2;
import com.dawidweiss.dyna.IPlayerFactory;
import com.dawidweiss.dyna.corba.CPlayerFactoryAdapter;
import com.dawidweiss.dyna.corba.ICPlayerFactory;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;

/**
 * Bot's brain. This class is responsible for processing games states, 
 * undertaking appropriate decisions
 * and spurring computer player into action.
 */
public final class Aibot implements IPlayerController, IPlayerController2, 
	IGameEventListener, IPlayerFactory, ICPlayerFactory {
	
	private static final Logger log = Logger.getLogger(Aibot.class);
	
	/** delegate factory */
	private final CPlayerFactoryAdapter delegate;

    /** This player's name. */
    private String name;

    /** Target position we're want to reach, given in pixels. */
    private Point target;
    
    /**
     * This player's current position in pixels.
     */
    private Point position;

    /** Random number generator. */
    private Random rnd = new Random();

    private BoardInfo boardInfo;
    
    /** navigation graph */
    private NavGraph graph;
    
    /** path finder for path planning */
    private PathFinder pathFinder;
    
    /** player knowledge about game state and competitors */
    private PlayerPerception percepton;
    
    /** time delay between two bomb can be planted */
    private int delay = 0;

    /** current player's direction */
    private volatile Direction direction;
    
    /** indicator when to drop bomb */
    private volatile boolean bombIndicator;

    /**
     * Constructs computer player
     * @param name player name
     */
    public Aibot(String name) {
        this.name = name;
        this.delegate = new CPlayerFactoryAdapter(this);
    }
    
    /**
     * Constructs computer player with default name
     */
    public Aibot() {
    	this.name = getDefaultPlayerName();
        this.delegate = new CPlayerFactoryAdapter(this);
    }

    @Override
    public synchronized boolean dropsBomb() {
        final boolean tmp = bombIndicator;
        /* clear status after has been read by server */
        bombIndicator = false;
        return tmp;
    }

    @Override
    public Direction getCurrent() {
        return direction;
    }
    
    @Override
	public synchronized ControllerState getState() {
		final boolean tmp = bombIndicator;
		bombIndicator = false;
		return new ControllerState(direction, tmp, 0);
	}

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events) {
    	try {
	        for (GameEvent event : events) {
	            if (event.type == GameEvent.Type.GAME_START) {
	                this.boardInfo = ((GameStartEvent) event).getBoardInfo();
	                this.graph = new NavGraph(boardInfo.gridSize);
	                this.pathFinder = new PathFinder(graph);
	                this.percepton = new PlayerPerception(boardInfo, graph);
	            } else if (event.type == GameEvent.Type.GAME_STATE) {
	                final GameStateEvent gse = (GameStateEvent) event;
	                
	                /* updates knowledge about the game */
	                percepton.update(gse);
	                PlayerAttributes myself = percepton.getPlayerByName(name);
	                if (myself.isDead) {
	                	continue;
	                }
	                position = boardInfo.pixelToGrid(myself.position);
	                
	                chooseDestination();
	                List<Node> path = pathFinder.getPath(position, target);
	                if (path != null) {
	                	target = path.get(0).getLocation();
	                	//pathFinder.print(position, target);
	                }
	                if (!shouldAvoidBomb()) {
	                	if (shouldDropBomb()) {
	                    	bombIndicator = true;
	                    	delay = Globals.DEFAULT_FRAME_RATE*2;
	                	}
	                }
	                moveToTarget();
	            }
	        }
    	} catch (RuntimeException e) {
    		log.debug("Unexpected runtime error occurred", e);
    	}
    }
    
    /**
     * Picks current destination goal depending on priority
     */
    public void chooseDestination() {
    	List<Node> candidates = null;
    	candidates = graph.getNodesByType(NodeType.BONUS);
    	/* yeah we like bonuses */
    	if (!candidates.isEmpty()){
    		int index = rnd.nextInt(candidates.size());
        	target = candidates.get(index).getLocation();
        /* move to chosen victim within our bomb scope (sort of. this is just heuristic) */
    	} else {
    		Point victimGridPos = boardInfo.pixelToGrid(chooseVictim());
    		/*Point displacementVec = new Point(position.x - victimGridPos.x,
    				position.y - victimGridPos.y);
    		int dx = Math.abs(displacementVec.x);
    		int dy = Math.abs(displacementVec.y);
    		int range = percepton.getPlayerByName(name).maxBombRange;
    		 //calculate point on the map we should approach 
    		if (dx != 0 && dx > dy) {
    			victimGridPos.translate(displacementVec.x / dx * range, 0);
    		} else if (dy != 0 && dx < dy){
    			victimGridPos.translate(0, displacementVec.y / dy * range);
    		} 
    		
    		 //ensure this point is empty to pick any nearby 
    		List<Node> nodes = graph.getNodesInRangeByType(NodeType.EMPTY, victimGridPos, 3);
    		if (!nodes.isEmpty()) {
    			target = nodes.get(0).getLocation();
    		} else {
    			target = victimGridPos;
    		}*/
    		target = victimGridPos;
    	}
    }
    
    /**
     * Assigns the closest player as a target to kill
     * @return pixel coordinates of chosen victim
     */
    private Point chooseVictim() {
    	Point currPixelPosition = boardInfo.gridToPixel(position);
    	List<PlayerAttributes> victims = percepton.
    		getNearestPlayersToTarget(currPixelPosition.x, currPixelPosition.y);
    	/* avoid chasing ourselves */
    	if (victims.size() > 1) {
    		return victims.get(1).position;
    	} else {
    		return position;
    	}
    }
    
    /**
     * Notifies if this is a good time to plant a bomb
     * @return true if we should drop bomb, false otherwise
     */
    public boolean shouldDropBomb() {
    	delay--;
    	PlayerAttributes myself = percepton.getPlayerByName(name);
    	Point victimGridPos = boardInfo.pixelToGrid(chooseVictim());
    	double distanceToVictim = Metrics.manhattanDistance(victimGridPos, position);
    	return distanceToVictim < myself.maxBombRange && delay < 0 ? true : false;
    }
    
    /**
     * Notifies if we should circumvent any bombs
     * @return true if we are in danger, false otherwise
     */
    private boolean shouldAvoidBomb() {
    	Node currNode = graph.getNodeAt(position);
    	Node targetNode = graph.getNodeAt(target);
    	if (currNode.getType() == NodeType.EXPLOSION) {
    		List<Node> safeplaceCandiates = graph.getNodesInRangeByType(NodeType.EMPTY, position, 3);
    		if (!safeplaceCandiates.isEmpty()) target = safeplaceCandiates.get(0).getLocation();
    		return false;
    	} else if (targetNode.getType() == NodeType.CRATE || targetNode.getType() == NodeType.EXPLOSION ) {
    		direction = null;
    		return true;
    	}
    	return false;
    }
    
    /**
     * Makes movement progress toward target location
     */
    private void moveToTarget() {
    	final Point sourceXY = boardInfo.gridToPixel(position);
    	final Point targetXY = boardInfo.gridToPixel(target);

    	if (Metrics.euclideanDistance(sourceXY, targetXY) < 3) {
    		direction = null;
    		this.target = null;
    		return;
    	}

        if (sourceXY.x < targetXY.x) direction = Direction.RIGHT;
        else if (sourceXY.x > targetXY.x) direction = Direction.LEFT;
        else if (sourceXY.y < targetXY.y) direction = Direction.DOWN;
        else if (sourceXY.y > targetXY.y) direction = Direction.UP;
    }
    
    @Override
    public IPlayerController getController(String playerName) {
    	return new Aibot(getDefaultPlayerName());
    }
    
    @Override
    public String getDefaultPlayerName() {
    	return "Bocik";
    }

    @Override
    public String getVendorName() {
    	return "Sławomir Belter, Patryk Żółtowski";
    }
    
    @Override
    public ICPlayerController getController(String playerName, POA poa) {
    	return delegate.getController(playerName, poa);
    }
}
