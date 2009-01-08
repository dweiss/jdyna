package com.dawidweiss.dyna.corba.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.audio.jxsound.GameSoundEffects;
import com.dawidweiss.dyna.corba.Adapters;
import com.dawidweiss.dyna.corba.bindings.*;
import com.dawidweiss.dyna.view.swing.BoardFrame;

/**
 * An player taking part in a remote game.
 */
public class PlayerServant extends ICPlayerControllerPOA
{
    private final static Logger logger = LoggerFactory.getLogger("corba.player");

    /** Local player controller. */
    private final IPlayerController localController;

    /** 
     * Remote controller callback. We pump events from {@link #localController} to this object.
     */
    private ICControllerCallback remoteController;
    
    /** Last state of {@link #localController}. */
    private ControllerState last;

    /** Sound effects replay. */
    private GameSoundEffects sounds;
    
    /** Board view. */
    private BoardFrame view;

    /* */
    private CPlayer [] players;
    
    /* */
    private CBoardInfo boardInfo;

    /*
     * 
     */
    public PlayerServant(IPlayerController controller)
    {
        this.localController = controller;
    }

    /*
     * 
     */
    public synchronized void onStart(CBoardInfo boardInfo, CPlayer [] players)
    {
        this.players = players;
        this.boardInfo = boardInfo; 

        view = new BoardFrame();
        view.setVisible(true);

        sounds = new GameSoundEffects();
        logger.info("Game started.");        
    }

    /*
     * 
     */
    public synchronized void onFrame(int frame, CGameEvent [] events)
    {
        /*
         * Update local views.
         */
        final List<GameEvent> adapted = Adapters.adapt(events, boardInfo, players);
        view.onFrame(frame, adapted);
        sounds.onFrame(frame, adapted);

        /*
         * Dispatch controller state, if changed.
         */
        final ControllerState now = new ControllerState(localController);
        if (last == null || !last.equals(now))
        {
            last = now;
            remoteController.update(Adapters.adapt(localController));
        }
    }

    /*
     * 
     */
    public synchronized void onEnd(CPlayerStatus [] players)
    {
        view.dispose();
        view = null;
        sounds = null;

        logger.info("Game finished.");
    }

    /*
     * 
     */
    public synchronized void onControllerSetup(ICControllerCallback callback)
    {
        this.remoteController = callback;
    }
}
