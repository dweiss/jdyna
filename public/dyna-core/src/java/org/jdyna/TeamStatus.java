package org.jdyna;

import java.io.Serializable;

/**
 * Statistics for a single team of players.
 */
@SuppressWarnings("serial")
public final class TeamStatus implements Serializable
{
    /**
     * Team name.
     */
    public TeamStatus(String teamName)
    {
        this.teamName = teamName;
    }

    /**
     * Number of players in this team, overall (dead and alive).
     */
    int playersTotal; 

    /**
     * Number of killed enemies, overall.
     */
    int killedEnemies;

    /**
     * Number of players left, overall.
     */
    int playersLeft; 

    /**
     * Number of lives left, overall.
     */
    int livesLeft;

    /**
     * Team name.
     */
    private String teamName; 

    /*
     * 
     */
    public TeamStatus(String teamName, int killedEnemies, int playersLeft, int livesLeft)
    {
        this.teamName = teamName;
        this.killedEnemies = killedEnemies;
        this.playersLeft = playersLeft;
        this.livesLeft = livesLeft;
    }

    /**
     * Add a given player to statistics.
     */
    public void add(PlayerStatus status)
    {
        this.killedEnemies += status.getKilledEnemies();
        this.livesLeft += status.getLivesLeft();
        this.playersLeft += status.isStoneDead() ? 0 : 1;
        this.playersTotal += 1;
    }

    @Override
    public String toString()
    {
        return teamName
            + ";"
            + " players=" + playersLeft
            + " enemies=" + killedEnemies
            + " lives=" + livesLeft;
    }
    
    public int getKilledEnemies()
    {
        return killedEnemies;
    }
    
    public int getLivesLeft()
    {
        return livesLeft;
    }
    
    public int getPlayersLeft()
    {
        return playersLeft;
    }
    
    public String getTeamName()
    {
        return teamName;
    }

    public int getPlayersTotal()
    {
        return playersTotal;
    }
}