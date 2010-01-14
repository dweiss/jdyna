package org.jdyna.players.tyson.emulator;

import java.util.List;

import org.jdyna.*;

import org.jdyna.players.tyson.emulator.gamestate.GameState;

/**
 * <p>
 * To be implemented by players - bots.
 * </p>
 * <p>
 * Subclasses should implement: {@link IGameEventListener} and {@link IPlayerController}
 * </p>
 * 
 * @author Michał Kozłowski
 */
public abstract class AbstractPlayerEmulator implements IGameEventListener,
    IPlayerController
{
    protected final Player player;
    protected GameState state;
    protected GameConfiguration conf;

    /**
     * @param name Player's name.
     */
    public AbstractPlayerEmulator(final String name)
    {
        player = new Player(name, this);
    }

    /*
     * (non-Javadoc)
     * @see org.jdyna.IGameEventListener#onFrame(int, java.util.List)
     */
    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        for (GameEvent event : events)
        {
            if (event instanceof GameStartEvent)
            {
                conf = ((GameStartEvent) event).getConfiguration();
            }
            else if (event instanceof GameStateEvent)
            {
                if (state != null)
                {
                    state.update(frame, (GameStateEvent) event);
                }
                else
                {
                    state = new GameState(conf, frame, (GameStateEvent) event, player.name);
                }
            }
        }
    }

    /**
     * @return Player's name.
     */
    public String getName()
    {
        return player.name;
    }

    /**
     * @return {@link Player} instance.
     */
    public Player getPlayer()
    {
        return player;
    }
}
