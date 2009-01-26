package com.dawidweiss.dyna;

import java.io.Serializable;

/**
 * Statistics and status of a single player during and after the game.
 */
@SuppressWarnings("serial")
public final class PlayerStatus implements Serializable
{
    /**
     * Player name.
     */
    String playerName;

    /**
     * Frame number when the last life was lost.
     */
    int deathFrame;

    /**
     * Number of killed enemies.
     */
    int killedEnemies;

    /**
     * Immortal status. 
     */
    boolean immortal; 

    /**
     * Dead status.
     */
    boolean dead; 

    /**
     * Number of lives left.
     */
    int livesLeft; 
    
    /*
     * 
     */
    public PlayerStatus(String playerName)
    {
        this.playerName = playerName;
    }

    /*
     * 
     */
    public PlayerStatus(String playerName, int deathFrame, int killedEnemies,
        boolean immortal, boolean dead, int livesLeft)
    {
        this.playerName = playerName;
        this.deathFrame = deathFrame;
        this.killedEnemies = killedEnemies;
        this.immortal = immortal;
        this.dead = dead;
        this.livesLeft = livesLeft;
    }

    @Override
    public String toString()
    {
        return playerName
            + ";"
            + (dead ? " dead" : "")
            + (dead ? " immortal" : "")
            + " lives=" + livesLeft
            + " enemies=" + killedEnemies
            + " deathFrame=" + deathFrame;
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public int getDeathFrame()
    {
        return deathFrame;
    }

    public int getKilledEnemies()
    {
        return killedEnemies;
    }

    public boolean isImmortal()
    {
        return immortal;
    }

    public boolean isDead()
    {
        return dead;
    }

    public int getLivesLeft()
    {
        return livesLeft;
    }
}