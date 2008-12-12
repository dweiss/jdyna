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
     * Movement controller for this player.
     */
    public final IPlayerController controller;

    /*
     * 
     */
    public Player(String name, IPlayerController controller)
    {
        assert name != null && controller != null;

        this.name = name;
        this.controller = controller;
    }
}
