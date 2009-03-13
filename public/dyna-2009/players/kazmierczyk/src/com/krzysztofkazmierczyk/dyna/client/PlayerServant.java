package com.krzysztofkazmierczyk.dyna.client;

import java.util.logging.Logger;

import org.jdyna.BoardInfo;
import org.jdyna.GameStateEvent;

import com.dawidweiss.dyna.corba.Adapters;
import com.dawidweiss.dyna.corba.bindings.CBoardInfo;
import com.dawidweiss.dyna.corba.bindings.CGameEvent;
import com.dawidweiss.dyna.corba.bindings.CGameResult;
import com.dawidweiss.dyna.corba.bindings.CPlayer;
import com.dawidweiss.dyna.corba.bindings.ICControllerCallback;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerPOA;
import com.krzysztofkazmierczyk.dyna.GameStateEventUpdater;
import com.krzysztofkazmierczyk.dyna.client.AI.AIMoveChooser;
import com.krzysztofkazmierczyk.dyna.client.AI.MoveChooserInterface;
import com.krzysztofkazmierczyk.dyna.client.game.Utilities;

/**
 * An player taking part in a remote game.
 */
public class PlayerServant extends ICPlayerControllerPOA
{

    private final static Logger logger = Logger.getLogger("player - ai");

    /**
     * Remote controller callback. We pump events from {@link #localController} to this
     * object.
     */
    private ICControllerCallback remoteController;

    private CPlayer [] players;

    private CBoardInfo cBoardInfo;

    private BoardInfo boardInfo;

    private MoveChooserInterface moveChooser;

    /** My id of player */
    private int myId;

    private final String myName;
    
    private GameStateEventUpdater currentGameState;

    public PlayerServant(String playerName)
    {
        myName = playerName;
    }

    public void setMyId(int myId)
    {
        this.myId = myId;
    }

    /*
     * 
     */
    public synchronized void onStart(CBoardInfo boardInfo, CPlayer [] players)
    {
        
       myId = Utilities.findMyName(players, myName);
        
        this.players = players;
        this.cBoardInfo = boardInfo;
        this.boardInfo = Adapters.adapt(cBoardInfo);

        moveChooser = new AIMoveChooser(myId);

        logger.info("Game started.");
    }


    /**
     * At now all only in one single tread. It is enough.
     */
    public synchronized void onFrame(int frame, CGameEvent [] events)
    {

        final GameStateEvent gameStateEvent = com.krzysztofkazmierczyk.dyna.client.game.Adapters
            .getGameStateEvent(events, cBoardInfo, players);

        if (currentGameState == null)
        {
            currentGameState = new GameStateEventUpdater(boardInfo, gameStateEvent, frame);
        }
        else
        {
            currentGameState.update(gameStateEvent, frame);
        }

        remoteController.update(moveChooser.move(currentGameState));

    }


    /*
     * 
     */
    public synchronized void onControllerSetup(ICControllerCallback callback)
    {
        logger.info("Setup controler");
        this.remoteController = callback;
    }


    @Override
    public void onEnd(CGameResult result)
    {
        currentGameState = null;
        logger.info("Game finished.");
    }
}
