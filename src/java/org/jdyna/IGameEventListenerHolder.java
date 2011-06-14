package org.jdyna;

import java.util.Collection;


/**
 * Something capable of storing {@link IGameEventListener}s.
 */
public interface IGameEventListenerHolder
{
    public void addListener(IGameEventListener l);
    public void removeListener(IGameEventListener l);
    public Collection<IGameEventListener> getListeners();
}
