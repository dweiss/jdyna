package org.jdyna;

/**
 * A single player in the game.
 */
public final class Player
{
    /**
     * The player can be in the following states (possibly a combination of these at the
     * same time).
     */
    public static enum State
    {
        LEFT, RIGHT, UP, DOWN, DEAD, DYING
    }

    /**
     * The player's unique identifier.
     */
    public final String name;

    /**
     * The player's team identifier.
     */
    public final String team;

    /**
     * Movement controller for this player.
     */
    public final IPlayerController controller;

    /**
     * Parses player name from two ':'-separated parts. The part before
     * the colon (optional) is the team name, the part after the colon is the player's
     * name.
     */
    public Player(String playerName, IPlayerController controller)
    {
        this(split(playerName), controller);
    }

    /**
     * Create a player belonging to a given team. 
     */
    public Player(PlayerTeamName p, IPlayerController controller)
    {
        assert p != null && controller != null;

        this.name = p.playerName;
        this.team = p.teamName;
        this.controller = controller;
    }

    /**
     * @return Splits the "team:playerName" pair into String[] {team, name}.
     */
    public static PlayerTeamName split(String playerName)
    {
        return new PlayerTeamName(playerName);
    }
}
