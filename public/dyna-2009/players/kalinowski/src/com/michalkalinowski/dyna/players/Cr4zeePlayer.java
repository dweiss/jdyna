package com.michalkalinowski.dyna.players;

import java.util.List;

import org.jdyna.*;


public final class Cr4zeePlayer implements IPlayerController, IPlayerController2,
    IGameEventListener
{
    public static final String NAME = "Cr4zee";
    public static final String VENDOR = "Michal Kalinowski";

    private String name;
    private BoardInfo boardInfo;
    private Tactics tactics;
    private int frame;
    private Direction direction;
    private boolean dropsBomb;

    public Cr4zeePlayer()
    {
        this.name = NAME;
    }

    public Cr4zeePlayer(String name)
    {
        this.name = name;
    }

    public static Player createPlayer(String name)
    {
        return new Player(name, new Cr4zeePlayer(name));
    }

    @Override
    public boolean dropsBomb()
    {
        return getState().dropsBomb;
    }

    @Override
    public Direction getCurrent()
    {
        return getState().direction;
    }

    @Override
    public ControllerState getState()
    {
        return new ControllerState(this.direction, this.dropsBomb);
    }

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        this.frame = frame;
        for (GameEvent event : events)
        {
            if (event.type == GameEvent.Type.GAME_START)
            {
                gameStartEventHandler((GameStartEvent) event);
            }
            if (event.type == GameEvent.Type.GAME_STATE)
            {
                gameStateEventHandler((GameStateEvent) event);
            }
            if (event.type == GameEvent.Type.GAME_OVER)
            {
                gameOverEventHandler((GameOverEvent) event);
            }
        }
    }

    private void gameStartEventHandler(GameStartEvent event)
    {
        this.boardInfo = event.getBoardInfo();
        this.tactics = new Tactics(this.name, this.boardInfo);
        this.direction = null;
        this.dropsBomb = false;
    }

    private void gameOverEventHandler(GameOverEvent event)
    {
        // do nothing... (for now)
    }

    private void gameStateEventHandler(GameStateEvent event)
    {
        tactics.updateState(this.frame, event.getCells(), event.getPlayers());
        tactics.process();
        this.direction = tactics.direction;
        this.dropsBomb = tactics.dropsBomb;
    }
}
