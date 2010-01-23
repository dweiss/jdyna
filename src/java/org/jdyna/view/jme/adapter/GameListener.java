package org.jdyna.view.jme.adapter;

import org.jdyna.CellType;
import org.jdyna.GameConfiguration;
import org.jdyna.GameStateEvent;

public interface GameListener
{
    void gameStarted(CellType cells[][], int w, int h);

    void playerMoved(String name, float px, float py, boolean immortal);

    void playerSpawned(String name, int x, int y, boolean joined);

    void playerDied(String name);

    void bombPlanted(int i, int j);

    void bombExploded(int i, int j, int left, int right, int up, int down);

    void bonusSpawned(int i, int j, CellType bonusType);

    void bonusTaken(int i, int j);

    void crateCreated(int i, int j);

    void crateDestroyed(int i, int j);

    void updateStatus(int frame, GameStateEvent state);

    void setGameConfiguration(GameConfiguration conf);
}
