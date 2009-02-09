package com.jdyna.players.kkazmierczyk;

import java.util.List;
import java.util.logging.Logger;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameOverEvent;
import com.dawidweiss.dyna.GameStartEvent;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.corba.bindings.CControllerState;
import com.krzysztofkazmierczyk.dyna.GameStateEventUpdater;
import com.krzysztofkazmierczyk.dyna.client.AI.AIMoveChooser;
import com.krzysztofkazmierczyk.dyna.client.AI.MoveChooserInterface;
import com.krzysztofkazmierczyk.dyna.client.game.Utilities;

public class KazikLocalController implements IPlayerController, IGameEventListener
{

    private BoardInfo boardInfo;

    private MoveChooserInterface moveChooser;
    private GameStateEventUpdater currentGameState;

    /** We must synchronise access to this variable */
    private CControllerState ccontrolerState;

    private final static Logger logger = Logger.getLogger("KazikLocalController");

    @Override
    public boolean dropsBomb()
    {
        synchronized (ccontrolerState)
        {
            if (ccontrolerState != null)
            {
                return ccontrolerState.dropsBomb;
            }
            else
            {
                return false;
            }
        }
    }

    @Override
    public Direction getCurrent()
    {
        synchronized (ccontrolerState)
        {
            return com.krzysztofkazmierczyk.dyna.client.game.Adapters
                .getDirection(ccontrolerState);
        }
    }

    @Override
    public synchronized void onFrame(int frame, List<? extends GameEvent> events)
    {

        GameStateEvent gameStateEvent = null;

        for (GameEvent gameEvent : events)
        {

            if (gameEvent instanceof GameOverEvent)
            {
                currentGameState = null;
            }

            if (gameEvent instanceof GameStateEvent)
            {
                gameStateEvent = (GameStateEvent) gameEvent;
            }

            if (gameEvent instanceof GameStartEvent)
            {
                boardInfo = ((GameStartEvent) gameEvent).getBoardInfo();
            }
        }

        if (gameStateEvent != null)
        {

            if (currentGameState == null)
            {
                currentGameState = new GameStateEventUpdater(boardInfo, gameStateEvent,
                    frame);
                final int myId = Utilities.findMyName(currentGameState.getPlayers(),
                    KazikFactory.PLAYER_NAME);
                moveChooser = new AIMoveChooser(myId);
            }
            else
            {
                currentGameState.update(gameStateEvent, frame);
            }

            CControllerState tempContrlerState = moveChooser.move(currentGameState);

            synchronized (tempContrlerState)
            {
                ccontrolerState = tempContrlerState;
            }

        }

    }

}
