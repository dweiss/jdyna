package com.dawidweiss.dyna.corba.client;

import java.util.List;

import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.audio.jxsound.GameSoundEffects;
import com.dawidweiss.dyna.corba.Adapters;
import com.dawidweiss.dyna.corba.bindings.*;
import com.dawidweiss.dyna.view.swing.BoardFrame;

/**
 * Displays the game board and state of a remote game. Adapts Corba structures
 * to their Java versions and forwards to normal views. 
 */
public class RemoteGameView extends ICGameListenerPOA
{
    /*
     * 
     */
    public synchronized void onStart(CBoardInfo boardInfo, CPlayer [] players)
    {
    }

    /*
     * 
     */
    public synchronized void onFrame(int frame, CGameEvent [] events)
    {
    }

    /*
     * 
     */
    public synchronized void onEnd(CPlayer winner, CStanding [] standings)
    {
    }
}
