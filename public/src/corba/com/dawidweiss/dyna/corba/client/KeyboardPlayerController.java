package com.dawidweiss.dyna.corba.client;

import java.util.logging.Logger;

import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.corba.Adapters;
import com.dawidweiss.dyna.corba.bindings.CBoardInfo;
import com.dawidweiss.dyna.corba.bindings.CBoardSnapshot;
import com.dawidweiss.dyna.corba.bindings.CPlayer;
import com.dawidweiss.dyna.corba.bindings.CStanding;
import com.dawidweiss.dyna.corba.bindings.ICControllerCallback;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerPOA;
import com.dawidweiss.dyna.view.IBoardSnapshot;
import com.dawidweiss.dyna.view.swing.BoardFrame;

/**
 * An adapter for {@link IPlayerController} updating player state on a remote game server and
 * displaying the current game state.
 */
public class KeyboardPlayerController extends ICPlayerControllerPOA
{
    private final static Logger logger = Logger.getLogger("player");

    private BoardFrame view;
    private CPlayer [] players;
    private CBoardInfo boardInfo;

    private final IPlayerController controller;
    private ICControllerCallback controllerCallback;
    private ControllerState last;

    public KeyboardPlayerController(IPlayerController controller)
    {
        this.controller = controller;
    }

    public synchronized void onStart(CBoardInfo boardInfo, CPlayer [] players)
    {
        logger.info("Game started.");
        this.players = players;
        this.boardInfo = boardInfo; 

        view = new BoardFrame(Adapters.adapt(boardInfo));
        view.setVisible(true);
    }

    public synchronized void onNextFrame(int frame, CBoardSnapshot snapshot)
    {
        /*
         * Check controller state updates.
         */
        final ControllerState now = new ControllerState(controller);
        if (last == null || !last.equals(now))
        {
            last = now;
            controllerCallback.update(Adapters.adapt(controller));
        }

        /*
         * Update local view.
         */
        final IBoardSnapshot adapted = Adapters.adapt(snapshot, boardInfo, players);
        view.onNextFrame(frame, adapted);
    }

    public synchronized void onEnd(CPlayer winner, CStanding [] standings)
    {
        logger.info("Game finished.");
        view.dispose();
        view = null;
    }

    public synchronized void onControllerSetup(ICControllerCallback callback)
    {
        this.controllerCallback = callback;
    }
}
