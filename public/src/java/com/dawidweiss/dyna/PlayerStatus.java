package com.dawidweiss.dyna;

/**
 * Statistics and status of a single player during and after the game.
 */
public final class PlayerStatus
{
    /**
     * Player name.
     */
    public final String playerName;

    /**
     * Frame number when the last life was lost.
     */
    public int deathFrame;

    /**
     * Number of killed enemies.
     */
    public int killedEnemies;

    /**
     * Immortal status. 
     */
    public boolean immortal; 

    /**
     * Dead status.
     */
    public boolean dead; 

    /**
     * Number of lives left.
     */
    public int livesLeft; 
    
    /*
     * 
     */
    public PlayerStatus(String playerName)
    {
        this.playerName = playerName;
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
}