package com.jdyna.emulator;

import java.util.List;

import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.Player;
import com.jdyna.emulator.gamestate.GameState;

/**
 * <p>
 * To be implemented by players - bots.
 * </p>
 * <p>
 * Subclasses should implement: {@see IGameEventListener} and {@see IPlayerController}
 * </p>
 * 
 * @author Michał Kozłowski
 */
public abstract class AbstractPlayerEmulator implements IGameEventListener, IPlayerController {
	protected final Player player;
	protected GameState state;

	/**
	 * @param name Player's name.
	 */
	public AbstractPlayerEmulator(final String name) {
		player = new Player(name, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dawidweiss.dyna.IGameEventListener#onFrame(int, java.util.List)
	 */
	@Override
	public void onFrame(int frame, List<? extends GameEvent> events) {
		for (GameEvent event : events) {
			if (event instanceof GameStateEvent) {
				if (state != null) {
					state.update(frame, (GameStateEvent) event);
				} else {
					state = new GameState(frame, (GameStateEvent) event, player.name);
				}
			}
		}
	}

	/**
	 * @return Player's name.
	 */
	public String getName() {
		return player.name;
	}

	/**
	 * @return {@link Player} instance.
	 */
	public Player getPlayer() {
		return player;
	}
}
