package com.dawidweiss.dyna.corba;

import java.util.List;

import org.jdyna.GameEvent;
import org.jdyna.IGameEventListener;

import com.dawidweiss.dyna.corba.bindings.*;
import com.google.common.collect.Lists;

/**
 * We want to save some network resources, so this interface proxies all events from a
 * remote game to local implementations of {@link ICPlayerController} or {@link ICGameListener}.
 */
final class GameEventsProxy extends ICPlayerControllerPOA
{
    private final List<ICGameListenerOperations> listeners = Lists.newArrayList();
    private final List<ICPlayerControllerOperations> players = Lists.newArrayList();
    private final List<IGameEventListener> local = Lists.newArrayList();

    private CBoardInfo boardInfo;
    private CPlayer [] pNames;

    /**
     * Subscribes the player to both game and player events.
     */
    public void add(ICPlayerControllerOperations p)
    {
        players.add(p);
        listeners.add(p);
    }

    /**
     * Subscribes a game listener object to game events.
     */
    public void add(ICGameListenerOperations g)
    {
        listeners.add(g);
    }

    /**
     * Subscribes a local game listener object to adapted game events.
     */
    public void add(IGameEventListener g)
    {
        local.add(g);
    }

    @Override
    public void onControllerSetup(ICControllerCallback callback)
    {
        for (ICPlayerControllerOperations p : players)
        {
            p.onControllerSetup(callback);
        }
    }

    @Override
    public void onEnd(CGameResult result)
    {
        for (ICGameListenerOperations gl : listeners)
        {
            gl.onEnd(result);
        }
    }

    @Override
    public void onFrame(int frame, CGameEvent [] events)
    {
        for (ICGameListenerOperations gl : listeners)
        {
            gl.onFrame(frame, events);
        }

        List<GameEvent> adapted = Adapters.adapt(events, boardInfo, pNames);
        for (IGameEventListener gl : local)
        {
            gl.onFrame(frame, adapted);
        }
    }

    @Override
    public void onStart(CBoardInfo boardInfo, CPlayer [] players)
    {
        this.boardInfo = boardInfo;
        this.pNames = players;

        for (ICGameListenerOperations gl : listeners)
        {
            gl.onStart(boardInfo, players);
        }
    }
}
