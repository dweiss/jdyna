package org.jdyna.frontend.swing;

import org.jdyna.network.sockets.GameHandle;
import org.jdyna.network.sockets.packets.ServerInfo;

/**
 * Game entry on the list of games to choose from.
 */
final class GameListEntry
{
    GameHandle handle;
    ServerInfo server;

    public GameListEntry(ServerInfo s, GameHandle handle)
    {
        this.server = s;
        this.handle = handle;
    }

    public String toString()
    {
        return server.serverAddress + " / " + handle.gameName;
    }
}
