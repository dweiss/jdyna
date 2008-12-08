package com.dawidweiss.dyna.corba;

import java.util.logging.Logger;

import com.dawidweiss.dyna.IController;
import com.dawidweiss.dyna.corba.bindings.CBoardInfo;
import com.dawidweiss.dyna.corba.bindings.CBoardSnapshot;
import com.dawidweiss.dyna.corba.bindings.CControllerState;
import com.dawidweiss.dyna.corba.bindings.CPlayer;
import com.dawidweiss.dyna.corba.bindings.CStanding;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerPOA;
import com.dawidweiss.dyna.view.IBoardSnapshot;
import com.dawidweiss.dyna.view.swing.BoardFrame;

/**
 * An adapter for {@link IController} updating player state on a remote game server and
 * displaying the current game state.
 */
public class ClientSidePlayerController extends ICPlayerControllerPOA
{
    private final static Logger logger = Logger.getLogger("player");
    private final IController controller;

    private BoardFrame view;
    private CPlayer [] players;
    private CBoardInfo boardInfo;

    public ClientSidePlayerController(IController controller)
    {
        this.controller = controller;
    }

    @Override
    public CControllerState state()
    {
        return Adapters.adapt(controller);
    }

    @Override
    public synchronized void onStart(CBoardInfo boardInfo, CPlayer [] players)
    {
        logger.info("Game started.");
        this.players = players;
        this.boardInfo = boardInfo; 

        view = new BoardFrame(Adapters.adapt(boardInfo));
        view.setVisible(true);
    }

    @Override
    public synchronized void onNextFrame(int frame, CBoardSnapshot snapshot)
    {
        /*
         * Because events from the game controller are asynchronous, it may happen
         * that view is still uninitialized.
         */
        if (view == null)
        {
            return;
        }
        IBoardSnapshot adapted = Adapters.adapt(snapshot, boardInfo, players);
        view.onNextFrame(frame, adapted);
    }

    @Override
    public synchronized void onEnd(CPlayer winner, CStanding [] standings)
    {
        logger.info("Game finished.");
        view.dispose();
        view = null;
    }
}
