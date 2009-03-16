package org.jdyna;

import org.apache.commons.lang.StringUtils;

/**
 * Full player name, including team name.
 */
public final class PlayerTeamName
{
    public final String teamName;
    public final String playerName;

    public PlayerTeamName(String playerName)
    {
        final int colon = playerName.indexOf(':');
        if (colon < 0)
        {
            this.playerName = playerName;
            this.teamName = "";
        }
        else
        {
            if (colon == 0 
                || colon == playerName.length() - 1
                || playerName.indexOf(':', colon + 1) >= 0)
            {
                throw new IllegalArgumentException("Invalid player name: " + playerName);
            }

            this.teamName = playerName.substring(0, colon);
            this.playerName = playerName.substring(colon + 1, playerName.length());
        }
    }

    /**
     * 
     */
    public String toString()
    {
        if (StringUtils.isEmpty(teamName)) return playerName;
        return teamName + ":" + playerName;
    }

    /**
     * Check if a given name is valid.
     */
    public static boolean isValid(String name)
    {
        try
        {
            new PlayerTeamName(name);
            return true;
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }
}
