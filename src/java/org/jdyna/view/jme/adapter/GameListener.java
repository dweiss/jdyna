package org.jdyna.view.jme.adapter;

import org.jdyna.view.jme.adapter.AbstractGameAdapter.BonusType;
import org.jdyna.view.jme.adapter.AbstractGameAdapter.DynaCell;

public interface GameListener
{
    void gameStarted(DynaCell cells[][],int w,int h);
    
    void playerMoved(String name,float px,float py, boolean immortal);
    void playerSpawned(String name, int x, int y, boolean joined);
    void playerDied(String name);
    
    void bombPlanted(int i,int j);
    void bombExploded(int i, int j, int left, int right, int up, int down);
    
    void bonusSpawned(int i,int j,BonusType bonusType);
    void bonusTaken(int i,int j);
    
    void crateCreated(int i,int j);
    void crateDestroyed(int i,int j);
}
