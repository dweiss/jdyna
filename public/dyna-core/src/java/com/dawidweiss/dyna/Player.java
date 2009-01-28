package com.dawidweiss.dyna;

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
        this(split(playerName)[1], split(playerName)[0], controller);
    }

    /**
     * Create a player belonging to a given team. 
     */
    public Player(String name, String team, IPlayerController controller)
    {
        assert name != null && controller != null;

        this.name = name;
        this.team = team;
        this.controller = controller;
    }

    /**
     * @return Splits the "team:playerName" pair into String[] {team, name}.
     */
    public static String [] split(String playerName)
    {
        final int colon = playerName.indexOf(':');
        if (colon < 0) return new String [] {null, playerName};
        if (colon == 0 || colon == playerName.length() - 1) 
            throw new IllegalArgumentException("Bad player name.");

        final String team = playerName.substring(0, colon);
        final String player = playerName.substring(colon + 1, playerName.length());
        return new String [] {team, player};
    }
}
